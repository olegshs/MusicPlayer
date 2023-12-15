package ga.sharich.musicplayer

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class PlayerService : Service() {
    private val binder by lazy { PlayerBinder() }

    inner class PlayerBinder : Binder() {
        fun getService(): PlayerService = this@PlayerService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, PlayerNotification.create())
        return super.onStartCommand(intent, flags, startId)
    }
}
