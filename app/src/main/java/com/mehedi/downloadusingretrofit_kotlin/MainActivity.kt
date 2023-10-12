package com.mehedi.downloadusingretrofit_kotlin

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import com.mehedi.downloadusingretrofit_kotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    companion object {
        const val MESSAGE_PROGRESS = "message_progress"
    }


    private val PERMISSION_REQUEST_CODE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        registerReceiver()

        binding.btnDownload.setOnClickListener {
            downloadFile()
        }


    }


    private fun downloadFile() {
        if (checkPermission()) {
            startDownload()
        } else {
            requestPermission()
        }
    }

    private fun startDownload() {
        val intent = Intent(this, DownloadService::class.java)
        startService(intent)
    }

    private fun registerReceiver() {
        val bManager: LocalBroadcastManager = LocalBroadcastManager.getInstance(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(MESSAGE_PROGRESS)
        bManager.registerReceiver(broadcastReceiver, intentFilter)
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == MESSAGE_PROGRESS) {
                val download: Download = intent.getParcelableExtra("download")!!
                binding.progress.progress = download.progress
                if (download.progress === 100) {
                    binding.progressText.text = "File Download Complete"
                } else {
                    binding.progressText.text = java.lang.String.format(
                        "Downloaded (%d/%d) MB",
                        download.currentFileSize,
                        download.totalFileSize
                    )
                }
            }
        }
    }

    private fun checkPermission(): Boolean {
        val result: Int = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }


//    fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String?>?,
//        grantResults: IntArray
//    ) {
//        if (permissions != null) {
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        }
//        when (requestCode) {
//            PERMISSION_REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startDownload()
//            } else {
//                Snackbar.make(
//                    findViewById(R.id.mainLayout),
//                    "Permission Denied, Please allow to proceed !",
//                    Snackbar.LENGTH_LONG
//                ).show()
//            }
//        }
//    }

}
