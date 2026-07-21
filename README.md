# Deadeye（死亡之眼）

一个 Minecraft **Forge 1.20.1** 模组：按住按键进入 RDR2 风格的"死亡之眼"——整个世界进入平滑慢动作，屏幕边缘泛起渐晕、中上方浮现眼睛标志。灵感来自 [TACZ: Plus](https://modrinth.com/mod/tacz-plus) 的 Deadeye 机制（基于其公开页面描述的全新独立实现，未使用任何反编译代码）。

## 功能

- **时间减速**：按住激活键（默认 `X`，松开恢复），全局减慢游戏 tick 速率——所有生物、弹射物、方块更新、玩家移动一致变慢；鼠标视角逐帧响应不受影响，画面借助 partialTick 插值保持丝滑慢动作而非卡顿跳帧
- **能量机制**：每位玩家拥有 0~100% 的死亡之眼能量。满能量默认可持续减速 5 秒（按现实时间计，与减速倍率无关）；耗尽后无法减速；停止使用 2 秒后开始恢复。开启 `infiniteEnergy` 后，服务端会将所有玩家的能量保持在 100%，不会消耗，也不会显示能量数值。能量由**服务端权威**计算并同步给客户端显示，多人游戏中客户端无法越权修改
- **屏幕特效**：四周渐晕 + 屏幕中上方的眼睛图标 + 眼睛旁的能量百分比数值，三者同色；渐晕与眼睛同步淡入淡出（实时计时，不受慢动作影响）；能量低于 100% 时显示数值以便观察恢复进度，达到 100% 后始终隐藏数值
- **改键**：在 控制(Controls) 设置中可改绑任意**键盘或鼠标**按键
- **多人游戏**：服务端权威、全局生效（任意玩家按住即减速，掉线自动恢复）；服务端要求客户端同装本模组，装了本模组的客户端也能正常进入原版服务器（功能静默禁用）
- **完全独立**：不依赖 TACZ / 时之钟；[Cloth Config API](https://modrinth.com/mod/cloth-config) 为可选依赖，装上后获得图形配置界面

## 配置

### 游戏机制（`config/deadeye-common.toml`，多人以服务端文件为准，客户端改本地副本无效）

| 项 | 说明 | 默认 | 范围 |
|---|---|---|---|
| `slowdownRate` | 激活时的时间流速倍率 | `0.25` | 0.1 ~ 1.0 |
| `energyDurationSeconds` | 满能量可持续减速的秒数（现实时间） | `5.0` | 0.5 ~ 600 |
| `infiniteEnergy` | 能量始终保持 100%，不会消耗 | `false` | `true` / `false` |
| `energyRecoveryDelaySeconds` | 停止使用后开始恢复能量的等待秒数 | `2.0` | 0 ~ 600 |
| `energyRecoveryPerTick` | 恢复阶段每 tick 回复的能量百分比 | `2.5` | 0.01 ~ 100 |
| `energySyncRate` | 中间值能量按墙钟时间计算的目标平均同步上限（次/秒）；0%/100% 始终立即同步 | `10` | 1 ~ 20 |

`energySyncRate` 不依赖当前服务器 TPS；1~20 的每个值都有独立的长期平均频率。服务端每 tick 最多发送一个新的中间能量值，因此当实际 TPS 低于配置值时，同步频率以当前 TPS 为物理上限，不会通过重复旧值补包。0% 与 100% 的边界同步不受此限制。

Cloth Config 界面中，持续时间滑条提供整数 1~30 秒；恢复延迟滑条提供 0~10 秒、步进 0.25 秒。TOML 与命令的原有合法范围保持不变。

三种修改方式：直接编辑 TOML（保存自动重载）；游戏内指令（见下）；Cloth Config 界面。

**指令**（查询所有人可用，设置需 OP 2 级；流速设置在激活中立即生效，能量参数每 tick 实时读取）：

```
/deadeye rate [0.1~1.0]            查询/设置时间流速倍率
/deadeye duration [秒]             查询/设置满能量减速时长
/deadeye recoverydelay [秒]        查询/设置能量恢复延迟
/deadeye recoveryrate [%每tick]    查询/设置能量恢复速度
/deadeye syncrate [1~20]           查询/设置能量显示同步速率（次/秒）
/deadeye energy                    查询自己当前的能量
```

### 视觉特效（`config/deadeye-client.toml`，纯客户端、每位玩家独立）

| 项 | 说明 | 默认 | 范围 |
|---|---|---|---|
| `vignette.color` | 渐晕/眼睛/能量数值的颜色（RRGGBB 十六进制） | `1A40E6`（蓝） | 任意 6 位十六进制 |
| `vignette.opacity` | 渐晕最大不透明度 | `0.30` | 0.0 ~ 1.0 |
| `vignette.bandWidth` | 渐变带宽度（占屏幕比例） | `0.22` | 0.05 ~ 0.5 |
| `eye.enabled` | 是否显示眼睛图标 | `true` | — |
| `eye.size` | 眼睛大小（GUI 像素） | `32` | 12 ~ 64 |
| `eye.verticalOffset` | 眼睛距顶部高度比例 | `0.13` | 0.0 ~ 0.45 |
| `energy.showText` | 是否显示能量百分比数值 | `true` | — |
| `energy.textOffsetX` | 能量数值相对眼睛右缘的横向偏移（像素） | `4` | -128 ~ 128 |
| `energy.textOffsetY` | 能量数值相对眼睛竖直中心的纵向偏移（像素） | `1` | -128 ~ 128 |

装有 Cloth Config 时：**模组列表 → Deadeye → 配置**，含颜色取色框与滑条，保存即时生效。眼睛贴图（`textures/gui/deadeye_eye.png`）为白色+透明通道、运行时按配置颜色染色，可被资源包覆盖；生成器源码见 `tools/EyeTextureGen.java`。

## 实现原理（简述）

与 TACZ: Plus 所依赖的时之钟（TimeStopClock）同样的思路——**直接修改游戏 tick 速率**：服务端每 tick 把 `MinecraftServer.nextTickTime` 后推，使 TPS 精确降为 `20 × 倍率`；客户端同步调整 `Timer.msPerTick`。三个原版字段通过 AccessTransformer 开放，全程无 Mixin。

## 构建

**命令行打包**只需 JDK 21；**在 IDEA 里开发**还需要装一个 JDK 17——ForgeGradle 为 1.20.1 准备开发环境（反编译 Minecraft、附加源码）的内部步骤固定使用 Java 17 工具链，与模组本体用 21 编译互不冲突，两个都要在。

缺哪个 JDK 时，Gradle 会尝试自动下载（foojay → Adoptium 的 GitHub Release），**国内网络下经常失败**，报错形如 `NoToolchainAvailableException ... Could not HEAD 'https://github.com/adoptium/temurin17-binaries/...'`。建议直接从清华镜像手动安装，解压到 `%USERPROFILE%\.jdks\` 即可被 Gradle 与 IDEA 自动识别：

- JDK 17：<https://mirrors.tuna.tsinghua.edu.cn/Adoptium/17/jdk/x64/windows/>
- JDK 21：<https://mirrors.tuna.tsinghua.edu.cn/Adoptium/21/jdk/x64/windows/>

```bash
# 命令行构建（Windows 下示例）
JAVA_HOME="C:/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot" ./gradlew build
# 产物：build/libs/deadeye-1.2.1.jar
```

注意事项：

- 首次构建/IDEA 首次 sync 需要下载并处理 Minecraft 本体，**5~15 分钟无输出属正常**，不要中途取消——反复中断会留下半成品缓存，导致后续报 `Start.java: 程序包net.minecraft.client.main不存在` 之类的连锁错误（删除项目下 `build` 目录后完整跑一次即可恢复）。
- IDEA 的 Gradle JVM（设置 → 构建工具 → Gradle）请选 **21**：本项目的 Gradle 8.8 最高只支持在 Java 22 上运行，被 IDEA 默认成更新的 JDK（如 25）会直接同步失败。
- 产物 jar 为 Java 21 字节码：游戏客户端需用 Java 21+ 运行（原版 1.20.1 启动器默认带 Java 17，需在启动器中改 Java 路径）。

---

*本模组的分析、设计与代码主要由 **Claude（Fable 5）** & **Codex（GPT 5.6 Sol）** 编写完成。*
