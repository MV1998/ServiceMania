package com.mohit.servicemania.services.started_services

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log

class SongDownloadHandlerThreadService : Service() {

    private var songDownloadLooper : Looper? = null
    private var songDownloadHandler: SongDownloadHandler? = null

    inner class SongDownloadHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            downloadSong(msg.obj as String, msg.arg1)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(SongDownloadService::class.simpleName, "onCreate: SongDownloadHandlerThreadService")

        HandlerThread("SongDownloadHandlerThread").apply {
            start()
            songDownloadLooper = looper
            songDownloadHandler = SongDownloadHandler(songDownloadLooper!!)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(SongDownloadService::class.simpleName, "onStartCommand: SongDownloadHandlerThreadService")
        val song = intent?.getStringExtra("songName") ?: "unknown song"
        songDownloadHandler?.obtainMessage().also { message: Message? ->
            message?.arg1 = startId
            message?.obj = song
            songDownloadHandler?.sendMessage(message!!)
        }

        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(SongDownloadService::class.simpleName, "onBind: SongDownloadHandlerThreadService")
        return null
    }

    private fun downloadSong(songName : String, startId: Int) {
        Log.d("TAG", "------------------------------------------------")
        Log.d("TAG", "downloadSong: before downloading service $songName.")
        Log.d("TAG", "downloadSong: maxMemory ${Runtime.getRuntime().maxMemory() / 1024 /1024 }")
        Log.d("TAG", "downloadSong: totalMemory ${Runtime.getRuntime().totalMemory() / 1024 / 1024}")
        Log.d("TAG", "downloadSong: UsedMemory ${(Runtime.getRuntime().totalMemory() / 1024 / 1024) -(Runtime.getRuntime().freeMemory() / 1024 / 1024)}")
        Log.d("TAG", "downloadSong: FreeMemory ${Runtime.getRuntime().freeMemory() / 1024 / 1024}")
        try {
            Log.d("TAG", "$songName is being downloaded.")
            Thread.sleep(4000)
            Log.d("TAG", "$songName downloaded.")
            stopSelf(startId)
        }catch (e : Exception) {
            Log.d("TAG", "downloadSong: exception arisen.")
        }
        Log.d("TAG", "downloadSong: after downloading service")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(javaClass.name, "onDestroy: SongDownloadHandlerThreadService")
    }
}