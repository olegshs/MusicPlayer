package ga.sharich.musicplayer

import org.videolan.libvlc.LibVLC

object VLC {
    val instance by lazy {
        LibVLC(App.context, ArrayList<String>().apply {
            //add("-vvv")
        })
    }
}
