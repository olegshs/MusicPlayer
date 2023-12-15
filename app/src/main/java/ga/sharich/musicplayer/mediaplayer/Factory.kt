package ga.sharich.musicplayer.mediaplayer

import android.content.Context
import android.net.Uri
import ga.sharich.musicplayer.mediaplayer.implementations.Android
import ga.sharich.musicplayer.mediaplayer.implementations.VLC

object Factory {
    const val IMPLEMENTATION_ANDROID = "Android"
    const val IMPLEMENTATION_VLC = "VLC"

    var defaultImplementation = IMPLEMENTATION_VLC

    fun create(context: Context, uri: Uri): MediaPlayer {
        val player = when (defaultImplementation) {
            IMPLEMENTATION_ANDROID -> Android(context, uri)
            IMPLEMENTATION_VLC -> VLC(context, uri)
            else -> throw Exception("Unknown implementation: %s".format(defaultImplementation))
        }

        return SafeMediaPlayer(player)
    }
}
