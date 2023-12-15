package ga.sharich.musicplayer

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.view.WindowManager
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ga.sharich.musicplayer.mediaplayer.Factory
import ga.sharich.musicplayer.mediaplayer.Wrapper
import ga.sharich.musicplayer.mediaplayer.wrappers.Mix
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

object Player {
    init {
        Factory.defaultImplementation = Factory.IMPLEMENTATION_VLC
    }

    private val player: Wrapper = Mix().apply {
        mixDuration = 1000
        pauseDuration = 500
    }

    val queue: ArrayList<Track> by lazy { Library.getQueue() }

    private var mCurrentTrackIndex = if (queue.isNotEmpty()) {
        0
    } else {
        -1
    }
    val currentTrackIndex: Int
        get() = mCurrentTrackIndex

    private var mPreviousTrackIndex = -1
    val previousTrackIndex: Int
        get() = mPreviousTrackIndex

    val currentTrack: Track?
        get() {
            if (mCurrentTrackIndex < 0) {
                return null
            }
            return queue[mCurrentTrackIndex]
        }

    val isPlaying: Boolean
        get() = player.isPlaying

    val duration: Long
        get() = currentTrack?.duration ?: 0

    private var mPosition: Long = 0
    var position: Long
        get() = player.position
        set(value) {
            player.position = value
            mPosition = value
            mPositionLiveData.value = value
        }

    var rating: Rating
        get() = currentTrack?.rating ?: Rating.Neutral
        set(value) {
            val track = currentTrack ?: return

            track.rating = value
            mRatingLiveData.value = value
        }

    private val mCurrentTrackLiveData = MutableLiveData(currentTrack)
    val currentTrackLiveData: LiveData<Track?>
        get() = mCurrentTrackLiveData

    private val mIsPlayingLiveData = MutableLiveData(isPlaying)
    val isPlayingLiveData: LiveData<Boolean>
        get() = mIsPlayingLiveData

    private val mDurationLiveData = MutableLiveData(duration)
    val durationLiveData: LiveData<Long>
        get() = mDurationLiveData

    private val mPositionLiveData = MutableLiveData(position)
    val positionLiveData: LiveData<Long>
        get() = mPositionLiveData

    private val mRatingLiveData = MutableLiveData(rating)
    val ratingLiveData: LiveData<Rating>
        get() = mRatingLiveData

    val artist: String
        get() = currentTrack?.artist ?: ""

    val album: String
        get() = currentTrack?.album ?: ""

    val title: String
        get() = currentTrack?.title ?: ""

    fun play() {
        if (isPlaying) {
            return
        }

        val track = currentTrack ?: return

        if (player.isStarted) {
            player.play()
        } else {
            player.start(App.context, Uri.parse(track.file.path), mPosition)
            player.setOnCompletionListener {
                next()
            }
        }

        mIsPlayingLiveData.value = isPlaying

        startPositionUpdate()

        GlobalScope.launch {
            cancelServiceAutoStop()
            startService()
            updateNotification()
            setWallpaper()
        }
    }

    fun pause() {
        if (!isPlaying) {
            return
        }

        player.pause()

        stopPositionUpdate()

        mIsPlayingLiveData.value = isPlaying

        GlobalScope.launch {
            updateNotification()
            restoreWallpaper()
            enableServiceAutoStop()
        }
    }

    fun previous(force: Boolean = false) {
        if (!force && isPlaying && (position > 5000)) {
            position = 0
            return
        }

        goto(mCurrentTrackIndex - 1)
    }

    fun next() {
        goto(mCurrentTrackIndex + 1)
    }

    fun goto(index: Int, start: Boolean = true, force: Boolean = false) {
        if (!force && (index == currentTrackIndex)) {
            return
        }

        stopPositionUpdate()

        val lastTrackIndex = queue.size - 1
        if (lastTrackIndex < 0) {
            return
        }

        var newIndex = index
        if (newIndex < 0) {
            newIndex = lastTrackIndex
        }
        if (newIndex > lastTrackIndex) {
            newIndex = 0
        }

        mPreviousTrackIndex = mCurrentTrackIndex
        mCurrentTrackIndex = newIndex

        val track = currentTrack ?: return

        if (start) {
            player.start(App.context, Uri.parse(track.file.path))
            player.setOnCompletionListener {
                next()
            }
        }

        mCurrentTrackLiveData.value = track
        mIsPlayingLiveData.value = isPlaying
        mDurationLiveData.value = duration
        mPositionLiveData.value = position
        mRatingLiveData.value = track.rating

        startPositionUpdate()

        GlobalScope.launch {
            cancelServiceAutoStop()
            startService()
            updateNotification()
            setWallpaper()
        }
    }

    fun stop() {
        stopPositionUpdate()

        GlobalScope.launch {
            cancelServiceAutoStop()
            stopService()
            restoreWallpaper()
        }

        player.stop()

        position = 0
        mIsPlayingLiveData.value = isPlaying
    }

