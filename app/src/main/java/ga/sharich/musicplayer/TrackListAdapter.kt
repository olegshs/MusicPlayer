package ga.sharich.musicplayer

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TrackListAdapter(private val owner: LifecycleOwner, private val tracks: List<Track>) :
    RecyclerView.Adapter<TrackListAdapter.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_DEFAULT = 0
        const val VIEW_TYPE_CURRENT = 1
    }

    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var trackId: Int = 0

        val layout: FrameLayout = view.findViewById(R.id.layout)
        val infoLayout: LinearLayout = view.findViewById(R.id.infoLayout)
        val titleText: TextView = view.findViewById(R.id.titleText)
        val artistText: TextView = view.findViewById(R.id.artistText)
        val durationText: TextView = view.findViewById(R.id.durationText)
        val coverImage: ImageView = view.findViewById(R.id.coverImage)
    }

    class ViewHolderDefault(view: View) : ViewHolder(view) {
    }

    class ViewHolderCurrent(view: View) : ViewHolder(view) {
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == Player.currentTrackIndex) {
            VIEW_TYPE_CURRENT
        } else {
            VIEW_TYPE_DEFAULT
        }
    }

    override fun onCreateViewHolder(group: ViewGroup, type: Int): ViewHolder {
        return when (type) {
            VIEW_TYPE_CURRENT -> {
                val view = LayoutInflater.from(group.context)
                    .inflate(R.layout.track_list_current_item, group, false)

                ViewHolderCurrent(view)
            }

            else -> {
                val view = LayoutInflater.from(group.context)
                    .inflate(R.layout.track_list_item, group, false)

                ViewHolderDefault(view)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val track = tracks[position]

        holder.trackId = track.id

        if (track.metaLiveData.value != null) {
            updateHolder(holder, track)
        } else {
            holder.trackId = track.id
            holder.titleText.text = track.file.nameWithoutExtension
            holder.infoLayout.visibility = View.INVISIBLE
            holder.artistText.text = ""
            holder.durationText.text = ""
            setCover(holder, R.drawable.music)

            val observer = object : Observer<TrackMeta> {
                override fun onChanged(t: TrackMeta) {
                    updateHolder(holder, track)
                    track.metaLiveData.removeObserver(this)
                }
            }
            track.metaLiveData.observe(owner, observer)

            GlobalScope.launch {
                track.meta
            }
        }

        holder.layout.setOnClickListener {
            Player.goto(holder.adapterPosition)
        }
    }

    private fun updateHolder(holder: ViewHolder, track: Track) {
        if (holder.trackId != track.id) {
            return
        }

        holder.titleText.text = track.title
        holder.artistText.text = track.artist
        holder.durationText.text = Helper.formatDuration(track.duration)
        holder.infoLayout.visibility = View.VISIBLE

        val cover = track.cover
        if (cover != null) {
            setCover(holder, cover)
        } else {
            setCover(holder, R.drawable.music)
        }
    }

    private fun setCover(holder: ViewHolder, cover: Bitmap) {
        holder.coverImage.setImageBitmap(cover)
        holder.coverImage.alpha = 1f
    }

    private fun setCover(holder: ViewHolder, @DrawableRes resId: Int) {
        holder.coverImage.setImageResource(resId)
        holder.coverImage.alpha = 0.2f
    }

    override fun getItemCount() = tracks.size
}
