package cn.edu.sicnucs.bml.servicemusicplayer

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.SeekBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import cn.edu.sicnucs.bml.servicemusicplayer.data.AudioRepository
import cn.edu.sicnucs.bml.servicemusicplayer.data.AudioTrack
import cn.edu.sicnucs.bml.servicemusicplayer.databinding.ActivityMainBinding
import cn.edu.sicnucs.bml.servicemusicplayer.player.MusicService
import cn.edu.sicnucs.bml.servicemusicplayer.player.PlaybackState
import cn.edu.sicnucs.bml.servicemusicplayer.player.PlayerSnapshot
import cn.edu.sicnucs.bml.servicemusicplayer.player.PlayerStateListener
import cn.edu.sicnucs.bml.servicemusicplayer.ui.TrackAdapter
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var trackAdapter: TrackAdapter
    private var tracks: List<AudioTrack> = emptyList()
    private var musicBinder: MusicService.MusicBinder? = null
    private var isBound = false
    private var isSeeking = false
    private var latestSnapshot = PlayerSnapshot()
    private var pendingNotificationPermission = false

    private val repository by lazy { AudioRepository(contentResolver) }
    private val loadExecutor = Executors.newSingleThreadExecutor()

    private val serviceListener = PlayerStateListener { snapshot ->
        latestSnapshot = snapshot
        renderPlayer(snapshot)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? MusicService.MusicBinder ?: return
            isBound = true
            musicBinder = binder
            binder.registerListener(serviceListener)
            binder.setPlaylist(tracks)
            latestSnapshot = binder.snapshot()
            renderPlayer(latestSnapshot)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            musicBinder = null
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        if (hasAudioPermission()) {
            loadMusicLibrary()
        } else {
            showPermissionRequired()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        pendingNotificationPermission = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupPlayerControls()
        bindMusicService()
        requestPermissionsIfNeeded()
    }

    private fun setupRecyclerView() {
        trackAdapter = TrackAdapter { index ->
            val track = tracks.getOrNull(index) ?: return@TrackAdapter
            prepareStartedService()
            musicBinder?.playAt(index)
            binding.tvStatus.text = getString(R.string.loading_track, track.title)
        }
        binding.rvTracks.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = trackAdapter
            itemAnimator = null
        }
    }

    private fun setupPlayerControls() {
        binding.btnRefresh.setOnClickListener { requestPermissionsIfNeeded(forceReload = true) }
        binding.btnPlay.setOnClickListener {
            prepareStartedService()
            musicBinder?.togglePlayPause()
        }
        binding.btnStop.setOnClickListener { musicBinder?.stopPlayback() }
        binding.btnPrevious.setOnClickListener {
            prepareStartedService()
            musicBinder?.previous()
        }
        binding.btnNext.setOnClickListener {
            prepareStartedService()
            musicBinder?.next()
        }
        binding.seekProgress.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    if (!fromUser) return
                    binding.tvPosition.text = formatDuration(
                        progress.toLong() * latestSnapshot.duration / 1000,
                    )
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    isSeeking = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    isSeeking = false
                    val targetMs = (seekBar?.progress ?: 0) * latestSnapshot.duration / 1000
                    musicBinder?.seekTo(targetMs)
                }
            },
        )
    }

    private fun bindMusicService() {
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun prepareStartedService() {
        if (!isBound) return
        startService(Intent(this, MusicService::class.java))
    }

    private fun requestPermissionsIfNeeded(forceReload: Boolean = false) {
        if (hasAudioPermission()) {
            if (forceReload || tracks.isEmpty()) loadMusicLibrary()
        } else {
            showPermissionRequired()
            val permissions = mutableListOf<String>()
            permissions += audioPermission()
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions += Manifest.permission.POST_NOTIFICATIONS
            }
            permissionLauncher.launch(permissions.toTypedArray())
        }

        if (
            hasAudioPermission() &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED &&
            !pendingNotificationPermission
        ) {
            pendingNotificationPermission = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun loadMusicLibrary() {
        binding.btnRefresh.isEnabled = false
        binding.tvLibraryHint.text = getString(R.string.scanning)
        binding.progressLibrary.visibility = View.VISIBLE

        loadExecutor.execute {
            val result = runCatching { repository.loadAudioTracks() }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                binding.btnRefresh.isEnabled = true
                binding.progressLibrary.visibility = View.GONE
                result.onSuccess { loadedTracks ->
                    tracks = loadedTracks
                    trackAdapter.submitList(tracks)
                    musicBinder?.let {
                        it.setPlaylist(tracks)
                        latestSnapshot = it.snapshot()
                        renderPlayer(latestSnapshot)
                    }
                    updateLibraryState()
                }.onFailure { error ->
                    tracks = emptyList()
                    trackAdapter.submitList(emptyList())
                    updateLibraryState(error.message)
                }
            }
        }
    }

    private fun updateLibraryState(error: String? = null) {
        val count = tracks.size
        binding.btnRefresh.text = getString(R.string.refresh)
        binding.tvLibraryCount.text = getString(R.string.track_count, count)
        binding.tvLibraryHint.text = when {
            error != null -> getString(R.string.read_failed, error)
            count == 0 -> getString(R.string.no_music_help)
            else -> getString(R.string.tap_track_help)
        }
        binding.tvEmpty.visibility = if (count == 0) View.VISIBLE else View.GONE
        binding.rvTracks.visibility = if (count == 0) View.GONE else View.VISIBLE
        binding.emptyTitle.text = if (error == null) {
            getString(R.string.no_music_title)
        } else {
            getString(R.string.read_failed_title)
        }
    }

    private fun showPermissionRequired() {
        binding.tvLibraryCount.text = getString(R.string.permission_short)
        binding.tvLibraryHint.text = getString(R.string.permission_help)
        binding.btnRefresh.text = getString(R.string.grant_access)
        binding.tvStatus.text = getString(R.string.waiting_permission)
        binding.tvEmpty.visibility = View.VISIBLE
        binding.rvTracks.visibility = View.GONE
        binding.emptyTitle.text = getString(R.string.no_permission_title)
    }

    private fun renderPlayer(snapshot: PlayerSnapshot) {
        binding.tvTrackTitle.text = snapshot.title.ifBlank {
            getString(R.string.choose_a_track)
        }
        binding.tvArtist.text = snapshot.artist.ifBlank {
            getString(R.string.player_subtitle)
        }
        binding.tvStatus.text = when (snapshot.state) {
            PlaybackState.IDLE -> getString(R.string.ready_to_play)
            PlaybackState.PREPARING -> getString(R.string.preparing)
            PlaybackState.PLAYING -> getString(R.string.playing)
            PlaybackState.PAUSED -> getString(R.string.paused)
            PlaybackState.STOPPED -> getString(R.string.stopped)
            PlaybackState.COMPLETED -> getString(R.string.completed)
            PlaybackState.ERROR -> snapshot.errorMessage ?: getString(R.string.playback_error)
        }
        binding.tvPosition.text = formatDuration(snapshot.position)
        binding.tvDuration.text = formatDuration(snapshot.duration)
        binding.seekProgress.isEnabled = snapshot.duration > 0
        if (!isSeeking) {
            val progress = if (snapshot.duration > 0) {
                (snapshot.position * 1000 / snapshot.duration).toInt().coerceIn(0, 1000)
            } else {
                0
            }
            binding.seekProgress.progress = progress
        }
        binding.btnPlay.text = if (snapshot.state == PlaybackState.PLAYING) {
            getString(R.string.pause)
        } else {
            getString(R.string.play)
        }
        val hasTracks = snapshot.total > 0
        binding.btnPlay.isEnabled = hasTracks
        binding.btnPrevious.isEnabled = hasTracks
        binding.btnNext.isEnabled = hasTracks
        binding.btnStop.isEnabled = snapshot.state != PlaybackState.IDLE &&
            snapshot.state != PlaybackState.STOPPED
        binding.tvQueuePosition.text = if (snapshot.index >= 0 && hasTracks) {
            getString(R.string.queue_position, snapshot.index + 1, snapshot.total)
        } else {
            getString(R.string.queue_empty)
        }
        trackAdapter.setSelectedIndex(snapshot.index)
    }

    private fun hasAudioPermission(): Boolean = ContextCompat.checkSelfPermission(
        this,
        audioPermission(),
    ) == PackageManager.PERMISSION_GRANTED

    private fun audioPermission(): String = if (
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    ) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private fun formatDuration(durationMs: Long): String {
        val seconds = durationMs.coerceAtLeast(0L) / 1000
        return String.format(Locale.getDefault(), "%d:%02d", seconds / 60, seconds % 60)
    }

    override fun onDestroy() {
        if (isBound) {
            musicBinder?.unregisterListener(serviceListener)
            unbindService(serviceConnection)
        }
        loadExecutor.shutdownNow()
        super.onDestroy()
    }
}
