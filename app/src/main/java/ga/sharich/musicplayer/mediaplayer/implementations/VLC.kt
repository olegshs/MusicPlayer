package ga.sharich.musicplayer.mediaplayer.implementations

import android.content.Context
import android.net.Uri
import ga.sharich.musicplayer.VLC
import ga.sharich.musicplayer.mediaplayer.MediaPlayer
import org.videolan.libvlc.Media
import kotlin.math.roundToInt
import org.videolan.libvlc.MediaPlayer as VlcMediaPlayer

class VLC(context: Context, uri: Uri) : MediaPlayer {
    private val lib = VLC.instance
    private val player = VlcMediaPlayer(lib).apply {
        Media(lib, uri.path).also {
            media = it
            it.release()
        }
    }

    private var mIsPlaying = player.isPlaying
    override val isPlaying: Boolean
        get() = mIsPlaying

    override val duration: Long
        get() = player.media?.duration ?: 0

    private var mPosition: Long = 0
    private var isPositionUpdateNeeded = false
    override var position: Long
        get() = mPosition
        set(value) {
            mPosition = value
            if (player.isSeekable) {
                player.time = value
            } else {
                isPositionUpdateNeeded = true
            }
        }

    override var volume: Float
        get() = player.volume.toFloat() / 100
        set(value) {
            player.volume = (value * 100).roundToInt()
        }

    override fun play() {
        player.play()
        mIsPlaying = true
    }

    override fun pause() {
        player.pause()
        mIsPlaying = false
    }

    override fun stop() {
        player.stop()
        mIsPlaying = false
    }

    override fun release() {
        player.release()
    }

    private var onCompletionListener = {}
    override fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }

    init {
        player.setEventListener {
            when (it.type) {
                VlcMediaPlayer.Event.Playing -> {
                    if (isPositionUpdateNeeded) {
                        isPositionUpdateNeeded = false
                        player.time = mPosition
                    }
                    mIsPlaying = player.isPlaying
                }

                VlcMediaPlayer.Event.TimeChanged -> {
                    mPosition = player.time
                }

                VlcMediaPlayer.Event.Paused,
                VlcMediaPlayer.Event.Stopped -> {
                    mIsPlaying = player.isPlaying
                }

                VlcMediaPlayer.Event.EndReached -> {
                    onCompletionListener.invoke()
                }
            }
        }
    }
}
