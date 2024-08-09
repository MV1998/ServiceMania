package com.mohit.servicemania.services.started_services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log


class SongDownloadDifferentThreadService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.d(SongDownloadDifferentThreadService::class.simpleName, "onCreate: SongDownloadDifferentThreadService")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(SongDownloadDifferentThreadService::class.simpleName, "onStartCommand: SongDownloadDifferentThreadService")
        val song = intent?.getStringExtra("songName") ?: "unknown song"
        val thread = Thread {
            Log.d("TAG", "onStartCommand: ${Thread.currentThread().name}")
            downloadSong(song)
        }
        thread.start()
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(SongDownloadDifferentThreadService::class.simpleName, "onBind: SongDownloadDifferentThreadService")
        return null
    }

    private fun downloadSong(songName : String) {
        Log.d("TAG", "------------------------------------------------")
        Log.d("TAG", "downloadSong: before downloading service $songName.")
        Log.d("TAG", "downloadSong: maxMemory ${Runtime.getRuntime().maxMemory() / 1024 /1024 }")
        Log.d("TAG", "downloadSong: totalMemory ${Runtime.getRuntime().totalMemory() / 1024 / 1024}")
        Log.d("TAG", "downloadSong: UsedMemory ${(Runtime.getRuntime().totalMemory() / 1024 / 1024) -(Runtime.getRuntime().freeMemory() / 1024 / 1024)}")
        Log.d("TAG", "downloadSong: FreeMemory ${Runtime.getRuntime().freeMemory() / 1024 / 1024}")
        try {
            Log.d("TAG", "$songName is being downloaded.")
            Thread.sleep(10000)
            Log.d("TAG", "$songName downloaded.")
        }catch (e : Exception) {
            Log.d("TAG", "downloadSong: exception arisen.")
        }
        Log.d("TAG", "downloadSong: after downloading service")
    }

}