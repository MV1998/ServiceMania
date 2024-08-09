package com.mohit.servicemania.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SongBroadcastReceiver(private val onCallBack : (String) -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val result = intent?.getStringExtra("songName")
        onCallBack.invoke(result ?: "Unknown Song")
    }
}