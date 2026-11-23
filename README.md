# Android 课程作业

本仓库按作业保存完整的 Android Studio 工程，并使用分支记录每次作业的开发与合并过程。

## 作业目录

| 目录 | 对应内容 |
| --- | --- |
| `hw1` | 多语言 Hello World |
| `hw2` | UI 控件代码生成与 MVC |
| `hw3` | 多 Activity、Intent、相机与 StopWatch |
| `hw4` | 多 Activity 翻牌游戏 |
| `hw5` | RecyclerView 三张牌翻牌游戏 |
| `hw6` | 多 Fragment 应用、两张牌与三张牌游戏、持久化存储 |
| `hw7` | 基于 Service、MediaPlayer 和 Notification 的音乐播放应用 |

## 分支用途

| 分支 | 用途 |
| --- | --- |
| `main` | 已验证并合并的稳定版本，也是默认分支 |
| `Dev` | 作业五开发分支，从作业四完成后的 `main` 建立 |
| `Frag` | 作业六开发分支，从 `Dev` 建立 |
| `hw7` | 作业七开发分支，从 `main` 建立，包含完整工程与实验报告 |
| `feat/hw4-photo-card-game` | 作业四历史功能分支，已合并，不再继续开发 |
| `master` | 早期作业三历史分支，已过时，不再用于提交 |

`Dev` 和 `Frag` 会保留以前的作业目录。它们不是互相独立的代码快照，而是在前一阶段基础上继续增加或修改作业，避免合并时误删历史内容。

`main` 只通过 Pull Request 更新。作业五通过 `Dev → main` 合并，作业六在 `Frag` 分支完成并通过测试后再通过 `Frag → main` 合并。

作业七在 `hw7` 分支完成并通过构建、Lint 和模拟器验证，再通过 Pull Request 合并到 `main`。

## 运行工程

每个 `hw*` 目录都是独立的 Android Studio 工程：

1. 在 Android Studio 中打开需要运行的 `hw*` 目录。
2. 等待 Gradle 同步完成。
3. 选择 `app` 运行配置。

例如运行作业五：

```powershell
cd hw5
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

运行作业六的完整验证：

```powershell
cd hw6
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest
```

## 文档

实验报告和辅助材料放在 `docs` 对应作业的子目录中。正式提交时仍按课程平台要求上传报告和项目地址。

各工程只提交源码、资源、Gradle 配置和 Wrapper。`build`、`.gradle`、`.idea`、`local.properties` 以及签名文件均不纳入版本控制。
