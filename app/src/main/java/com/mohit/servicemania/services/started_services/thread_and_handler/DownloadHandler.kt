package com.mohit.servicemania.services.started_services.thread_and_handler

import android.os.Handler
import android.os.Message
import android.util.Log

class DownloadHandler : Handler() {

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        downloadSong(msg.obj as String)
    }

    private fun downloadSong(songName : String) {
        Log.d("TAG", "------------------------------------------------")
        Log.d("TAG", "downloadSong: before downloading service $songName.")
        Log.d("TAG", "downloadSong: maxMemory ${Runtime.getRuntime().maxMemory() / 1024 /1024 }")
        Log.d("TAG", "downloadSong: totalMemory ${Runtime.getRuntime().totalMemory() / 1024 / 1024}")
        Log.d("TAG", "downloadSong: UsedMemory ${(Runtime.getRuntime().totalMemory() / 1024 / 1024) -
                (Runtime.getRuntime().freeMemory() / 1024 / 1024)}")
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