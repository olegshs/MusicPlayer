package ga.sharich.musicplayer.mediaplayer.effects

import ga.sharich.musicplayer.mediaplayer.MediaPlayer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FadeOut(private val player: MediaPlayer) {
    private lateinit var job: Job

    fun start(duration: Int, callback: () -> Unit = {}) {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + duration

        cancel()

        val startVolume = player.volume
        if (!player.isPlaying || (startVolume <= 0)) {
            callback.invoke()
            return
        }

        job = GlobalScope.launch {
            do {
                delay(50)

                val time = System.currentTimeMillis()
                val elapsed = time - startTime

                var volume = startVolume - (elapsed.toFloat() / duration * startVolume)
                if (volume < 0) {
                    volume = 0f
                }

                if (player.isPlaying) {
                    player.volume = volume
                }
            } while (time < endTime)

            player.volume = 0f

            callback.invoke()
        }
    }

    fun cancel(callback: (MediaPlayer) -> Unit = {}) {
        if (this::job.isInitialized && job.isActive) {
            job.cancel()
            callback.invoke(player)
        }
    }
}
