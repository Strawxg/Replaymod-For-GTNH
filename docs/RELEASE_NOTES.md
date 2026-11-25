# Release notes — release1.2

**Attach to this release:** `Replaymod-2.5.2-GTNH-release1.2.jar`
(`10884633` bytes · `SHA256 81523FFD75334E2295CC67AC233EE06153D07BF56807BC9B2121730C2F1EBB41`)

> ⚠️ **Disclaimer**: the patch layer was implemented **mainly by an AI coding agent (DeepSeek Harness / flash)**. The maintainer is not a professional programmer and does not fully understand the code, so **unknown defects may exist** — use at your own risk.

## What changed in 1.2

* **JourneyMap's replay-time NPE flood is gone.** While a replay was open, JourneyMap asked the game
  for a "Realms server name", got `null` (1.7.10 has no Realms), and then logged a full stack trace
  every time it resolved its map directory — once per dimension change. Measured on GTNH 2.9.1:
  **217 lines in a 77-second replay** (`554` in one 10-minute session). 1.2 hands JourneyMap a stable
  world name while a replay is open (`MixinJourneymapWorldName`, hooked at
  `journeymap.client.data.WorldData#getWorldName`), so the null path is never entered. Outside a
  replay JourneyMap behaves exactly as before; the patch never touches the replay's world data,
  dimension logic, input handling or draw order.
* The 1.1 "skip an unregistered-dimension respawn" guard is unchanged.

## Tested with

* **GT New Horizons 2.9.2** (Java 26, Qomicex launcher): recorded Overworld → personal dimension 180 →
  Twilight Forest → Overworld, replayed at 2×; 30 mixins applied, no crash, `Couldn't get service name`
  went from 217 (2.9.1) to **0**.
* GTNH 2.9.0 / 2.9.1: the same dependency check passes (AE2 `rv3-beta-1104`, Et Futurum `2.6.66`,
  CustomMainMenu `1.14.1`, UniMixins `0.3.1` — the target class bytes are identical on all three).

## Known limitations (accepted for 1.2)

Full list in [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md). Highlights:

* **Other mods' interfaces still overlap the replay timeline.** Galaxy Map, GT machine screens, NEI and
  the AE2 / GT input-output bus are drawn on top of the timeline and speed bar. Draw-order issue,
  **not fixed in 1.2** — it is the first target of the new **Alpha branch** (Alpha → beta → release),
  and will not reach a release before it is verified there.
* **A recording started after the world finished loading contains no terrain / no player entry.**
  Enable ReplayMod's "auto start recording".
* A replay that respawns into a dimension the client has never registered still skips that dimension
  change (1.1 behaviour, unchanged).

## Install

1. Put `Replaymod-2.5.2-GTNH-release1.2.jar` into the GTNH instance `mods/`.
2. Make sure these runtime dependencies are present: **UniMixins ≥ 0.1.7**, **GTNHLib ≥ 0.6.6**,
   **LWJGL3ify ≥ 1.5.7**.
3. Remove the original `replaymod-1.7.10-2.5.2.jar` (same modid — this jar replaces it).
4. Restart. The "回放中心" entry appears on the main menu.
5. Video export needs ffmpeg: drop `ffmpeg.exe` at `<GAMEDIR>\ffmpeg\bin\ffmpeg.exe` (or anywhere
   under the game directory). Tested with **ffmpeg 9.1 full_build**.

## Verifying your download

```powershell
Get-FileHash .\Replaymod-2.5.2-GTNH-release1.2.jar -Algorithm SHA256
# expected: 81523FFD75334E2295CC67AC233EE06153D07BF56807BC9B2121730C2F1EBB41
```

The jar contains **6154** unique entries (6155 raw), 52 classes under `com/gtnh/replaycompat/**`
(30 mixins). Upstream has 6101 unique (6102 raw): both numbers are +53.

---

# Release notes — release1.1 (previous)

**Attach to this release:** `Replaymod-2.5.2-GTNH-release1.1.jar`
(`10882998` bytes · `SHA256 E9AACAB9DF6859151A537CF7419C078314EB7B9419D8DD74F28412429F9EE1DB`)

> ⚠️ **Disclaimer**: the patch layer was implemented **mainly by an AI coding agent (DeepSeek V4.1 Pro / Flash)**. The maintainer is not a professional programmer and does not fully understand the code, so **unknown defects may exist** — use at your own risk.

---

## What this is

**ReplayMod 1.7.10-2.5.2, made usable on GT New Horizons 2.9.0** (Minecraft 1.7.10 on lwjgl3ify /
LWJGL 3 / SDL3 with no GLFW, Angelica, Mixin 0.8.7 via UniMixins, Java 17–26).

Stock ReplayMod either fails to start on this stack or breaks in many ways. This build adds a patch
layer (`com.gtnh.replaycompat`: 29 mixins + 9 helper classes + a coremod/ASM entry point) on top of
the **unmodified upstream jar** — nothing is deleted from it, three of its files are replaced, and
52 entries are added.

## What changed in 1.1

