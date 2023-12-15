package ga.sharich.musicplayer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import org.videolan.libvlc.Media
import org.videolan.libvlc.interfaces.IMedia
import java.io.File

class TrackMetaVLC(private val file: File) : TrackMeta {
    private val media = Media(VLC.instance, file.path).apply {
        parse()
    }

    override val artist: String? = media.getMeta(IMedia.Meta.Artist)
    override val album: String? = media.getMeta(IMedia.Meta.Album)
    override val year: String? = media.getMeta(IMedia.Meta.Date)
    override val title: String? = media.getMeta(IMedia.Meta.Title)
    override val duration: String = media.duration.toString()
    override val cover: Bitmap? = media.getMeta(IMedia.Meta.ArtworkURL)?.let {
        BitmapFactory.decodeFile(Uri.parse(it).path)
    }

    init {
        media.release()
    }
}
