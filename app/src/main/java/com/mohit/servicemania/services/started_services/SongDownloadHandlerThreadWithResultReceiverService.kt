package com.mohit.servicemania.services.started_services

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.ResultReceiver
import android.util.Log

class SongDownloadHandlerThreadWithResultReceiverService : Service() {

    private var songDownloadLooper : Looper? = null
    private var songDownloadHandler: SongDownloadHandler? = null
    private var resultReceiver : ResultReceiver? = null

    inner class SongDownloadHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            downloadSong(msg.obj as String, msg.arg1)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(
            SongDownloadService::class.simpleName,
            "onCreate: SongDownloadHandlerThreadWithResultReceiverService")
        HandlerThread("SongDownloadHandlerThreadWithResultReceiverService").apply {
            start()
            songDownloadLooper = looper
            songDownloadHandler = SongDownloadHandler(songDownloadLooper!!)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(
            SongDownloadService::class.simpleName,
            "onStartCommand: SongDownloadHandlerThreadWithResultReceiverService")
        val song = intent?.getStringExtra("songName") ?: "unknown song"
        resultReceiver = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(Intent.EXTRA_RESULT_RECEIVER, ResultReceiver::class.java)
        }else {
            intent?.getParcelableExtra<ResultReceiver>(Intent.EXTRA_RESULT_RECEIVER)
        }
        songDownloadHandler?.obtainMessage().also { message: Message? ->
            message?.arg1 = startId
            message?.obj = song
            songDownloadHandler?.sendMessage(message!!)
        }
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(
            SongDownloadService::class.simpleName,
            "onBind: SongDownloadHandlerThreadWithResultReceiverService")
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
            val bundle = Bundle()
            bundle.putString("song", songName)
            resultReceiver?.send(12, bundle)
            stopSelf(startId)
        }catch (e : Exception) {
            Log.d("TAG", "downloadSong: exception arisen.")
        }
        Log.d("TAG", "downloadSong: val bundle = Bundle()\n"+
                "        bundle.putString(\"result\", \"success\")\n"+
                "        resultReceiver?.send(11, bundle) after downloading service")
    }

    override fun onDestroy() {
        super.onDestroy()
        val bundle = Bundle()
        bundle.putString("result", "success")
        resultReceiver?.send(11, bundle)
        Log.d(javaClass.name, "onDestroy: SongDownloadHandlerThreadWithResultReceiverService")
    }

}