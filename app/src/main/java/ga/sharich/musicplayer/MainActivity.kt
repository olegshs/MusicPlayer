package ga.sharich.musicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.TypedValue
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    var seeking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkStoragePermission {
            initTrackListView()
            initPosition()
            initDuration()
            initSeekBar()
            initPlayButton()
            initTrackButtons()
            initLikeButtons()
            initStopButton()
        }
    }

    private fun checkStoragePermission(callback: (isGranted: Boolean) -> Unit) {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE

        ContextCompat.checkSelfPermission(applicationContext, permission).also {
            if (it == PackageManager.PERMISSION_GRANTED) {
                callback(true)
                return@also
            }

            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                callback(isGranted)
            }.launch(permission)
        }
    }

    private fun initTrackListView() {
        val adapter = TrackListAdapter(this, Player.queue)

        val trackListView = findViewById<RecyclerView>(R.id.trackListView)
        trackListView.adapter = adapter
        trackListView.layoutManager = LinearLayoutManager(this)
        attachItemTouchHelperToTrackListView(trackListView, adapter)

        Player.currentTrackLiveData.observe(this, {
            val prevTrackIndex = Player.previousTrackIndex
            if (prevTrackIndex >= 0) {
                adapter.notifyItemChanged(prevTrackIndex)
            }

            adapter.notifyItemChanged(Player.currentTrackIndex)
            trackListView.scrollToPosition(Player.currentTrackIndex)
        })

        Player.isPlayingLiveData.observe(this, {
            if (Player.isPlaying) {
                trackListView.scrollToPosition(Player.currentTrackIndex)
            }
        })
    }

    private fun attachItemTouchHelperToTrackListView(trackListView: RecyclerView, adapter: TrackListAdapter) {
        @ColorInt
        fun themeColor(@AttrRes attrRes: Int): Int =
            TypedValue()
                .also { theme.resolveAttribute(attrRes, it, true) }
                .data

        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
            ItemTouchHelper.START or ItemTouchHelper.END
        ) {
            override fun onMove(
                view: RecyclerView,
                holder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = holder.adapterPosition
                val to = target.adapterPosition

                Player.move(from, to)
                adapter.notifyItemMoved(from, to)

                return true
            }

            override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
                val position = holder.adapterPosition

                Player.remove(position)
                adapter.notifyItemRemoved(position)
            }

            override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
                return defaultValue * 10
            }

            override fun onSelectedChanged(holder: RecyclerView.ViewHolder?, state: Int) {
                super.onSelectedChanged(holder, state)

                if (state == ItemTouchHelper.ACTION_STATE_DRAG) {
                    holder?.itemView?.setBackgroundColor(
                        themeColor(R.attr.colorGrayLight)
                    )
                }
            }

            override fun clearView(view: RecyclerView, holder: RecyclerView.ViewHolder) {
                super.clearView(view, holder)

                holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(applicationContext, android.R.color.transparent)
                )
            }
        }

        ItemTouchHelper(callback).attachToRecyclerView(trackListView)
    }

    private fun initPosition() {
        val positionText = findViewById<TextView>(R.id.positionText)

        Player.positionLiveData.observe(this, { position ->
            if (seeking) {
                return@observe
            }
            positionText.text = Helper.formatDuration(position)
        })
    }

    private fun initDuration() {
        val durationText = findViewById<TextView>(R.id.durationText)

        Player.durationLiveData.observe(this, { duration ->
            durationText.text = Helper.formatDuration(duration)
        })
    }

    private fun initPlayButton() {
        val playButton = findViewById<ImageButton>(R.id.playButton)
        playButton.setOnClickListener {
            if (Player.isPlaying) {
                Player.pause()
            } else {
                Player.play()
            }
        }

        Player.isPlayingLiveData.observe(this, { isPlaying ->
            if (isPlaying) {
                playButton.contentDescription = "Pause"
                playButton.setImageResource(R.drawable.pause)
            } else {
                playButton.contentDescription = "Play"
                playButton.setImageResource(R.drawable.play)
            }
        })
    }

    private fun initTrackButtons() {
        val previousButton = findViewById<ImageButton>(R.id.previousButton)
        previousButton.setOnClickListener {
            Player.previous()
        }

        val nextButton = findViewById<ImageButton>(R.id.nextButton)
        nextButton.setOnClickListener {
            Player.next()
        }
    }

    private fun initStopButton() {
        val stopButton = findViewById<ImageButton>(R.id.stopButton)
        stopButton.setOnClickListener {
            Player.stop()
        }
    }

    private fun initLikeButtons() {
        val likeButton = findViewById<ImageButton>(R.id.likeButton)
        likeButton.setOnClickListener {
            Player.like()
        }
        Player.ratingLiveData.observe(this, { rating: Rating ->
            if (rating == Rating.Liked) {
                likeButton.setImageResource(R.drawable.thumb_up)
            } else {
                likeButton.setImageResource(R.drawable.thumb_up_outline)
            }
        })

        val dislikeButton = findViewById<ImageButton>(R.id.dislikeButton)
        dislikeButton.setOnClickListener {
            Player.dislike()
        }
        Player.ratingLiveData.observe(this, { rating: Rating ->
            if (rating == Rating.Disliked) {
                dislikeButton.setImageResource(R.drawable.thumb_down)
            } else {
                dislikeButton.setImageResource(R.drawable.thumb_down_outline)
            }
        })
    }

    private fun initSeekBar() {
        val positionText = findViewById<TextView>(R.id.positionText)

        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) {
                    return
                }
                positionText.text = Helper.formatDuration(progress)
            }

            override fun onStartTrackingTouch(bar: SeekBar?) {
                seeking = true
            }

            override fun onStopTrackingTouch(bar: SeekBar?) {
                Player.position = seekBar.progress.toLong()
                seeking = false
            }
        })

        Player.durationLiveData.observe(this, { duration ->
            seekBar.max = duration.toInt()
        })

        Player.positionLiveData.observe(this, { position ->
            if (!seeking) {
                seekBar.progress = position.toInt()
            }
        })
    }
}
