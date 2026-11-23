# Release notes — release1.0

**Attach to this release:** `Replaymod-2.5.2-GTNH-release1.0.jar`
(`10881470` bytes · `SHA256 C2E7953B7E964AED48F61BFFA9C55BB495CE3B2256F2D5F501E74F12B6F8B208`)

> ⚠️ **Disclaimer**: the patch layer was implemented **mainly by an AI coding agent (DeepSeek V4.1 Pro / Flash)**. The maintainer is not a professional programmer and does not fully understand the code, so **unknown defects may exist** — use at your own risk.

---

## What this is

**ReplayMod 1.7.10-2.5.2, made usable on GT New Horizons 2.9.0** (Minecraft 1.7.10 on lwjgl3ify /
LWJGL 3 / SDL3 with no GLFW, Angelica, Mixin 0.8.7 via UniMixins, Java 17–26).

Stock ReplayMod either fails to start on this stack or breaks in many ways. This build adds a patch
layer (`com.gtnh.replaycompat`: 29 mixins + 9 helper classes + a coremod/ASM entry point) on top of
the **unmodified upstream jar** — nothing is deleted from it, three of its files are replaced, and
52 entries are added.

## Known limitations (accepted for 1.0)

Full list in [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md). Highlights:

* **Replaying a dimension change to an unregistered / dynamic dimension crashes** (e.g. PersonalSpace
  personal dimension 180). *Workaround:* replay in the same environment, or only replays that never
  changed dimension.
* **A recording started after the world finished loading contains no terrain / no player entry.**
  Enable ReplayMod's "auto start recording".
* A disappearing player during a replay is usually the camera being in first person / on a spectated
  player, not a missing entity.
* With the mouse unlocked, **WASD does not drive the camera** — upstream design (press Ctrl).

## Install

1. Put `Replaymod-2.5.2-GTNH-release1.0.jar` into the GTNH instance `mods/`.
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
| Camera | speed resets on dimension change | `ReplayCameraSpeed` |
| Recording | no entry for the recording player | `MixinS0CPacketSpawnPlayerProfileGuard` + `ReplaySpawnProfile` |
| Recording | log flooded with `RejectedExecutionException` | `MixinPacketListenerSaveGuard` |
| Editing | saving / applying markers crashed | 2 mixins |
| Export | video export rendered a blank frame | `MixinPipelinesNoPbo`, `MixinPboFrameCapturerGlReadPixels` |
| Misc | AE2+Waila crash; Et Futurum NPE; multiplayer handshake disconnect | 3 mixins |

## Verifying your download

```powershell
Get-FileHash .\Replaymod-2.5.2-GTNH-release1.0.jar -Algorithm SHA256
# expected: C2E7953B7E964AED48F61BFFA9C55BB495CE3B2256F2D5F501E74F12B6F8B208
```

The jar contains **6153** unique entries (6154 raw); the upstream jar has 6101 unique (6102 raw).
Both numbers are +52 over upstream.

## Building it yourself

See [`BUILDING.md`](BUILDING.md).

## Credits

**Luoxueruoling** (author, all in-game testing and field reports), an **AI coding agent**
(DeepSeek Harness / `deepseek-flash`, implementation and bytecode analysis), two external testers,
and the toolchain: Java 8 `javac`, Mixin 0.8.7 / UniMixins, ASM 9.x, RetroFuturaGradle, `javap`.
Upstream ReplayMod is **MIT** by CrushedPixel and johni0702.

## License

MIT — see [`LICENSE`](../LICENSE). The upstream jar is not redistributed in the repository; this
release attaches a jar that contains it, with its original `LICENSE.txt` / `NOTICE.txt` kept inside.
