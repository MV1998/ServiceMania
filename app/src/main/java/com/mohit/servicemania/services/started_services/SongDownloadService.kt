package com.mohit.servicemania.services.started_services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class SongDownloadService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.d(SongDownloadService::class.simpleName, "onCreate: SongDownloadService")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(SongDownloadService::class.simpleName, "onStartCommand: SongDownloadService")
        val song = intent?.getStringExtra("songName") ?: "unknown song"
        downloadSong(song)
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(SongDownloadService::class.simpleName, "onBind: SongDownloadService")
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
            Thread.sleep(4000)
            Log.d("TAG", "$songName downloaded.")
        }catch (e : Exception) {
            Log.d("TAG", "downloadSong: exception arisen.")
        }
        Log.d("TAG", "downloadSong: after downloading service")
    }
}