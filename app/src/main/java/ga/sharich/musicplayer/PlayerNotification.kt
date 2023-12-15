package ga.sharich.musicplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object PlayerNotification {
    fun create(): Notification {
        val context = App.context

        val intentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }

        val pendingIntent: PendingIntent =
            Intent(context, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(context, 0, notificationIntent, intentFlags)
            }

        val channelId = createNotificationChannel("ga.sharich.musicplayer", "Music Player")

        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setShowActionsInCompactView(0, 2, 3)
            .setShowCancelButton(true)
            .setCancelButtonIntent(actionPendingIntent(PlayerReceiver.ActionStop))

        return NotificationCompat.Builder(context, channelId)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.music)
            .setLargeIcon(Player.currentTrack?.cover)
            .setContentTitle(Player.title)
            .setContentText(Player.artist)
            .setContentIntent(pendingIntent)
            .addAction(
                NotificationCompat.Action(
                    if (Player.rating == Rating.Liked) {
                        R.drawable.thumb_up
                    } else {
                        R.drawable.thumb_up_outline
                    },
                    "Like",
                    actionPendingIntent(PlayerReceiver.ActionLike)
                )
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.skip_previous,
                    "Previous",
                    actionPendingIntent(PlayerReceiver.ActionPrevious)
                )
            )
            .addAction(
                if (Player.isPlaying) {
                    NotificationCompat.Action(
                        R.drawable.pause,
                        "Pause",
                        actionPendingIntent(PlayerReceiver.ActionPlay)
                    )
                } else {
                    NotificationCompat.Action(
                        R.drawable.play,
                        "Play",
                        actionPendingIntent(PlayerReceiver.ActionPlay)
                    )
                }
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.skip_next,
                    "Next",
                    actionPendingIntent(PlayerReceiver.ActionNext)
                )
            )
            .addAction(
                NotificationCompat.Action(
                    if (Player.rating == Rating.Disliked) {
                        R.drawable.thumb_down
                    } else {
                        R.drawable.thumb_down_outline
                    },
                    "Dislike",
                    actionPendingIntent(PlayerReceiver.ActionDislike)
                )
            )
            .setStyle(mediaStyle)
            .build()
    }

    private fun createNotificationChannel(channelId: String, channelName: String): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return channelId
        }

        val context = App.context

        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        return channelId
    }

    private fun actionPendingIntent(actionName: String): PendingIntent {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }

        val context = App.context
        val intent = Intent(context, PlayerReceiver::class.java).apply {
            action = actionName
        }

        return PendingIntent.getBroadcast(context, 0, intent, flags)
    }
}
