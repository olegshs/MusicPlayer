package ga.sharich.musicplayer

import java.io.File

object TrackMetaFactory {
    fun get(file: File): TrackMeta {
        return TrackMetaVLC(file)
    }
}
