# 对原始 mod 的修改总账（release1.0）

> 目标：让 **ReplayMod 1.7.10-2.5.2** 在 **GTNH 2.9.0** 上可用。
> 本文只回答一件事：**我们到底改了原始 mod 的什么**。

## 0. 一页速览

| 维度 | 数字 / 事实 |
|---|---|
| 被改的原始 jar | `replaymod-1.7.10-2.5.2.jar`（上游基线共 **6101** 个条目） |
| **删除的条目** | **0** —— 上游内容一个都没删 |
| **新增的条目** | **52** = **51 个 class**（`com/gtnh/replaycompat/**`）+ **1 个 mixin 配置**（`mixins.replaycompat.json`） |
| **改写的上游文件** | **3 个**：`META-INF/MANIFEST.MF`、`mixins.recording.replaymod.json`、`assets/replaymod/lang/zh_CN.lang` |
| 51 个新 class 的构成 | **29** 功能 mixin + **9** util（7 个类 + 2 个内部类）+ **12** ASM transformer + **1** coremod 入口 |
| 当前版本 | **release1.0**：`Replaymod-2.5.2-GTNH-release1.0.jar`，`10881470` B，`SHA256 C2E7953B7E964AED48F61BFFA9C55BB495CE3B2256F2D5F501E74F12B6F8B208` |

**一句话**：**没有 fork 上游源码重新编译**。做法是——保留上游 jar 的全部内容，用脚本把一层
`com.gtnh.replaycompat` 适配包**注入**进去，并把引导入口（manifest）、上游的一处 mixin 列表、
以及汉化文件改掉。

## 1. jar 结构层面的修改（最要紧的一处）

上游与 release1.0 的 `META-INF/MANIFEST.MF` 对比（逐行，取自 jar）：

| 键 | 上游（原始） | release1.0（我们） | 为什么 |
|---|---|---|---|
| `TweakClass` | `com.replaymod.core.tweaker.ReplayModTweaker` | **`org.spongepowered.asm.launch.MixinTweaker`** | GTNH 用 unimixins + Mixin 0.8.7；上游 tweaker 在 GTNH 上不可用 |
| `FMLCorePlugin` | `com.replaymod.core.LoadingPlugin` | **`com.gtnh.replaycompat.ReplayModTransformers`** | 上游 coremod 的 transformer 在 GTNH + RFB 上会 `ClassCircularityError` |
| `ForceLoadAsMod` | *（无）* | **`true`** | 强制按 mod 加载（GTNH 系 mod 约定） |
| `MixinConfigs` | *（无）* | **一整行新增**（10 个配置，末尾追加 `mixins.replaycompat.json`） | 上游靠自家 tweaker 拉配置；现在显式声明 |
| `TweakOrder` | `0` | *（已移除）* | 与 MixinTweaker 组合后不再需要 |
| `FMLCorePluginContainsFMLMod` | `true` | `true` | 未改 |
| `FMLAT` | `replaymod_at.cfg` | `replaymod_at.cfg` | 未改（上游访问转换器照旧生效） |

## 2. 补丁层做了什么（摘要）

29 个 mixin + 9 个 util + ASM/coremod 入口，覆盖：

- **启动/coremod**：重实现 `GLErrorTransformer` / `GLStateTrackerTransformer`（opt-in）
- **主菜单**：注入「回放中心」入口（Custom Main Menu 文字按钮）
- **输入**：鼠标锁、按键桥、滚轮调速
- **回放**：第一人称/手、加载期 `theWorld == null` 守卫
- **相机**：跨维度保持速度（`ReplayCameraSpeed`）
- **录制**：玩家 spawn 条目、保存线程池守卫
- **编辑**：marker / chunk 坏包容错
- **导出**：非 PBO 抓帧（修白屏）
- **杂项**：AE2+Waila、Et Futurum NPE、握手期 OpenGui 兜底

## 3. 复核方法

见 [`BUILDING.md`](BUILDING.md)。每个 class 都在 jar 的 `com/gtnh/replaycompat/**` 下；
条目数可用 zip 工具核对（**6153** unique / **6154** raw，比上游 6101/6102 各多 52）。
