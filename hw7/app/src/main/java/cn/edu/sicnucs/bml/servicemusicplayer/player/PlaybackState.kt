package cn.edu.sicnucs.bml.servicemusicplayer.player

enum class PlaybackState {
    IDLE,
    PREPARING,
    PLAYING,
    PAUSED,
    STOPPED,
    COMPLETED,
    ERROR,
}

data class PlayerSnapshot(
    val title: String = "",
    val artist: String = "",
    val position: Long = 0L,
    val duration: Long = 0L,
    val state: PlaybackState = PlaybackState.IDLE,
    val index: Int = -1,
    val total: Int = 0,
    val errorMessage: String? = null,
)

fun interface PlayerStateListener {
    fun onPlayerStateChanged(snapshot: PlayerSnapshot)
}
