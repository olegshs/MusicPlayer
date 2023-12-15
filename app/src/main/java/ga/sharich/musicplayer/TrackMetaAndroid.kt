package ga.sharich.musicplayer

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import java.io.File

class TrackMetaAndroid(private val file: File) : TrackMeta {
    private val meta = MediaMetadataRetriever().apply {
        setDataSource(file.path)
    }

    override val artist = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
    override val album = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
    override val year = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)
    override val title = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
    override val duration = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
    override val cover = meta.embeddedPicture?.let {
        BitmapFactory.decodeByteArray(it, 0, it.size)
    }
}
