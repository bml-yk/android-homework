package cn.edu.sicnucs.bml.servicemusicplayer.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import cn.edu.sicnucs.bml.servicemusicplayer.MainActivity
import cn.edu.sicnucs.bml.servicemusicplayer.R
import cn.edu.sicnucs.bml.servicemusicplayer.data.AudioTrack
import java.util.concurrent.CopyOnWriteArraySet

class MusicService : Service() {

    inner class MusicBinder : Binder() {
        fun setPlaylist(tracks: List<AudioTrack>) = setPlaylistInternal(tracks)
        fun playAt(index: Int) = playbackHandler.post { loadTrack(index, autoPlay = true) }
        fun togglePlayPause() = playbackHandler.post { togglePlayPauseInternal() }
        fun stopPlayback() = playbackHandler.post { stopPlaybackInternal() }
        fun previous() = playbackHandler.post { moveToOffset(-1) }
        fun next() = playbackHandler.post { moveToOffset(1) }
        fun seekTo(positionMs: Long) = playbackHandler.post { seekToInternal(positionMs) }
        fun snapshot(): PlayerSnapshot = currentSnapshot
        fun registerListener(listener: PlayerStateListener) = listeners.add(listener)
        fun unregisterListener(listener: PlayerStateListener) = listeners.remove(listener)
    }

    private val binder = MusicBinder()
    private val listeners = CopyOnWriteArraySet<PlayerStateListener>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var playbackThread: HandlerThread
    private lateinit var playbackHandler: Handler

    private var mediaPlayer: MediaPlayer? = null
    private var playlist: List<AudioTrack> = emptyList()
    private var currentIndex = -1
    private var playbackState = PlaybackState.IDLE
    private var pendingAutoPlay = false
    private var errorMessage: String? = null
    private var notificationActive = false

    @Volatile
    private var currentSnapshot = PlayerSnapshot()

    private val progressTask = object : Runnable {
        override fun run() {
            if (playbackState == PlaybackState.PLAYING) {
                publishSnapshot()
            }
            playbackHandler.postDelayed(this, PROGRESS_INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        playbackThread = HandlerThread("MusicPlaybackThread").apply { start() }
        playbackHandler = Handler(playbackThread.looper)
        playbackHandler.post {
            createPlayer()
            playbackHandler.post(progressTask)
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE -> playbackHandler.post { togglePlayPauseInternal() }
            ACTION_STOP -> playbackHandler.post { stopPlaybackInternal() }
            ACTION_PREVIOUS -> playbackHandler.post { moveToOffset(-1) }
            ACTION_NEXT -> playbackHandler.post { moveToOffset(1) }
        }
        return START_STICKY
    }

    private fun createPlayer() {
        releasePlayerOnly()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build(),
            )
            setOnPreparedListener {
                playbackState = if (pendingAutoPlay) PlaybackState.PLAYING else PlaybackState.PAUSED
                if (pendingAutoPlay) {
                    it.start()
                }
                pendingAutoPlay = false
                errorMessage = null
                publishSnapshot()
            }
            setOnCompletionListener {
                if (playlist.size > 1) {
                    loadTrack((currentIndex + 1) % playlist.size, autoPlay = true)
                } else {
                    playbackState = PlaybackState.COMPLETED
                    publishSnapshot()
                }
            }
            setOnErrorListener { _, what, extra ->
                playbackState = PlaybackState.ERROR
                pendingAutoPlay = false
                errorMessage = "播放失败 (code=$what, extra=$extra)"
                publishSnapshot()
                true
            }
        }
    }

    private fun setPlaylistInternal(tracks: List<AudioTrack>) {
        playbackHandler.post {
            val unchanged = playlist.map { it.uri } == tracks.map { it.uri }
            playlist = tracks.toList()
            if (unchanged) {
                publishSnapshot()
                return@post
            }

            releasePlayerOnly()
            createPlayer()
            currentIndex = if (tracks.isEmpty()) -1 else 0
            playbackState = PlaybackState.IDLE
            pendingAutoPlay = false
            errorMessage = null
            if (tracks.isNotEmpty()) {
                loadTrack(0, autoPlay = false)
            } else {
                publishSnapshot()
            }
        }
    }

    private fun loadTrack(index: Int, autoPlay: Boolean) {
        if (index !in playlist.indices) return
        releasePlayerOnly()
        currentIndex = index
        playbackState = PlaybackState.PREPARING
        pendingAutoPlay = autoPlay
        errorMessage = null
        publishSnapshot()
        createPlayer()

        try {
            mediaPlayer?.setDataSource(this, playlist[index].uri)
            mediaPlayer?.prepareAsync()
        } catch (exception: Exception) {
            playbackState = PlaybackState.ERROR
            errorMessage = exception.message ?: "音频文件无法读取"
            pendingAutoPlay = false
            publishSnapshot()
        }
    }

    private fun togglePlayPauseInternal() {
        if (playlist.isEmpty()) return
        when (playbackState) {
            PlaybackState.PLAYING -> {
                mediaPlayer?.pause()
                playbackState = PlaybackState.PAUSED
                publishSnapshot()
            }

            PlaybackState.PAUSED -> {
                mediaPlayer?.start()
                playbackState = PlaybackState.PLAYING
                publishSnapshot()
            }

            PlaybackState.PREPARING -> {
                pendingAutoPlay = !pendingAutoPlay
                publishSnapshot()
            }

            PlaybackState.IDLE,
            PlaybackState.STOPPED,
            PlaybackState.COMPLETED,
            PlaybackState.ERROR,
            -> loadTrack(if (currentIndex >= 0) currentIndex else 0, autoPlay = true)
        }
    }

