package ga.sharich.musicplayer

import android.os.Environment
import android.webkit.MimeTypeMap
import java.io.File

object Library {
    fun getQueue(): ArrayList<Track> {
        val tracks = ArrayList<Track>()

        val files = getFiles(Environment.getExternalStorageDirectory().toString() + "/Music")
        for (file in files) {
            val track = Track(file)
            tracks.add(track)
        }

        return tracks
    }

    private fun getFiles(dirName: String): ArrayList<File> {
        val filesList = ArrayList<File>()

        val dir = File(dirName)
        if (!dir.exists()) {
            return filesList
        }

        val files = dir.listFiles()
        if (files == null) {
            return filesList
        }

        for (file in files.sorted()) {
            if (file.isDirectory) {
                filesList.addAll(getFiles(file.path))
                continue
            }

            if (isSupportedType(file)) {
                filesList.add(file)
            }
        }

        return filesList
    }

    private fun isSupportedType(file: File): Boolean {
        return getMimeType(file)?.startsWith("audio/") ?: false
    }

    private fun getMimeType(file: File): String? {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase())
    }
}
