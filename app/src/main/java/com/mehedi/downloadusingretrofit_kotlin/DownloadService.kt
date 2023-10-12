package com.mehedi.downloadusingretrofit_kotlin

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import okhttp3.ResponseBody
import retrofit2.Retrofit
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class DownloadService : IntentService("DownloadService") {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private var totalFileSize = 0


    override fun onHandleIntent(intent: Intent?) {
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT
            >= Build.VERSION_CODES.O
        ) {
            makeNotificationChannel(
                "CHANNEL_1", "Example channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationBuilder = NotificationCompat.Builder(this, "CHANNEL_1")
                .setSmallIcon(R.drawable.ic_download)
                .setContentTitle("Download")
                .setContentText("Downloading File")
                .setAutoCancel(true)

        } else {

            notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_download)
                .setContentTitle("Download")
                .setContentText("Downloading File")
                .setAutoCancel(true)
        }


        notificationManager.notify(0, notificationBuilder.build())
        initDownload()
    }

    private fun initDownload() {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://file-examples.com/")
            .build()
        val retrofitInterface: RetrofitInterface = retrofit.create(RetrofitInterface::class.java)
        val request = retrofitInterface.downloadFile()
        try {
            request.execute().body()?.let { downloadFile(it) }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun downloadFile(body: ResponseBody) {
        var count: Int
        val data = ByteArray(1024 * 4)
        val fileSize: Long = body.contentLength()
        val bis: InputStream = BufferedInputStream(body.byteStream(), 1024 * 8)
        val outputFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "file.zip"
        )
        val output: OutputStream = FileOutputStream(outputFile)
        var total: Long = 0
        val startTime = System.currentTimeMillis()
        var timeCount = 1
        while (bis.read(data).also { count = it } != -1) {
            total += count.toLong()
            totalFileSize = (fileSize / Math.pow(1024.0, 2.0)).toInt()
            val current = Math.round(total / Math.pow(1024.0, 2.0)).toDouble()
            val progress = (total * 100 / fileSize).toInt()
            val currentTime = System.currentTimeMillis() - startTime
            val download = Download()
            download.totalFileSize = totalFileSize
            if (currentTime > 1000 * timeCount) {
                download.currentFileSize = current.toInt()
                download.progress = progress
                sendNotification(download)
                timeCount++
            }
            output.write(data, 0, count)
        }
        onDownloadComplete()
        output.flush()
        output.close()
        bis.close()
    }

    private fun sendNotification(download: Download) {

        sendIntent(download)
        if (Build.VERSION.SDK_INT
            >= Build.VERSION_CODES.O
        ) {
            makeNotificationChannel(
                "CHANNEL_1", "Example channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val notificationBuilder = NotificationCompat.Builder(
                this,
                "CHANNEL_1"
            )

        }




        notificationBuilder.setProgress(100, download.progress, false)
        notificationBuilder.setContentText(
            String.format(
                "Downloaded (%d/%d) MB",
                download.currentFileSize,
                download.totalFileSize
            )
        )
        notificationManager.notify(0, notificationBuilder.build())
    }


    private fun sendIntent(download: Download) {
        val intent = Intent(MainActivity.MESSAGE_PROGRESS)
        intent.putExtra("download", download)
        LocalBroadcastManager.getInstance(this@DownloadService).sendBroadcast(intent)
    }

    private fun onDownloadComplete() {
        val download = Download()
        download.progress = 100
        sendIntent(download)
        notificationManager.cancel(0)
        notificationBuilder.setProgress(0, 0, false)
        notificationBuilder.setContentText("File Downloaded")
        notificationManager.notify(0, notificationBuilder.build())
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        notificationManager.cancel(0)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun makeNotificationChannel(
        channelId: String,
        channelName: String,
        importance: Int
    ) {
        val channel = NotificationChannel(
            channelId, channelName, importance
        )
        val notificationManager = getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.createNotificationChannel(
            channel
        )
    }


}