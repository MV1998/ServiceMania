package com.mohit.servicemania.services.started_services.thread_and_handler

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Message
import android.util.Log
import com.mohit.servicemania.services.started_services.SongDownloadDifferentThreadService

class SongDownloadUsingSeparateHandlerAndThreadService : Service() {

    lateinit var downloadThread : DownloadThread

    override fun onCreate() {
        super.onCreate()
        Log.d(
            SongDownloadDifferentThreadService::class.simpleName,
            "onCreate: SongDownloadUsingSeparateHandlerAndThreadService")
        downloadThread = DownloadThread()
        downloadThread.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(
            SongDownloadDifferentThreadService::class.simpleName,
            "onStartCommand: SongDownloadUsingSeparateHandlerAndThreadService")
        val song = intent?.getStringExtra("songName") ?: "unknown song"
        val message = Message.obtain()
        message.obj = song
        downloadThread.downloadHandler?.sendMessage(message)
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(
            SongDownloadDifferentThreadService::class.simpleName,
            "onBind: SongDownloadUsingSeparateHandlerAndThreadService")
        return null
    }

}