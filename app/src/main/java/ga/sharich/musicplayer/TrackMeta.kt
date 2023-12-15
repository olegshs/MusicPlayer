package ga.sharich.musicplayer

import android.graphics.Bitmap

interface TrackMeta {
    val artist: String?
    val album: String?
    val year: String?
    val title: String?
    val duration: String?
    val cover: Bitmap?
}
