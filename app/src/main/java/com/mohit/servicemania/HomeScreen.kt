package com.mohit.servicemania

import android.Manifest
import android.app.Service
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.ResultReceiver
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.mohit.servicemania.broadcast_receivers.TImeTickBroadcastReceiver
import com.mohit.servicemania.services.bound_services.MusicPlayerBoundService
import com.mohit.servicemania.services.bound_services.SongDownloadBoundService
import com.mohit.servicemania.services.job_schedule.MyJobSchedulerService
import com.mohit.servicemania.services.started_services.SongDownloadDifferentThreadService
import com.mohit.servicemania.services.started_services.SongDownloadHandlerThreadService
import com.mohit.servicemania.services.started_services.SongDownloadHandlerThreadWithBroadcastReceiverService
import com.mohit.servicemania.services.started_services.SongDownloadHandlerThreadWithResultReceiverService
import com.mohit.servicemania.services.started_services.SongDownloadIntentService
import com.mohit.servicemania.services.started_services.SongDownloadService
import com.mohit.servicemania.services.started_services.thread_and_handler.SongDownloadUsingSeparateHandlerAndThreadService
import com.mohit.servicemania.services.work_manager.MyWorker
import java.util.concurrent.TimeUnit

val JOB_ID = 10012

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SystemBroadcastReceiver(systemAction : String,
                            onSystemEvent: (intent : Intent?) -> Unit) {
    val context = LocalContext.current

    val currentOnSystemEvent by rememberUpdatedState(onSystemEvent)

    DisposableEffect(key1 = Unit) {

        val receiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                currentOnSystemEvent(intent)
            }
        }

        Log.d(javaClass.name, "HomeScreen: Registered")
        context.registerReceiver(receiver, IntentFilter(systemAction),
            Context.RECEIVER_NOT_EXPORTED)
        onDispose {
            Log.d(javaClass.name, "HomeScreen: Disposed")
            context.unregisterReceiver(receiver)
        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(modifier: Modifier) {

    val tag = "HomeScreen"
    val scrollState = rememberScrollState()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showDialog by remember { mutableStateOf(false) }
    var showDownloadCompleteDialog by remember {
        mutableStateOf(false)
    }

    val tImeTickBroadcastReceiver = TImeTickBroadcastReceiver()

    var songName by remember {
        mutableStateOf("Song are being downloaded.")
    }

    var workManager : WorkManager? = null
    var workRequest : OneTimeWorkRequest? = null

//    val receiver = remember {
//        SongBroadcastReceiver { currentSongName ->
//            Log.d("TAG", "HomeScreen: CurrentSong $currentSongName")
//            songName = currentSongName
//        }
//    }
//    DisposableEffect(key1 = Unit) {
//
//        Log.d(javaClass.name, "HomeScreen: Registered")
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            context.registerReceiver(receiver, IntentFilter("com.mohit.servicemania.download_complete"),
//                Context.RECEIVER_NOT_EXPORTED)
//        }else {
//           // Context.registerReceiver(receiver, IntentFilter("com.mohit.servicemania.download_complete"))
//        }
//
//        onDispose {
//            Log.d(javaClass.name, "HomeScreen: Disposed")
//            context.unregisterReceiver(receiver)
//        }
//
//    }

    class DownloadResultReceiver(handler: Handler) : ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            super.onReceiveResult(resultCode, resultData)

            if (resultCode == 11) {
               val result =  resultData?.getString("result")
                Log.d(javaClass.name, "onReceiveResult: $result")
                showDialog = false
                showDownloadCompleteDialog = true
            }else {
                val result =  resultData?.getString("song")
                if (!showDialog) {
                    showDialog = true
                }
                songName = result ?: "Unknown Error"
            }
        }
    }

    var isBounded by remember {
        mutableStateOf(false)
    }
    var songDownloadBoundService : SongDownloadBoundService? = null

    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as SongDownloadBoundService.SongDownloadBinder
            songDownloadBoundService = binder.getService()
            songDownloadBoundService?.state?.observe(lifecycleOwner) {
                Log.d("TAG", "onServiceConnected: listened $it")
            }
            isBounded = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("TAG", "onServiceDisconnected: service disconnected")
            isBounded = false
        }
    }

    val resultReceiver = DownloadResultReceiver(Handler((Looper.getMainLooper())))

    if (showDialog) {
        Dialog(onDismissRequest = {
            showDialog = false
        }) {
            Card {
                Column {
                    Text(text = "Song $songName downloaded.")
                    Spacer(modifier = modifier.height(10.dp))
                    CircularProgressIndicator()
                }
            }
        }
    }
    if (showDownloadCompleteDialog) {
        Dialog(onDismissRequest = {
            showDownloadCompleteDialog = false
        }) {
            Card(
                modifier =
                modifier
                    .width(200.dp)
                    .wrapContentHeight()
                    .padding(20.dp)
            ) {
               Text(text = "All song has been downloaded")
            }
        }
    }


    SystemBroadcastReceiver("com.mohit.servicemania.download_complete") { intent ->
        Log.d("Tag", "HomeScreen: Intent Listening")
    }

    var isMusicPlayerBinderConnected by remember {
        mutableStateOf(false)
    }
    var musicButtonState by remember {
        mutableStateOf("Play")
    }
    var musicPlayerBoundService : MusicPlayerBoundService? = null
    val musicPlayerServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(tag, "onServiceConnected: musicPlayerServiceConnection")
            val binder = service as MusicPlayerBoundService.MusicPlayerBinder
            musicPlayerBoundService = binder.getMusicPlayerService()
            isMusicPlayerBinderConnected = true
            musicPlayerBoundService?.musicState?.observe(lifecycleOwner) {
                musicButtonState = if (!it) {
                    "Play"
                }else {
                    "Pause"
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(tag, "onServiceConnected: onServiceDisconnected")
            musicPlayerBoundService = null
            isMusicPlayerBinderConnected = true
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {

    }
    val foregroundServiceLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {

    }


    DisposableEffect(lifecycleOwner) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                foregroundServiceLauncher.launch(Manifest.permission.FOREGROUND_SERVICE)
            }
        }


        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    context.registerReceiver(tImeTickBroadcastReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
                }
                Lifecycle.Event.ON_START -> {
                    Log.d(tag, "HomeScreen: ${event.name} ")
                    val intent = Intent(context, MusicPlayerBoundService::class.java)
                    context.bindService(intent, musicPlayerServiceConnection, Service.BIND_AUTO_CREATE)

                }
                Lifecycle.Event.ON_RESUME -> {
                    Log.d(tag, "HomeScreen: ${event.name} ")
                    if (isMusicPlayerBinderConnected) {
                        if (musicPlayerBoundService!!.isPlaying()) {
                            musicButtonState = "Play"
                        }
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    Log.d(tag, "HomeScreen: ${event.name} ")
                }
                Lifecycle.Event.ON_STOP -> {
                    Log.d(tag, "HomeScreen: ${event.name} ")
                    if (isMusicPlayerBinderConnected) {
                        musicPlayerBoundService?.musicState?.removeObservers(lifecycleOwner)
                        context.unbindService(musicPlayerServiceConnection)
                        isMusicPlayerBinderConnected = false
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    Log.d(tag, "HomeScreen: ${event.name} ")
                    context.unregisterReceiver(tImeTickBroadcastReceiver)
                }
                Lifecycle.Event.ON_ANY -> {
                    Log.d(tag, "HomeScreen: ${event.name} ")
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally) {

        StartedServiceExampleWithName(
            "Started Service On Main Thread",
            onStart = {
                for (i in 1..10) {
                    val intent = Intent(context, SongDownloadService::class.java).apply {
                        putExtra("songName", "Song $i")
                    }
                    context.startService(intent)
                }
            }) {
            context.stopService(Intent(context, SongDownloadService::class.java))
        }

        StartedServiceExampleWithName(
            "Started Service Different Thread With Thread Class",
            onStart = {
                showDialog = true
                for (i in 1..10) {
                    val intent = Intent(context, SongDownloadDifferentThreadService::class.java).apply {
                        putExtra("songName", "Song $i")
                    }
                    context.startService(intent)
                }
            }) {
            context.stopService(Intent(context, SongDownloadDifferentThreadService::class.java))
        }

        StartedServiceExampleWithName(
            "Started Service Different Thread and Handler Class",
            onStart = {
                showDialog = true
                for (i in 1..10) {
                    val intent = Intent(context, SongDownloadUsingSeparateHandlerAndThreadService::class.java).apply {
                        putExtra("songName", "Song $i")
                    }
                    context.startService(intent)
                }
            }) {
            context.stopService(Intent(context, SongDownloadUsingSeparateHandlerAndThreadService::class.java))
        }

        StartedServiceExampleWithName(
            "Started Service HandlerThread Class",
            onStart = {
                showDialog = true
                for (i in 1..10) {
                    val intent = Intent(context, SongDownloadHandlerThreadService::class.java).apply {
                        putExtra("songName", "Song $i")
                    }
                    context.startService(intent)
                }
            }) {
            context.stopService(Intent(context, SongDownloadHandlerThreadService::class.java))
        }

        StartedServiceExampleWithName(
            "Started Service HandlerThread Class With ResultReceiver",
            onStart = {
                showDialog = true
                for (i in 1..10) {
                    val intent = Intent(context,
                        SongDownloadHandlerThreadWithResultReceiverService::class.java).apply {
                        putExtra("songName", "Song $i")
                    }
                    intent.putExtra(Intent.EXTRA_RESULT_RECEIVER, resultReceiver)
                    context.startService(intent)
                }
            }) {
            context.stopService(Intent(context,
                SongDownloadHandlerThreadWithResultReceiverService::class.java))
        }

        StartedServiceExampleWithName(
            "Started Service HandlerThread Class With " +
                    "BroadcastReceiverService",
            onStart = {
                showDialog = true
                for (i in 1..10) {
                    val intent = Intent(context,
                        SongDownloadHandlerThreadWithBroadcastReceiverService::class.java).apply {
                        putExtra("songName", "Song $i")
                    }
                    context.startService(intent)
                }
            }) {
            context.stopService(Intent(context,
                SongDownloadHandlerThreadWithBroadcastReceiverService::class.java))
        }

        StartedServiceExampleWithName(
            "IntentService Example",
            onStart = {
                showDialog = true
                for (i in 1..10) {
                    val intent = Intent(context,
                        SongDownloadIntentService::class.java).apply {
                        putExtra("songName", "Song $i")
                    }
                    context.startService(intent)
                }
            }) {
//            context.stopService(Intent(context,
//                SongDownloadIntentService::class.java))
        }

        StartedServiceExampleWithName(
            "Bound Service Example",
            onStart = {
                if (!isBounded) {
                    val intent = Intent(context, SongDownloadBoundService::class.java)
                    context.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE)
                }else {
                    songDownloadBoundService?.downloadSong("song 1")
                }
            }) {
            context.unbindService(serviceConnection)
        }

        Card {
            Button(onClick = {
                if (isMusicPlayerBinderConnected) {
                    if (musicPlayerBoundService!!.isPlaying()) {
                        musicButtonState = "Play"
                        musicPlayerBoundService?.pause()
                    }else {
                        musicButtonState = "Pause"
                        musicPlayerBoundService?.play()
                        val intent = Intent(context, MusicPlayerBoundService::class.java)
                        context.startService(intent)
                    }
                }
            }) {
                Text(text = musicButtonState)
            }
        }

        StartedServiceExampleWithName(
            "Job Scheduler",
            onStart = {
                val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                val jobInfo = JobInfo.Builder(JOB_ID, ComponentName(context, MyJobSchedulerService::class.java))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    .setMinimumLatency(0)
                    .setPersisted(true)
                    .build()
               val result = jobScheduler.schedule(jobInfo)
                if (result == JobScheduler.RESULT_SUCCESS) {
                    Log.d("TAG", "HomeScreen: Job has been scheduled")
                }else {
                    Log.d("TAG", "HomeScreen: Job not scheduled")
                }
            }) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.cancel(JOB_ID)
        }


        StartedServiceExampleWithName(
            "Work Manager",
            onStart = {
                workManager = WorkManager.getInstance(context.applicationContext)
                workRequest = OneTimeWorkRequest.Builder(MyWorker::class.java)
                    .build()
                workManager?.enqueue(workRequest!!)
                workManager?.beginWith(mutableListOf(workRequest, workRequest))?.then(workRequest!!)
                    ?.enqueue()
            }) {
            if (workManager != null && workRequest != null) {
                workManager?.cancelWorkById(workRequest!!.id)
            }
        }
    }
}


@Composable
fun StartedServiceExampleWithName(name : String, onStart : () -> Unit,
                          modifier: Modifier = Modifier
                          ,onStop : () -> Unit) {
    Card(
        modifier
            .padding(10.dp)
            .fillMaxWidth()) {
        Column(
            modifier
                .padding(10.dp)
                .fillMaxWidth()) {
            Text(text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp)
            Spacer(modifier = modifier.height(10.dp))
            Row(modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly){
                Button(onClick = onStop) {
                    Text(text = "Stop Service")
                }
                Button(onClick = onStart) {
                    Text(text = "Start Service")
                }
            }
        }
    }
}