# ReplayMod 1.7.10 · GT New Horizons 兼容补丁

**简体中文** | [English](README.en.md) | [日本語](README.ja.md) | [한국어](README.ko.md)

> 让 **ReplayMod 1.7.10（2.5.2）** 在 **GTNH 2.9.0 Stable正式版（Java 17–26 / lwjgl3ify）** 上正常工作：
> 录制、回放、导出视频。
> 基于 ReplayMod（MIT，原作者 **CrushedPixel** / **johni0702**）。

**当前版本：Release 1.1** · 对应 jar：`Replaymod-2.5.2-GTNH-release1.1.jar`

> ⚠️ **免责声明（务必阅读）**：本补丁层的实现**主要由 AI 编码代理（DeepSeek V4.1 Pro / Flash）完成**；维护者本人不是专业程序员，未完全掌握相关计算机代码知识。因此本项目**可能存在未知缺陷、兼容性问题或数据风险**。请**自行评估风险**后使用；欢迎反馈问题，但**不保证及时修复**。

---

## 这是什么

本仓库是**补丁层（patch layer）**，不重编译 ReplayMod 源码，而是在运行时用 Mixin/ASM 对官方
`replaymod-1.7.10-2.5.2.jar` 注入补丁，并为 GTNH 补齐运行时依赖（UniMixins / GTNHLib / LWJGL3ify）。

- **不包含任何 ReplayMod / Forge / Minecraft 源码**（见 [`NOTICE.md`](NOTICE.md)）。
- 产物是一个**完整替代**官方 jar 的补丁 jar（内含原版全部类 + 补丁层）：
  **不需要**再放原版 ReplayMod。

## 安装

1. 下载 `Replaymod-2.5.2-GTNH-release1.1.jar`，放入 GTNH 实例的 `mods/`。
2. 确保 `mods/` 里有以下**运行时依赖**（缺一不可）：

   | 依赖 | 最低版本 | 说明 |
   |---|---|---|
   | UniMixins | 0.1.7 | Mixin 0.8.7 运行时 |
   | GTNHLib | 0.6.6 | GTNH 公共库 |
   | LWJGL3ify | 1.5.7（+ forgePatches） | Java 17+ 下的 LWJGL3 兼容层 |

3. **务必移除原版** `replaymod-1.7.10-2.5.2.jar`：
   两者 modid 相同（`replaymod`），同时放会重复加载冲突。
4. 启动游戏，主菜单右侧出现「回放中心」入口；按 ReplayMod 键位（默认）开始/停止录制。

## 导出视频需要 ffmpeg

ReplayMod 导出视频依赖 **ffmpeg**。ReplayMod 会按下面顺序查找（Windows）：

1. `<游戏目录>\ffmpeg\bin\ffmpeg.exe`（**推荐**）
2. 否则递归搜索**整个游戏目录**里的任意 `ffmpeg.exe`
3. 都找不到 → 导出时提示「要渲染视频需要安装 ffmpeg」

**装法（二选一）**：

- **推荐**：把下载的 Windows 版 `ffmpeg.exe` 放到
  `<游戏目录>\ffmpeg\bin\ffmpeg.exe`
  （`<游戏目录>` 即该实例的 `.minecraft` / 版本目录）。
- 或：把 `ffmpeg.exe` 放到游戏目录任意位置。

**下载**：<https://ffmpeg.org/download.html> 或 <https://www.gyan.dev/ffmpeg/builds/>
（需含 `libx264` 的构建；essentials / full 均可）。
本补丁在 **ffmpeg 9.1 full_build** 上实测导出正常。

> 默认导出命令为：
> `-y -f rawvideo -pix_fmt bgra -s <W>x<H> -r <FPS> -i - -an -c:v libx264 -b:v <码率> -pix_fmt yuv420p "<out.mp4>"`
> 该命令在 ffmpeg 7/8/9 上均可用。

## 已知限制（Release 1.1 接受）

- **回放遇到「未注册维度」会被跳过（1.1 已修复崩溃）**：例如 PersonalSpace 的个人维度（维度 180）。
  旧版本会直接抛 `IllegalArgumentException: Could not get provider type ...`；现在会**保留当前世界**并写一行日志
  `[replaycompat] skipped respawn to unregistered dimension N`。代价是那一段维度切换不会真的切过去（画面停在原维度）。
  判断依据是 `DimensionManager.isDimensionRegistered`，不写死任何维度名；GC 等正常注册的维度行为完全不变。
- **其它 mod 的界面会盖住回放时间轴**：打开星系图 / GT 机器界面 / NEI 等时，它们绘制在时间轴之上，
  导致看不到、点不到时间轴；GC 的 HUD 警告（如「氧气装置配置无效」）同理。这是绘制层级问题，**1.1 未修复**。
- **中途开始录制的录像可能缺地形 / 缺玩家条目**：ReplayMod 只在区块/玩家首次到达时记录。
  *建议*：开启 ReplayMod 的 **自动开始录制（AUTO_START_RECORDING）**，从进世界就开始录。
- **回放里「人消失」通常是相机视角问题**（相机贴在第一人称/旁观者身上），不是实体丢失；用自由相机查看。
- 更完整的已知问题见 [`docs/KNOWN_ISSUES.md`](docs/KNOWN_ISSUES.md)。

## 构建（可选，从源码复现 jar）

详见 [`docs/BUILDING.md`](docs/BUILDING.md)。需要：

- **JDK 8**（编译补丁层）、**JDK 25 + Gradle 9.4.1**（用 RFG 产出反编译基座 `recompiled_minecraft-1.7.10.jar`）
- 编译依赖（**不随仓库分发**）：官方 `replaymod-1.7.10-2.5.2.jar`、Forge 1.7.10 dev jar、
  launchwrapper、netty、gson、guava、asm、AE2 等
- 两步：`tools/build.ps1` 编译补丁层 → `tools/apply.ps1` 注入官方 jar

## 许可与致谢

- 本补丁层以 **MIT** 发布（见 [`LICENSE`](LICENSE)）。
- 基于 **ReplayMod**（MIT，原作者 **CrushedPixel**、**johni0702**）；补丁 jar 内保留其 `LICENSE.txt`。
- 第三方组件说明见 [`NOTICE.md`](NOTICE.md)；与上游的精确关系（含目标 jar 的 SHA-256）见 [`UPSTREAM.md`](UPSTREAM.md)。
- 思路致谢：补丁层方案借鉴了 [`replaymod-gtnh-compat`](https://github.com/Luoxueruoling/replaymod-gtnh-compat) 项目的探索，特此感谢。
