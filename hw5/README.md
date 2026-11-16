# 作业五：RecyclerView 三张牌翻牌游戏

应用标题：毕美灵2024110101。项目使用 Kotlin、XML Views、RecyclerView 和 MVC。

## 功能

- 首页显示并编辑姓名、性别、学号、电话、邮箱、爱好和头像。
- 首页工具栏菜单提供“关于”对话框。
- 竖屏使用 16 张牌、4 列；横屏使用 24 张牌、6 列。
- 连续翻开三张牌，三张点数都相同或三张花色都相同即匹配成功。
- 每次翻牌扣 1 分，三张匹配时奖励为单牌分值 × 3 × 4，三张不匹配另扣 2 分。
- 支持重新开始、退出、返回、全部匹配与无可匹配提示。
- 保存页面重建时的个人信息、牌局、分数和最高分。

## MVC 结构

- Model：`model/Card`、`model/Deck`、`model/CardMatchingGame`。
- View：`activity_main.xml`、`activity_game.xml`、`item_card.xml` 及个人信息页面。
- Adapter：`CardAdapter` 从 `RecyclerView.Adapter` 派生并处理牌面回调。
- Controller：`MainActivity`、`EditProfileActivity`、`GameActivity`。

## 运行

使用 Android Studio 打开本目录，等待 Gradle 同步，选择 `app` 后运行。项目沿用 Android API 37 和现有依赖版本；`local.properties` 由本机生成，不提交 Git。

## 测试

- `CardGameTest` 覆盖三张同点数、三张同花色、三张不匹配、盖回、重置、牌组与状态恢复。
- `AppFlowTest` 覆盖个人信息保存/取消/重建，以及翻牌、重置和退出流程。

执行单元测试：

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

构建调试 APK：

```powershell
.\gradlew.bat :app:assembleDebug
```
