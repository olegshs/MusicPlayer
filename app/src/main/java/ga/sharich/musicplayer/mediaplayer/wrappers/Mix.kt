package ga.sharich.musicplayer.mediaplayer.wrappers

import android.content.Context
import android.net.Uri
import ga.sharich.musicplayer.mediaplayer.Factory
import ga.sharich.musicplayer.mediaplayer.MediaPlayer
import ga.sharich.musicplayer.mediaplayer.Wrapper
import ga.sharich.musicplayer.mediaplayer.effects.FadeIn
import ga.sharich.musicplayer.mediaplayer.effects.FadeOut

class Mix : Wrapper {
    var mixDuration = 500
    var pauseDuration = 250

    private lateinit var context: Context
    private lateinit var uri: Uri

    private var player: MediaPlayer? = null

    private var fadeIn: FadeIn? = null
    private var fadeOut: FadeOut? = null

    override val isStarted: Boolean
        get() = player != null

    private var mIsPlaying = false
    override val isPlaying: Boolean
        get() = mIsPlaying

    override val duration: Long
        get() = player?.duration ?: 0

    override var position: Long
        get() = player?.position ?: 0
        set(value) {
            player?.also {
                if (it.isPlaying) {
                    start(context, uri, value)
                } else {
                    it.position = value
                }
            }
        }

    override fun start(context: Context, uri: Uri, position: Long) {
        this.context = context
        this.uri = uri

        val currentPlayer = player
        if (currentPlayer != null) {
            fadeIn?.apply {
                cancel()
                fadeIn = null
            }
            fadeOut?.apply {
                cancel {
                    it.stop()
                    it.release()
                }
            }
            fadeOut = FadeOut(currentPlayer).apply {
                start(mixDuration) {
                    currentPlayer.stop()
                    currentPlayer.release()
                    fadeOut = null
                }
            }
        }

        val newPlayer = Factory.create(context, uri)
        newPlayer.position = position

        newPlayer.setOnCompletionListener {
            stop()
            onCompletionListener.invoke()
        }

        if ((currentPlayer != null) && (currentPlayer.volume > 0.5)) {
            newPlayer.volume = 0f
            fadeIn = FadeIn(newPlayer).apply {
                start(mixDuration) {
                    fadeIn = null
                }
            }
        } else {
            newPlayer.play()
        }

        player = newPlayer
        mIsPlaying = newPlayer.isPlaying
    }

    override fun play() {
        player?.apply {
            play()
            mIsPlaying = isPlaying
        }
    }

    override fun pause() {
        mIsPlaying = false

        fadeIn?.apply {
            cancel {
                it.volume = 1f
            }
            fadeIn = null
        }
        fadeOut?.apply {
            cancel {
                if (it != player) {
                    it.stop()
                    it.release()
                }
            }
            fadeOut = null
        }
        player?.also {
            fadeOut = FadeOut(it).apply {
                start(pauseDuration) {
                    it.pause()
                    it.volume = 1f
                    fadeOut = null
                }
            }
        }
    }

    override fun stop() {
        mIsPlaying = false

        val currentPlayer = player ?: return
        currentPlayer.stop()
        currentPlayer.release()

        fadeIn?.apply {
            cancel {
                it.stop()
                it.release()
            }
            fadeIn = null
        }
        fadeOut?.apply {
            cancel {
                it.stop()
                it.release()
            }
            fadeOut = null
        }

        player = null
    }

    private var onCompletionListener = {}

    override fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }
}
