package com.mohit.servicemania.services.bound_services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mohit.servicemania.MusicApplication
import com.mohit.servicemania.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicPlayerBoundService : Service() {

    private val tag = javaClass.name

    private val _musicState = MutableLiveData<Boolean>()
    val musicState: LiveData<Boolean> = _musicState
    var builder : NotificationCompat.Builder? = null

    private val musicPlayerBinder by lazy {
        MusicPlayerBinder()
    }

    private val mediaPlayer by lazy {
        MediaPlayer.create(this, R.raw.song_bound_service)
            .apply {
                isLooping = false
                //prepareAsync()
            }
    }

    override fun onCreate() {
        super.onCreate()


        Log.d(tag, "onCreate: MusicPlayerBoundService")
        mediaPlayer.setOnCompletionListener {
            Log.d(tag, "onCreate: setOnCompletionListener")
            _musicState.postValue(it.isPlaying)
            stopSelf()
        }
        mediaPlayer.setOnPreparedListener {
            _musicState.postValue(it.isPlaying)
            Log.d(tag, "setOnPreparedListener: ")
        }


    }

    inner class MusicPlayerBinder: Binder() {
        fun getMusicPlayerService(): MusicPlayerBoundService {
            return this@MusicPlayerBoundService
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(tag, "onStartCommand: MusicPlayerBoundService ")

        createNotification()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(tag, "onBind: MusicPlayerBoundService ")
        return musicPlayerBinder
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Log.d(tag, "onRebind:MusicPlayerBoundService ")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(tag, "onUnbind: MusicPlayerBoundService")
        return true
    }

    fun play() {
        mediaPlayer.start()
        Log.d(tag, "play: ")
    }

    fun pause() {
        mediaPlayer.pause()
        Log.d(tag, "pause: ")
    }

    fun stop() {
        mediaPlayer.stop()
        Log.d(tag, "stop: ")
    }

    fun isPlaying() : Boolean {
        return mediaPlayer.isPlaying
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        Log.d(tag, "onDestroy: MusicPlayerBoundService")
    }

    private fun createNotification() {

        builder = NotificationCompat.Builder(this, MusicApplication.channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Music Player")
            .setAutoCancel(true)
            .setContentText("Playing the song in the app terminated state.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setProgress(0, 0, false)
           // .addAction(NotificationCompat.Action(R.drawable.ic_launcher_background, "Play", null))

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val notification = builder?.build()
            startForeground(12, notification)
        }
    }
}