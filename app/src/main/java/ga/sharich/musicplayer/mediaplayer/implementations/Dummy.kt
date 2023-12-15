package ga.sharich.musicplayer.mediaplayer.implementations

import ga.sharich.musicplayer.mediaplayer.MediaPlayer

class Dummy : MediaPlayer {
    override val isPlaying: Boolean
        get() = false

    override val duration: Long
        get() = 0

    override var position: Long
        get() = 0
        set(value) {}

    override var volume: Float
        get() = 0f
        set(value) {}

    override fun play() {
    }

    override fun pause() {
    }

    override fun stop() {
    }

    override fun release() {
    }

    override fun setOnCompletionListener(listener: () -> Unit) {
    }
}
