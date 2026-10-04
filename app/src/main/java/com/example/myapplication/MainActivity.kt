package com.example.myapplication

import android.app.DownloadManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private var boundService: MyBoundService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as MyBoundService.LocalBinder
            boundService = binder.getService()
            isBound = true
            Log.d("BoundService", "Сервис подключен (Bound)")
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            Log.d("BoundService", "Сервис отключен")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onStartBound = {
                            Log.d("BoundService", "Сервис запустился")
                            Toast.makeText(this, "Сервис запустился", Toast.LENGTH_SHORT).show()
                            if (!isBound) {
                                Intent(this, MyBoundService::class.java).also { intent ->
                                    bindService(intent, connection, Context.BIND_AUTO_CREATE)
                                }
                            }
                        },
                        onStopBound = {
                            Log.d("BoundService", "Сервис завершился")
                            Toast.makeText(this, "Сервис завершился", Toast.LENGTH_SHORT).show()
                            if (isBound) {
                                unbindService(connection)
                                isBound = false
                            }
                        },
                        onStartForeground = { startForegroundService() },
                        onStopForeground = { stopForegroundService() },
                        onDownload = { downloadMp3() }
                    )
                }
            }
        }
    }

    private fun startForegroundService() {
        val intent = Intent(this, MyForegroundService::class.java)
        intent.putExtra(Constants.inputExtra, "Started")
        ContextCompat.startForegroundService(this, intent)
    }

    private fun stopForegroundService() {
        val intent = Intent(this, MyForegroundService::class.java)
        stopService(intent)
    }

    private fun downloadMp3() {
        val fileUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
        val fileName = "my_downloaded_song.mp3"

        val request = DownloadManager.Request(Uri.parse(fileUrl))
            .setTitle(fileName)
            .setDescription("Downloading MP3...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)

        Toast.makeText(this, "Загрузка MP3 началась", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun MainScreen(
    onStartBound: () -> Unit,
    onStopBound: () -> Unit,
    onStartForeground: () -> Unit,
    onStopForeground: () -> Unit,
    onDownload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onStartBound, modifier = Modifier.fillMaxWidth()) {
            Text("Запустить Bound Service (Лог)")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onStopBound, modifier = Modifier.fillMaxWidth()) {
            Text("Остановить Bound Service (Лог)")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onStartForeground, modifier = Modifier.fillMaxWidth()) {
            Text("Start Foreground Service")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onStopForeground, modifier = Modifier.fillMaxWidth()) {
            Text("Stop Foreground Service")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onDownload, modifier = Modifier.fillMaxWidth()) {
            Text("Скачать MP3 (DownloadManager)")
        }
    }
}