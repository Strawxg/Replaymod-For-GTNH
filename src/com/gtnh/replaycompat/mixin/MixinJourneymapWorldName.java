package com.gtnh.replaycompat.mixin;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Stops JourneyMap's replay-time {@code NullPointerException} flood by giving it a stable world name
 * while a replay is open.
 *
 * <h3>The flood (verified in GTNH 2.9.1, JourneyMap 5.2.23-fairplay)</h3>
 *
 * <pre>
 * journeymap.client.data.WorldData#getServerName()          (private static)
 *   ForgeHelper.INSTANCE.getRealmsServerName().replace(':','_')   -&gt; NPE, Realms name is null in a replay
 *   (catch) log.error("Couldn't get service name: {}", ...)       -&gt; this is the line that floods
 *   getLegacyServerName().replace(':','_')                        -&gt; also null in a replay (no socket) -&gt; NPE escapes
 *
 * journeymap.client.data.WorldData#getWorldName(Minecraft, boolean)   (public static)
 *   !mc.isSingleplayer()  -&gt; worldName = theWorld.worldInfo.getWorldName(); serverName = getServerName(); ...
 *   -&gt; the escaped NPE kills the JourneyMap task that called it
 *
 * journeymap.client.io.FileHandler#getJMWorldDir(World)  -&gt;  ...getJMWorldDirForWorldId -&gt; throws
 * called from journeymap.client.thread.RunnableTask and JourneymapClient.stopMapping (WorldEventHandler.Unload)
 * </pre>
 *
 * <p>Observed cost in GTNH 2.9.1: 554 error lines in one 10-minute recording session, 217 in a 77-second
 * replay, one group per dimension change, each with a full stack trace - plus the JourneyMap tasks that
 * die with it re-scheduling themselves, so the client keeps reloading JourneyMap state.
 *
 * <h3>Why here and not at {@code getServerName()}</h3>
 *
 * <p>{@code getServerName()} cannot be fixed by an {@code @At("RETURN")} hook: the exception is thrown
 * inside it, so the hook never runs. {@code getRealmsServerName()} is also the wrong place to patch
 * unconditionally: in <b>normal multiplayer</b> it also returns {@code null} (1.7.10 has no Realms), and
 * JourneyMap deliberately falls back to the socket host name there - substituting a constant globally
 * would rename every multiplayer map directory. So the patch is gated on "a replay is open" and only
 * then short-circuits {@code getWorldName()}, which is the single entry point both flooding call paths
 * go through.
 *
 * <p>The returned name is a constant: JourneyMap only uses it to pick its on-disk map directory, and
 * nothing it records during a replay is worth keeping per-world. It never touches {@code theWorld}
 * itself, so the replay's terrain and entity state are unaffected.
 *
 * <p>"A replay is open" comes from {@code ReplayModReplay.instance.getReplayHandler() != null}, resolved
 * reflectively and cached - the same pattern as {@code MixinNoFirstPersonHandInReplay}. Every failure
 * path leaves JourneyMap's own behaviour untouched ({@code require = 0}, {@code remap = false}).
 */
@Mixin(targets = "journeymap.client.data.WorldData")
public class MixinJourneymapWorldName {

    /**
     * Constant world directory name JourneyMap uses while a replay is open. Not localised and not
     * per-replay on purpose: a single throw-away directory beats a new one for every recording.
     */
    private static final String REPLAY_WORLD_NAME = "ReplayMod";

    private static Method getReplayHandler;
    private static Object replayModReplayInstance;
    private static boolean resolved;
    private static boolean warned;

    @Inject(method = "getWorldName", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void replaycompat$stableWorldNameInReplay(Minecraft mc, boolean flag,
            CallbackInfoReturnable<String> cir) {
        try {
            Object handler = replaycompat$replayHandler();
            if (handler == null) {
                return; // not in a replay: JourneyMap keeps its normal naming
            }
            cir.setReturnValue(REPLAY_WORLD_NAME);
        } catch (Throwable t) {
            if (!warned) {
                warned = true;
                try {
                    System.out.println("[replaycompat] journeymap world-name guard failed: " + t);
                } catch (Throwable ignored) {
                    // logging must never escape
                }
            }
        }
    }

    private static Object replaycompat$replayHandler() throws Exception {
        if (!resolved) {
            replaycompat$resolve();
        }
        if (getReplayHandler == null || replayModReplayInstance == null) {
            return null;
        }
        return getReplayHandler.invoke(replayModReplayInstance);
    }

    private static synchronized void replaycompat$resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        try {
            Class<?> replayModReplay = Class.forName("com.replaymod.replay.ReplayModReplay");
            getReplayHandler = replayModReplay.getMethod("getReplayHandler");
            Field instance = replayModReplay.getField("instance");
            replayModReplayInstance = instance.get(null);
        } catch (Throwable t) {
            getReplayHandler = null;
            replayModReplayInstance = null;
        }
    }
}
