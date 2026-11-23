# 作业六：多 Fragment 翻牌游戏

应用标题：毕美灵2024110101。项目使用 Kotlin、XML Views、RecyclerView、Fragment 和序列化文件存储。

## 功能

- `MainActivity` 通过 `BottomNavigationView` 在三个 Fragment 之间切换。
- 个人信息 Fragment 支持姓名、学号、电话、邮箱、性别、爱好和头像编辑。
- 两张牌游戏：两张牌点数相同或花色相同即匹配。
- 三张牌游戏：三张牌点数都相同或三张花色都相同即匹配。
- 两个游戏都支持重新开始、自动计分、最高分和全部匹配提示。
- 个人信息、头像、两种牌局、得分和最高分退出程序后仍能恢复。

## Fragment 结构

- `ProfileFragment`：个人信息与最高分展示。
- `TwoCardGameFragment`：两张牌翻牌游戏。
- `ThreeCardGameFragment`：三张牌翻牌游戏。
- `BaseGameFragment`：两个游戏 Fragment 共用的 RecyclerView 控制器。
- `MainActivity`：负责底部导航和 Fragment 切换。

## 持久化

`HistoryStore` 使用：

- `openFileOutput`、`openFileInput`
- `ObjectOutputStream`、`ObjectInputStream`

`AppState`、`ProfileData`、`Card` 和 `CardMatchingGame` 均实现 `Serializable`。头像会复制到应用内部文件，避免选择器临时 URI 在重启后失效。

## 运行

使用 Android Studio 打开本目录，等待 Gradle 同步，选择 `app` 后运行。`local.properties` 由本机生成，不提交 Git。

## 测试

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
.\gradlew.bat :app:assembleDebugAndroidTest
```

`CardGameTest` 覆盖两张牌和三张牌规则、计分、重置、随机牌组以及完整状态序列化。`AppFlowTest` 覆盖个人信息持久化、两张牌与三张牌切换、翻牌、重置和 Activity 重建恢复。