    private fun stopPlaybackInternal() {
        pendingAutoPlay = false
        releasePlayerOnly()
        createPlayer()
        playbackState = PlaybackState.STOPPED
        errorMessage = null
        publishSnapshot()
    }

    private fun moveToOffset(offset: Int) {
        if (playlist.isEmpty()) return
        val base = if (currentIndex in playlist.indices) currentIndex else 0
        val target = (base + offset + playlist.size) % playlist.size
        loadTrack(target, autoPlay = true)
    }

    private fun seekToInternal(positionMs: Long) {
        if (playbackState !in setOf(
                PlaybackState.PLAYING,
                PlaybackState.PAUSED,
                PlaybackState.COMPLETED,
            )
        ) {
            return
        }
        val player = mediaPlayer ?: return
        val safePosition = positionMs.coerceIn(0L, player.duration.toLong())
        player.seekTo(safePosition.toInt())
        publishSnapshot()
    }

    private fun publishSnapshot() {
        val track = playlist.getOrNull(currentIndex)
        val position = when (playbackState) {
            PlaybackState.PLAYING, PlaybackState.PAUSED -> {
                runCatching { mediaPlayer?.currentPosition?.toLong() ?: 0L }.getOrDefault(0L)
            }
            else -> 0L
        }
        val duration = track?.duration?.takeIf { it > 0 }
            ?: runCatching { mediaPlayer?.duration?.toLong() ?: 0L }.getOrDefault(0L)
        val snapshot = PlayerSnapshot(
            title = track?.title.orEmpty(),
            artist = track?.artist.orEmpty(),
            position = position,
            duration = duration,
            state = playbackState,
            index = currentIndex,
            total = playlist.size,
            errorMessage = errorMessage,
        )
        currentSnapshot = snapshot
        mainHandler.post {
            listeners.forEach { it.onPlayerStateChanged(snapshot) }
            updateNotification(snapshot)
        }
    }

    private fun updateNotification(snapshot: PlayerSnapshot) {
        if (snapshot.state == PlaybackState.IDLE) {
            if (notificationActive) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                notificationManager.cancel(NOTIFICATION_ID)
                notificationActive = false
            }
            return
        }
        if (snapshot.state == PlaybackState.STOPPED) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            notificationManager.cancel(NOTIFICATION_ID)
            notificationActive = false
            return
        }
        if (
            snapshot.state == PlaybackState.PREPARING &&
            !notificationActive
        ) {
            return
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(snapshot.title.ifBlank { getString(R.string.app_name) })
            .setContentText(notificationText(snapshot))
            .setContentIntent(contentIntent())
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(snapshot.state == PlaybackState.PLAYING)
            .addAction(
                R.drawable.ic_previous,
                getString(R.string.previous),
                serviceAction(ACTION_PREVIOUS, 1),
            )
            .addAction(
                if (snapshot.state == PlaybackState.PLAYING) R.drawable.ic_pause else R.drawable.ic_play,
                if (snapshot.state == PlaybackState.PLAYING) {
                    getString(R.string.pause)
                } else {
                    getString(R.string.play)
                },
                serviceAction(ACTION_TOGGLE, 2),
            )
            .addAction(
                R.drawable.ic_next,
                getString(R.string.next),
                serviceAction(ACTION_NEXT, 3),
            )
            .build()

        if (snapshot.state == PlaybackState.PLAYING) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            notificationActive = true
        } else {
            if (notificationActive) {
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun notificationText(snapshot: PlayerSnapshot): String = when (snapshot.state) {
        PlaybackState.PREPARING -> "正在准备 · ${snapshot.artist}"
        PlaybackState.PLAYING -> "正在播放 · ${snapshot.artist}"
        PlaybackState.PAUSED -> "已暂停 · ${snapshot.artist}"
        PlaybackState.COMPLETED -> "播放完成 · ${snapshot.artist}"
        PlaybackState.ERROR -> snapshot.errorMessage.orEmpty()
        else -> snapshot.artist
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun contentIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun serviceAction(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, MusicService::class.java).setAction(action)
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun releasePlayerOnly() {
        mediaPlayer?.let { player ->
            runCatching {
                player.setOnPreparedListener(null)
                player.setOnCompletionListener(null)
                player.setOnErrorListener(null)
                player.reset()
                player.release()
            }
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        notificationManager.cancel(NOTIFICATION_ID)
        playbackHandler.removeCallbacks(progressTask)
        playbackHandler.post {
            releasePlayerOnly()
            playbackThread.quitSafely()
        }
        super.onDestroy()
    }

    private val notificationManager: NotificationManager
        get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val ACTION_TOGGLE = "cn.edu.sicnucs.bml.servicemusicplayer.TOGGLE"
        const val ACTION_STOP = "cn.edu.sicnucs.bml.servicemusicplayer.STOP"
        const val ACTION_PREVIOUS = "cn.edu.sicnucs.bml.servicemusicplayer.PREVIOUS"
        const val ACTION_NEXT = "cn.edu.sicnucs.bml.servicemusicplayer.NEXT"
        private const val CHANNEL_ID = "service_music_playback"
        private const val NOTIFICATION_ID = 701
        private const val PROGRESS_INTERVAL_MS = 250L
    }
}
