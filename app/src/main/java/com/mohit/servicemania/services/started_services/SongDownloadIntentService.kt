package com.mohit.servicemania.services.started_services

import android.app.IntentService
import android.content.Intent
import android.util.Log


// In android 8.0 has background work limitation, so using this approach might be giving problem and issues.

class SongDownloadIntentService : IntentService("SongDownloadIntentService") {

    val TAG = javaClass.name
    init {
        setIntentRedelivery(true)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: SongDownloadIntentService ")
    }

    override fun onHandleIntent(intent: Intent?) {
        val song = intent?.getStringExtra("songName") ?: "Unknown Song"
        Log.d(javaClass.name, "onHandleIntent: $song")
        downloadSong(song)
    }


    private fun downloadSong(songName : String) {
        Log.d("TAG", "--------------------${Thread.currentThread().name}-------------------------")
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

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: SongDownloadIntentService")
    }

}