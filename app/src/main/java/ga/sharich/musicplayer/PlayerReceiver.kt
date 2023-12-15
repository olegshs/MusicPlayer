package ga.sharich.musicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PlayerReceiver : BroadcastReceiver() {
    companion object {
        const val ActionPlay = "ga.sharich.musicplayer.actions.play"
        const val ActionPrevious = "ga.sharich.musicplayer.actions.previous"
        const val ActionNext = "ga.sharich.musicplayer.actions.next"
        const val ActionStop = "ga.sharich.musicplayer.actions.stop"
        const val ActionLike = "ga.sharich.musicplayer.actions.like"
        const val ActionDislike = "ga.sharich.musicplayer.actions.dislike"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ActionPlay -> {
                if (Player.isPlaying) {
                    Player.pause()
                } else {
                    Player.play()
                }
            }

            ActionPrevious -> {
                Player.previous()
            }

            ActionNext -> {
                Player.next()
            }

            ActionStop -> {
                Player.stop()
            }

            ActionLike -> {
                Player.like()
            }

            ActionDislike -> {
                Player.dislike()
            }
        }
    }
}
