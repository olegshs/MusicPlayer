package ga.sharich.musicplayer.mediaplayer

import android.content.Context
import android.net.Uri

interface Wrapper {
    val isStarted: Boolean

    val isPlaying: Boolean

    val duration: Long

    var position: Long

    fun start(context: Context, uri: Uri, position: Long = 0)

    fun play()

    fun pause()

    fun stop()

    fun setOnCompletionListener(listener: () -> Unit)
}
