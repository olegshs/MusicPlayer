package ga.sharich.musicplayer.mediaplayer

interface MediaPlayer {
    val isPlaying: Boolean

    val duration: Long

    var position: Long

    var volume: Float

    fun play()

    fun pause()

    fun stop()

    fun release()

    fun setOnCompletionListener(listener: () -> Unit)
}
