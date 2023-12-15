package ga.sharich.musicplayer

import android.app.Application
import android.content.Context

class App : Application() {
    companion object {
        private lateinit var mContext: Context

        val context: Context
            get() = mContext
    }

    override fun onCreate() {
        super.onCreate()

        mContext = applicationContext
    }
}
