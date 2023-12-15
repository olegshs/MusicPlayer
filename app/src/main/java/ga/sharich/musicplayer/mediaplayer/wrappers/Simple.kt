package ga.sharich.musicplayer.mediaplayer.wrappers

import android.content.Context
import android.net.Uri
import ga.sharich.musicplayer.mediaplayer.Factory
import ga.sharich.musicplayer.mediaplayer.MediaPlayer
import ga.sharich.musicplayer.mediaplayer.Wrapper

class Simple : Wrapper {
    private var player: MediaPlayer? = null

    override val isStarted: Boolean
        get() = player != null

    override val isPlaying: Boolean
        get() = player?.isPlaying ?: false

    override val duration: Long
        get() = player?.duration ?: 0

    override var position: Long
        get() = player?.position ?: 0
        set(value) {
            player?.position = value
        }

    override fun start(context: Context, uri: Uri, position: Long) {
        player?.apply {
            stop()
            release()
        }
        player = Factory.create(context, uri).apply {
            this.position = position
            setOnCompletionListener {
                onCompletionListener.invoke()
            }
            play()
        }
    }

    override fun play() {
        player?.play()
    }

    override fun pause() {
        player?.pause()
    }

    override fun stop() {
        player?.apply {
            stop()
            release()

            player = null
        }
    }

    private var onCompletionListener = {}

    override fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }
}
