# 对原始 mod 的修改总账（release1.2）

> 目标：让 **ReplayMod 1.7.10-2.5.2** 在 **GTNH 2.9.0 – 2.9.2** 上可用。
> 本文只回答一件事：**我们到底改了原始 mod 的什么**。

## 0. 一页速览

| 维度 | 数字 / 事实 |
|---|---|
| 被改的原始 jar | `replaymod-1.7.10-2.5.2.jar`（上游基线共 **6101** 个条目） |
| **删除的条目** | **0** —— 上游内容一个都没删 |
| **新增的条目** | **53** = **52 个 class**（`com/gtnh/replaycompat/**`）+ **1 个 mixin 配置**（`mixins.replaycompat.json`） |
| **改写的上游文件** | **3 个**：`META-INF/MANIFEST.MF`、`mixins.recording.replaymod.json`、`assets/replaymod/lang/zh_CN.lang` |
| 52 个新 class 的构成 | **30** 功能 mixin + **9** util（7 个类 + 2 个内部类）+ **12** ASM transformer + **1** coremod 入口 |
| 当前版本 | **release1.2**：`Replaymod-2.5.2-GTNH-release1.2.jar`，`10884633` B，`SHA256 81523FFD75334E2295CC67AC233EE06153D07BF56807BC9B2121730C2F1EBB41` |

**1.0 → 1.1 只改了补丁层里的 2 个 class，没有新增/删除任何条目**：`NullWorldGuard`（新增 5 个方法）与
`MixinNetHandlerPlayClientNullWorldGuard`（`guard$deferRespawn` 先判「目标维度未注册就跳过」）。

**1.1 → 1.2 只新增 1 个 class**：`MixinJourneymapWorldName`（注入 `journeymap.client.data.WorldData#getWorldName`，
回放期间返回固定世界名，消掉 JourneyMap 的 NPE 刷屏），mixin 配置随之多一项。条目数 6153 → **6154** unique
（6154 → 6155 raw），其余 class 字节不变。

**一句话**：**没有 fork 上游源码重新编译**。做法是——保留上游 jar 的全部内容，用脚本把一层
`com.gtnh.replaycompat` 适配包**注入**进去，并把引导入口（manifest）、上游的一处 mixin 列表、
以及汉化文件改掉。

## 1. jar 结构层面的修改（最要紧的一处）

上游与 release1.2 的 `META-INF/MANIFEST.MF` 对比（逐行，取自 jar）：

| 键 | 上游（原始） | release1.2（我们） | 为什么 |
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
- **回放（1.1）**：`S07` 目标维度未注册时跳过，不再崩（`NullWorldGuard.shouldSkipUnregisteredRespawn`）
- **回放（1.2）**：JourneyMap 回放期 NPE 刷屏消除（`MixinJourneymapWorldName`，注入 `WorldData#getWorldName`）
- **相机**：跨维度保持速度（`ReplayCameraSpeed`）
- **录制**：玩家 spawn 条目、保存线程池守卫
- **编辑**：marker / chunk 坏包容错
- **导出**：非 PBO 抓帧（修白屏）
- **杂项**：AE2+Waila、Et Futurum NPE、握手期 OpenGui 兜底

## 3. 复核方法

见 [`BUILDING.md`](BUILDING.md)。每个 class 都在 jar 的 `com/gtnh/replaycompat/**` 下；
条目数可用 zip 工具核对（**6154** unique / **6155** raw，比上游 6101/6102 各多 53）。
