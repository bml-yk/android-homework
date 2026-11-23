# 实验七 Service 音乐播放器

应用标题：毕美灵2024110101

这是一个使用 Android `Service`、`MediaPlayer`、`ContentResolver` 和
`Notification` 完成的本地音乐播放应用。Activity 通过绑定服务获取
`Binder`，并显示服务返回的歌曲名、播放状态、当前位置和总时长。

## 功能

- 使用 `ContentResolver` 查询 `MediaStore.Audio.Media` 中的本地音乐。
- 在独立播放线程中完成播放、暂停、停止、上一首、下一首和进度调整。
- Activity 通过 Binder 获取播放状态，每 250 ms 刷新一次 SeekBar。
- 拖动 SeekBar 可调整当前歌曲的播放位置。
- 开始播放后显示前台服务通知，通知中包含歌曲名、播放控制和应用入口。
- Android 13 及以上申请 `READ_MEDIA_AUDIO`，旧版本申请
  `READ_EXTERNAL_STORAGE`。

## 运行

1. 使用 Android Studio 打开本目录。
2. 等待 Gradle 同步完成后运行 `app`。
3. 首次启动允许“音乐和音频”以及“通知”权限。
4. 将 mp3、m4a、wav 等音频放入模拟器或手机的 `Music` 文件夹。
5. 点击“刷新音乐”，再点击歌曲即可播放。

## 核心类

- `MainActivity`：权限处理、音乐列表、界面刷新和服务绑定。
- `AudioRepository`：通过 `ContentResolver` 读取音频元数据。
- `MusicService`：独立播放线程、`MediaPlayer`、播放队列、Binder 和通知。
- `TrackAdapter`：展示本地歌曲列表。

项目已按实验要求把应用标题设置为“毕美灵2024110101”。

## 构建与验证

- `.\gradlew.bat assembleDebug` 构建成功。
- `.\gradlew.bat lintDebug` 无错误、无警告。
- 已在 Android API 37 模拟器上验证列表、播放、暂停、停止、上一首、
  下一首、SeekBar 拖动和通知栏歌曲名。
- 可安装 APK 位于 `docs/Experiment7_ServiceMusicPlayer-debug.apk`。
- 完成版实验报告位于
  `docs/实验七_基于Service的音乐播放应用_完成版.docx`。

## 上传 Gitee

在 Gitee 新建空仓库后，在项目目录执行：

```powershell
git init
git add .
git commit -m "完成实验七 Service 音乐播放应用"
git remote add origin <你的Gitee仓库地址>
git push -u origin master
```

上传完成后，把仓库地址填写到实验报告首页的“Gitee网址”横线处。
