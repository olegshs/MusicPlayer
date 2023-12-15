package ga.sharich.musicplayer.mediaplayer

import ga.sharich.musicplayer.mediaplayer.implementations.Dummy

class SafeMediaPlayer(player: MediaPlayer) : MediaPlayer {
    private var p = player

    override val isPlaying: Boolean
        get() = p.isPlaying

    override val duration: Long
        get() = p.duration

    override var position: Long
        get() = p.position
        set(value) {
            p.position = value
        }

    override var volume: Float
        get() = p.volume
        set(value) {
            p.volume = value
        }

    override fun play() = p.play()

    override fun pause() = p.pause()

    override fun stop() = p.stop()

    override fun release() {
        p.apply {
            p = Dummy()
            release()
        }
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
        p.setOnCompletionListener(listener)
    }
}
