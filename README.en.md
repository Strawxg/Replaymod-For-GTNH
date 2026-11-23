# ReplayMod 1.7.10 · GT New Horizons Compatibility Patch

[简体中文](README.md) | **English** | [日本語](README.ja.md) | [한국어](README.ko.md)

> Makes **ReplayMod 1.7.10 (2.5.2)** actually work on **GT New Horizons 2.9.0 Stable (Java 17–26 / lwjgl3ify)**:
> recording, replay, and video export.
> Based on ReplayMod (MIT, by **CrushedPixel** / **johni0702**).

**Current version: Release 1.0** · jar: `Replaymod-2.5.2-GTNH-release1.0.jar`

> ⚠️ **Disclaimer (please read)**: this patch layer was implemented **mainly by an AI coding agent (DeepSeek V4.1 Pro / Flash)**. The maintainer is not a professional programmer and does not fully understand the code, so **unknown defects, compatibility issues or data risks may exist**. Use at your own risk; feedback is welcome, but fixes are not guaranteed.

---

## What this is

An **injection-style patch layer**: it does not recompile ReplayMod's source. Instead it uses Mixin/ASM
at runtime to patch the official `replaymod-1.7.10-2.5.2.jar`, and adds the runtime dependencies GTNH
needs (UniMixins / GTNHLib / LWJGL3ify).

- **Contains no ReplayMod / Forge / Minecraft source code** (see [`NOTICE.md`](NOTICE.md)).
- The artifact is a **complete replacement** for the official jar (all upstream classes + the patch layer):
  you do **not** need the original ReplayMod jar as well.

## Install

1. Download `Replaymod-2.5.2-GTNH-release1.0.jar` and put it into the GTNH instance's `mods/`.
2. Make sure the following **runtime dependencies** are present (all required):

   | Dependency | Minimum | Notes |
   |---|---|---|
   | UniMixins | 0.1.7 | Mixin 0.8.7 runtime |
   | GTNHLib | 0.6.6 | GTNH common library |
   | LWJGL3ify | 1.5.7 (+ forgePatches) | LWJGL3 compat layer for Java 17+ |

3. **Remove the original** `replaymod-1.7.10-2.5.2.jar`: both use the same modid (`replaymod`), so
   having both loaded causes a duplicate-mod conflict.
4. Launch the game; the "回放中心" (Replay Center) entry appears on the main menu. Use ReplayMod's
   keybind (default) to start/stop recording.

## Video export needs ffmpeg

ReplayMod's video export depends on **ffmpeg**. On Windows it looks for it in this order:

1. `<GAMEDIR>\ffmpeg\bin\ffmpeg.exe` (**recommended**)
2. otherwise, recursively search the **whole game directory** for any `ffmpeg.exe`
3. if neither is found → export reports "要渲染视频需要安装 ffmpeg" (installing ffmpeg is required)

**How to install (either one)**:

- **Recommended**: put the downloaded Windows `ffmpeg.exe` at
  `<GAMEDIR>\ffmpeg\bin\ffmpeg.exe`
  (`<GAMEDIR>` is the instance's `.minecraft` / version directory).
- Or: put `ffmpeg.exe` anywhere under the game directory.

**Download**: <https://ffmpeg.org/download.html> or <https://www.gyan.dev/ffmpeg/builds/>
(needs a build with `libx264`; essentials or full both work).
This patch was tested with **ffmpeg 9.1 full_build**.

> The default export command is:
> `-y -f rawvideo -pix_fmt bgra -s <W>x<H> -r <FPS> -i - -an -c:v libx264 -b:v <bitrate> -pix_fmt yuv420p "<out.mp4>"`
> It works on ffmpeg 7/8/9.

## Known limitations (accepted for Release 1.0)

- **Replaying a dimension change to an unregistered / dynamic dimension crashes** — e.g. PersonalSpace's
  personal dimension (dimension 180). If the recording contains a respawn to such a dimension, the
  client's `DimensionManager` cannot find it and Forge throws.
  *Workaround*: replay in the **same environment** it was recorded in, or only replay recordings that
  **never changed dimension**.
- **A recording started after the world finished loading may lack terrain / the player entry** —
  ReplayMod only records chunks/players when they first arrive.
  *Suggestion*: enable ReplayMod's **AUTO_START_RECORDING** so recording starts as soon as you enter.
- **A "disappearing player" during a replay is usually a camera issue** (the camera is in first person
  or attached to a spectated player), not a missing entity; use the free camera.
- More complete known issues: [`docs/KNOWN_ISSUES.md`](docs/KNOWN_ISSUES.md).

## Building (optional, to reproduce the jar from source)

See [`docs/BUILDING.md`](docs/BUILDING.md). Requires:

- **JDK 8** (compile the patch layer), **JDK 25 + Gradle 9.4.1** (use RFG to produce the decompiled base
  `recompiled_minecraft-1.7.10.jar`)
- Compile dependencies (**not distributed with this repository**): the official
  `replaymod-1.7.10-2.5.2.jar`, the Forge 1.7.10 dev jar, launchwrapper, netty, gson, guava, asm, AE2, etc.
- Two steps: `tools/build.ps1` compiles the patch layer → `tools/apply.ps1` injects it into the official jar.

## License & credits

- This patch layer is released under **MIT** (see [`LICENSE`](LICENSE)).
- Based on **ReplayMod** (MIT, by **CrushedPixel** and **johni0702**); the patched jar keeps its
  original `LICENSE.txt`.
- Third-party notices: [`NOTICE.md`](NOTICE.md); exact upstream relationship (including the target jar's
  SHA-256): [`UPSTREAM.md`](UPSTREAM.md).