* **Replaying a respawn to a dimension this client has not registered no longer crashes.**
  Vanilla would build a `WorldClient` for the target dimension and throw
  `IllegalArgumentException: Could not get provider type for dimension N` — this is what happened when
  a recording entered **PersonalSpace's personal dimension (dimension 180)**. The respawn is now
  skipped, the replay keeps the world it already has, and one diagnostic line is written to
  `logs/fml-client-latest.log`:

  ```
  [replaycompat] skipped respawn to unregistered dimension 180 (registered: -1,0,1,2,7,...)
  ```

  The judge is `DimensionManager.isDimensionRegistered(dim)` — no dimension id or mod name is
  hardcoded, so any mod's "registered at world load / on demand" dimension is covered (personal
  dimensions, modded planets, outer lands, ...). Dimensions that are registered normally
  (vanilla, Galacticraft, and everything else present at replay time) behave exactly as before.
  **What you lose:** that one dimension change is not shown — the replay stays in the previous
  dimension for the skipped segment.

## Known limitations (accepted for 1.1)

Full list in [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md). Highlights:

* **Other mods' interfaces overlap the replay timeline.** Galaxy Map, GT machine screens and GC
  warnings (e.g. the oxygen warning) are drawn on top of the timeline and speed bar, so you cannot see
  or click the timeline while they are open. This is a draw-order issue: the replay overlay is drawn
  in the HUD layer (`RenderGameOverlayEvent.Text`), while those screens/HUDs draw later. **Not fixed in
  1.1.**
* **A recording started after the world finished loading contains no terrain / no player entry.**
  Enable ReplayMod's "auto start recording".
* A disappearing player during a replay is usually the camera being in first person / on a spectated
  player, not a missing entity.
* With the mouse unlocked, **WASD does not drive the camera** — upstream design (press Ctrl).

## Install

1. Put `Replaymod-2.5.2-GTNH-release1.1.jar` into the GTNH instance `mods/`.
2. Make sure these runtime dependencies are present: **UniMixins ≥ 0.1.7**, **GTNHLib ≥ 0.6.6**,
   **LWJGL3ify ≥ 1.5.7**.
3. Remove the original `replaymod-1.7.10-2.5.2.jar` (same modid — this jar replaces it).
4. Restart. The "回放中心" entry appears on the main menu.
5. Video export needs ffmpeg: drop `ffmpeg.exe` at `<GAMEDIR>\ffmpeg\bin\ffmpeg.exe` (or anywhere
   under the game directory). Tested with **ffmpeg 9.1 full_build**.

## What this build fixes

| Area | Symptom | Fixed by |
|---|---|---|
| Startup | stock transformers made GTNH unusable | transformers re-implemented, made opt-in |
| Main menu | no Replay entry; panel not clickable | 3 mixins |
| Input | mouse lock loops / stuck mouse / dead hotkeys / broken wheel | 6 patches + a key bridge |
| Replay | opened in first person with hand rendered; crashes while loading | 4 mixins |
| Replay | crash on a respawn to an unregistered dimension (e.g. personal dimension 180) | `MixinNetHandlerPlayClientNullWorldGuard` + `NullWorldGuard` |
| Camera | speed resets on dimension change | `ReplayCameraSpeed` |
| Recording | no entry for the recording player | `MixinS0CPacketSpawnPlayerProfileGuard` + `ReplaySpawnProfile` |
| Recording | log flooded with `RejectedExecutionException` | `MixinPacketListenerSaveGuard` |
| Editing | saving / applying markers crashed | 2 mixins |
| Export | video export rendered a blank frame | `MixinPipelinesNoPbo`, `MixinPboFrameCapturerGlReadPixels` |
| Misc | AE2+Waila crash; Et Futurum NPE; multiplayer handshake disconnect | 3 mixins |

## Verifying your download

```powershell
Get-FileHash .\Replaymod-2.5.2-GTNH-release1.1.jar -Algorithm SHA256
# expected: E9AACAB9DF6859151A537CF7419C078314EB7B9419D8DD74F28412429F9EE1DB
```

The jar contains **6153** unique entries (6154 raw); the upstream jar has 6101 unique (6102 raw).
Both numbers are +52 over upstream.

## Building it yourself

See [`BUILDING.md`](BUILDING.md).

## Credits

**The maintainer** (in-game testing and field reports), an **AI coding agent**
(DeepSeek Harness / `deepseek-flash`, implementation and bytecode analysis), two external testers,
and the toolchain: Java 8 `javac`, Mixin 0.8.7 / UniMixins, ASM 9.x, RetroFuturaGradle, `javap`.
Upstream ReplayMod is **MIT** by CrushedPixel and johni0702.

> The patch-layer approach was inspired by the
> [`replaymod-gtnh-compat`](https://github.com/Luoxueruoling/replaymod-gtnh-compat) project. Thanks for
> paving the way.

## License

MIT — see [`LICENSE`](../LICENSE). The upstream jar is not redistributed in the repository; this
release attaches a jar that contains it, with its original `LICENSE.txt` / `NOTICE.txt` kept inside.
