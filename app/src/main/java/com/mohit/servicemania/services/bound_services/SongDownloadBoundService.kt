package com.mohit.servicemania.services.bound_services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SongDownloadBoundService : Service() {

    private val TAG = javaClass.name
    private val mBinder = SongDownloadBinder()

    private var songDownloadBoundHandler : SongDownloadBoundHandler? = null
    private var songDownloadBoundLooper : Looper? = null
    private val _mutableState = MutableLiveData<String>()
    val state : LiveData<String> get() = _mutableState

    inner class SongDownloadBoundHandler(looper : Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            downloadHandler(msg.obj as String)
        }
    }

   inner class SongDownloadBinder : Binder() {
        fun getService(): SongDownloadBoundService {
            return this@SongDownloadBoundService
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: ")
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind: ")
        val handlerThread = HandlerThread("SongDownloadBoundService")
        handlerThread.start()
        songDownloadBoundLooper = handlerThread.looper
        songDownloadBoundHandler = SongDownloadBoundHandler(songDownloadBoundLooper!!)
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind: ")
        return true
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Log.d(TAG, "onRebind: onRebind")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }

    fun downloadSong(song: String) {
        songDownloadBoundHandler?.obtainMessage().also {message: Message? ->
            message?.obj = song
            songDownloadBoundHandler?.sendMessage(message!!)
        }
    }

    private fun downloadHandler(songName : String) {
        try {
            Log.d(TAG, "downloadHandler: songName $songName is being downloaded.")
            Thread.sleep(3000)
            Log.d(TAG, "downloadHandler: songName $songName has been downloaded.")
            Log.d(TAG, "downloadHandler: Can be dispatched to UI using LiveData.")
            _mutableState.postValue(songName)
        }catch (e: Exception) {
            Log.d(TAG, "downloadHandler: ")
        }
    }

}