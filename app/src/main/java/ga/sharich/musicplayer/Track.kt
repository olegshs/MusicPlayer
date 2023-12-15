package ga.sharich.musicplayer

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import java.io.File

class Track(val file: File) {
    companion object {
        private var lastId = 0
    }

    val id: Int = ++lastId

    val meta: TrackMeta by lazy {
        TrackMetaFactory.get(file).also {
            metaLiveData.postValue(it)
        }
    }
    val metaLiveData = MutableLiveData<TrackMeta>()

    val artist: String by lazy {
        meta.artist ?: "Unknown artist"
    }

    val album: String by lazy {
        meta.album ?: "Unknown album"
    }

    val year: Long by lazy {
        Integer.parseInt(meta.year ?: "0").toLong()
    }

    val title: String by lazy {
        meta.title ?: file.name
    }

    val duration: Long by lazy {
        Integer.parseInt(meta.duration ?: "0").toLong()
    }

    val cover: Bitmap? by lazy {
        meta.cover
    }

    var rating: Rating = Rating.Neutral
}
