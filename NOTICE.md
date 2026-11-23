# Third-party notices

This repository contains **only our own patch code**. It does **not** contain, mirror or
relicense any part of ReplayMod, Minecraft, Forge or the bundled libraries described below.
To build a working mod you supply the upstream ReplayMod jar yourself.

---

## ReplayMod (the mod this project patches)

* Project: <https://github.com/ReplayMod/ReplayMod>
* Version targeted: **1.7.10-2.5.2** (the last release supporting Minecraft 1.7.10)
* Authors: **CrushedPixel, johni0702** and contributors
* License: **MIT** — the upstream jar ships `LICENSE.txt` starting with
  `The MIT License (MIT)`.

The files under `overlay/` are **modified copies of files that live inside the upstream jar**
(`META-INF/MANIFEST.MF`, `mixins.recording.replaymod.json`,
`assets/replaymod/lang/zh_CN.lang`). They are kept here so the patch can be applied
reproducibly. Their original MIT copyright remains with the ReplayMod authors; the
modifications are ours and are released under the same MIT terms.

If you redistribute a **built jar** (upstream code + our patch), keep the upstream
`LICENSE.txt` / `NOTICE.txt` inside it intact and credit the ReplayMod authors — that is
what the MIT license asks for.

## Third-party code embedded in the upstream jar

The upstream ReplayMod jar embeds several libraries (among them **ASM** under
`org/spongepowered/asm/lib/**` and **Apache Commons Codec** under
`com/replaymod/lib/org/apache/**`), mostly under the **Apache License 2.0**; the jar's own
`NOTICE.txt` and `META-INF/LICENSE.txt` carry their notices. **None of that code is
included in this repository** — it stays in the jar you supply.

## Minecraft, Minecraft Forge, GT New Horizons

Not distributed here in any form. ReplayMod itself, and this patch, are fan-made
modifications and are not affiliated with Mojang or the GTNH team.

## Build-time only

The build script expects a Minecraft 1.7.10 + Forge development jar
(`recompiled_minecraft-1.7.10.jar`) and the ReplayMod jar on the compile classpath.
Neither is committed; both are used only to compile, and no part of them ends up in this
repository's source tree.

---

## Who made this

* **The maintainer** — in-game testing, field reports, and the decisions about what to fix.
* **An AI coding agent** (DeepSeek Harness, `deepseek-flash` / `deepseek-v4-pro`) — implemented the
  patch layer, did the bytecode archaeology (`javap` against both the MCP-named and the SRG-named
  Minecraft jars), and wrote this documentation.
* **Two external testers** on a ~340-mod pack, who ran builds in a real GTNH world and reported what
  actually broke.

### Approach credit

The **patch-layer approach** — inject Mixin/ASM into the official jar instead of rebuilding the dead
1.7.10 build chain — was inspired by the exploration in the
[`replaymod-gtnh-compat`](https://github.com/Luoxueruoling/replaymod-gtnh-compat) project.
Thanks for paving the way.

### Toolchain

Java 8 `javac` · **Mixin 0.8.7 / UniMixins** · **ASM 9.x** · **RetroFuturaGradle** (the 1.7.10 dev
jars used for compilation) · `javap` and the JDK's zip APIs for verification · PowerShell for the
build scripts · and of course the **ReplayMod / ReplayStudio** jars themselves, which are the reason
any of this is possible.