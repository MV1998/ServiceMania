package com.mohit.servicemania.services.work_manager

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class MyWorker(context: Context, val workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {

        Log.d("TAG", "doWork: ${Thread.currentThread().name} ${workerParams.tags}")
        var i = 0
        while (i < 100) {
            Log.d("TAG", "doWork: Song $i is being downloaded")
            Thread.sleep(4000)
            Log.d("TAG", "doWork: Song $i has been Download")
            i++
        }

        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        Log.d("TAG", "onStopped: work manager has been stopped")
    }
}