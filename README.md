# Deadeye（死亡之眼）

一个 Minecraft **Forge 1.20.1** 模组：按住按键进入 RDR2 风格的"死亡之眼"——整个世界进入平滑慢动作，屏幕边缘泛起渐晕、中上方浮现眼睛标志。灵感来自 [TACZ: Plus](https://modrinth.com/mod/tacz-plus) 的 Deadeye 机制（基于其公开页面描述的全新独立实现，未使用任何反编译代码）。

## 功能

- **时间减速**：按住激活键（默认 `X`，松开恢复），全局减慢游戏 tick 速率——所有生物、弹射物、方块更新、玩家移动一致变慢；鼠标视角逐帧响应不受影响，画面借助 partialTick 插值保持丝滑慢动作而非卡顿跳帧
- **屏幕特效**：四周渐晕 + 屏幕中上方的眼睛图标，两者同色、同步淡入淡出（实时计时，不受慢动作影响）
- **改键**：在 控制(Controls) 设置中可改绑任意**键盘或鼠标**按键
- **多人游戏**：服务端权威、全局生效（任意玩家按住即减速，掉线自动恢复）；服务端要求客户端同装本模组，装了本模组的客户端也能正常进入原版服务器（功能静默禁用）
- **完全独立**：不依赖 TACZ / 时之钟；[Cloth Config API](https://modrinth.com/mod/cloth-config) 为可选依赖，装上后获得图形配置界面

## 配置

### 时间流速（`config/deadeye-common.toml`，多人以服务端文件为准）

| 项 | 说明 | 默认 | 范围 |
|---|---|---|---|
| `slowdownRate` | 激活时的时间流速倍率 | `0.5` | 0.1 ~ 1.0 |

三种修改方式：直接编辑 TOML（保存自动重载）；游戏内指令 `/deadeye rate`（查询）、`/deadeye rate 0.3`（设置，需 OP 2 级，正在激活时立即生效）；Cloth Config 界面。

### 视觉特效（`config/deadeye-client.toml`，纯客户端、每位玩家独立）

| 项 | 说明 | 默认 | 范围 |
|---|---|---|---|
| `vignette.color` | 渐晕与眼睛的颜色（RRGGBB 十六进制） | `1A40E6`（蓝） | 任意 6 位十六进制 |
| `vignette.opacity` | 渐晕最大不透明度 | `0.30` | 0.0 ~ 1.0 |
| `vignette.bandWidth` | 渐变带宽度（占屏幕比例） | `0.22` | 0.05 ~ 0.5 |
| `eye.enabled` | 是否显示眼睛图标 | `true` | — |
| `eye.size` | 眼睛大小（GUI 像素） | `24` | 12 ~ 64 |
| `eye.verticalOffset` | 眼睛距顶部高度比例 | `0.12` | 0.0 ~ 0.45 |

装有 Cloth Config 时：**模组列表 → Deadeye → 配置**，含颜色取色框与滑条，保存即时生效。眼睛贴图（`textures/gui/deadeye_eye.png`）为白色+透明通道、运行时按配置颜色染色，可被资源包覆盖；生成器源码见 `tools/EyeTextureGen.java`。

## 实现原理（简述）

与 TACZ: Plus 所依赖的时之钟（TimeStopClock）同样的思路——**直接修改游戏 tick 速率**：服务端每 tick 把 `MinecraftServer.nextTickTime` 后推，使 TPS 精确降为 `20 × 倍率`；客户端同步调整 `Timer.msPerTick`。三个原版字段通过 AccessTransformer 开放，全程无 Mixin。

## 构建

```bash
# 需要 JDK 21（Windows 下示例）
JAVA_HOME="C:/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot" ./gradlew build
# 产物：build/libs/deadeye-1.0.0.jar
```

---

*本模组的分析、设计与代码主要由 Anthropic 的 **Claude（Fable 5）** 编写完成。*