    fun like() {
        if (currentTrack == null) {
            return
        }

        rating = when (rating) {
            Rating.Neutral -> Rating.Liked
            Rating.Liked -> Rating.Neutral
            Rating.Disliked -> Rating.Liked
        }

        GlobalScope.launch {
            updateNotification()
        }
    }

    fun dislike(skip: Boolean = true) {
        if (currentTrack == null) {
            return
        }

        rating = when (rating) {
            Rating.Neutral -> Rating.Disliked
            Rating.Liked -> Rating.Disliked
            Rating.Disliked -> Rating.Neutral
        }

        if (skip && (rating == Rating.Disliked)) {
            next()
            return
        }

        GlobalScope.launch {
            updateNotification()
        }
    }

    fun move(from: Int, to: Int) {
        if (!isValidIndex(from) || !isValidIndex(to)) {
            return
        }

        if (from == mCurrentTrackIndex) {
            mCurrentTrackIndex = to
        } else if ((from < mCurrentTrackIndex) && (to >= mCurrentTrackIndex)) {
            mCurrentTrackIndex--
        } else if ((from > mCurrentTrackIndex) && (to <= mCurrentTrackIndex)) {
            mCurrentTrackIndex++
        }

        queue[to] = queue[from].also { queue[from] = queue[to] }
    }

    fun remove(index: Int) {
        if (!isValidIndex(index)) {
            return
        }

        queue.removeAt(index)

        if (index < mCurrentTrackIndex) {
            mCurrentTrackIndex--
        } else if (queue.isEmpty()) {
            mCurrentTrackIndex = -1
            mCurrentTrackLiveData.value = null
            mDurationLiveData.value = 0
            mRatingLiveData.value = Rating.Neutral
            stop()
        } else if (index == mCurrentTrackIndex) {
            if (isPlaying) {
                goto(mCurrentTrackIndex, start = true, force = true)
            } else {
                if (index == queue.size) {
                    mCurrentTrackIndex--
                }
                goto(mCurrentTrackIndex, start = false, force = true)
            }
        }
    }

    private fun isValidIndex(index: Int): Boolean = (index in 0 until queue.size)

    private fun updateNotification() {
        NotificationManagerCompat.from(App.context).notify(1, PlayerNotification.create())
    }

    private lateinit var positionUpdate: Job
    private const val positionUpdateDelay = 100L

    private fun startPositionUpdate() {
        stopPositionUpdate()

        positionUpdate = GlobalScope.launch {
            while (true) {
                mPositionLiveData.postValue(position)
                delay(positionUpdateDelay)
            }
        }
    }

    private fun stopPositionUpdate() {
        if (this::positionUpdate.isInitialized && positionUpdate.isActive) {
            positionUpdate.cancel()
        }
    }

    private var isServiceStarted = false

    private fun startService() {
        if (isServiceStarted) {
            return
        }

        val (context, intent) = getContextAndIntent()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        isServiceStarted = true
    }

    private fun stopService() {
        if (!isServiceStarted) {
            return
        }

        val (context, intent) = getContextAndIntent()
        context.stopService(intent)

        isServiceStarted = false
    }

    private lateinit var serviceAutoStop: Job
    private const val serviceAutoStopMinutes = 30

    private fun enableServiceAutoStop() {
        cancelServiceAutoStop()

        serviceAutoStop = GlobalScope.launch {
            delay(serviceAutoStopMinutes * 60_000L)
            stopService()
        }
    }

    private fun cancelServiceAutoStop() {
        if (this::serviceAutoStop.isInitialized && serviceAutoStop.isActive) {
            serviceAutoStop.cancel()
        }
    }

    private fun getContextAndIntent(): Pair<Context, Intent> {
        val context = App.context
        val intent = Intent(context, PlayerService::class.java)

        return Pair(context, intent)
    }

    private fun setWallpaper() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return
        }

        val cover = currentTrack?.cover ?: return

        val screenSize = Point().also {
            (App.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .defaultDisplay.getRealSize(it)
        }

        val screenWidth = screenSize.x.toFloat()
        val screenHeight = screenSize.y.toFloat()
        val orientation = Resources.getSystem().configuration.orientation
        val screenRatio = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            screenWidth / screenHeight
        } else {
            screenHeight / screenWidth
        }

        val coverWidth = cover.width.toFloat()
        val coverHeight = cover.height.toFloat()
        val coverRatio = coverWidth / coverHeight

        val rect = if (coverRatio >= screenRatio) {
            val offset = (coverWidth - coverWidth * screenRatio) / 2
            Rect(offset.roundToInt(), 0, (coverWidth - offset).roundToInt(), cover.height)
        } else {
            val offset = (coverHeight - coverHeight * screenRatio) / 2
            Rect(0, offset.roundToInt(), cover.width, (coverHeight - offset).roundToInt())
        }

        WallpaperManager.getInstance(App.context).run {
            setBitmap(cover, rect, false, WallpaperManager.FLAG_LOCK)
        }
    }

    private fun restoreWallpaper() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return
        }

        WallpaperManager.getInstance(App.context).run {
            clear(WallpaperManager.FLAG_LOCK)
        }
    }
}
