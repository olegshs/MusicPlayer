package ga.sharich.musicplayer.mediaplayer.implementations

import android.content.Context
import android.net.Uri
import ga.sharich.musicplayer.mediaplayer.MediaPlayer
import android.media.MediaPlayer as AndroidMediaPlayer

class Android(context: Context, uri: Uri) : MediaPlayer {
    private val player = AndroidMediaPlayer.create(context, uri)

    override val isPlaying: Boolean
        get() = player.isPlaying

    override val duration: Long
        get() = player.duration.toLong()

    override var position: Long
        get() = player.currentPosition.toLong()
        set(value) = player.seekTo(value.toInt())

    private var mVolume: Float = 1f
    override var volume: Float
        get() = mVolume
        set(value) {
            mVolume = value
            player.setVolume(mVolume, mVolume)
        }

    override fun play() = player.start()

    override fun pause() = player.pause()

    override fun stop() = player.stop()

    override fun release() = player.release()

    override fun setOnCompletionListener(listener: () -> Unit) {
        player.setOnCompletionListener {
            listener.invoke()
        }
    }
}
