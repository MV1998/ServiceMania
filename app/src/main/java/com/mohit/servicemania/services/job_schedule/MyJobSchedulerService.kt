package com.mohit.servicemania.services.job_schedule

import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.JobIntentService
import kotlinx.coroutines.delay
import kotlin.concurrent.thread


class MyJobInt : JobIntentService() {
    override fun onHandleWork(intent: Intent) {
        TODO("Not yet implemented")
    }

}

class MyJobSchedulerService : JobService() {

    val TAG : String = "MyJobSchedulerService"
    private var isJobCompleted = false
    private var isJobCancelled =  false

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "onStartJob: ")
        Log.d(TAG, "onStartJob: Thread Name : ${Thread.currentThread().name}")

        thread {
            Log.d(TAG, "onStartJob: work has been started...")
            var i = 0
            while(i < 100) {
                if (isJobCancelled) {
                    break
                }
                Log.d(TAG, "onStartJob: started work number $i")
                Thread.sleep(10000)
                Log.d(TAG, "onStartJob: Work $i completed")
                i++
            }
            Log.d(TAG, "onStartJob: work has been completed")
            isJobCompleted = !isJobCancelled
            jobFinished(params, isJobCompleted)
        }

        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        isJobCancelled = true
        Log.d(TAG, "onStopJob: ")
        return true
    }

}