package com.mohit.servicemania.services.started_services.thread_and_handler

import android.os.Looper

class DownloadThread: Thread() {

    var downloadHandler: DownloadHandler? = null

    override fun run() {
        super.run()
        Looper.prepare()
        downloadHandler = DownloadHandler()
        Looper.loop()
    }
}