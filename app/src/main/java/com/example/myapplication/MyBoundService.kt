package com.example.myapplication

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class MyBoundService : Service() {

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): MyBoundService = this@MyBoundService
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d("BoundService", "onBind вызван")
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("BoundService", "Сервис создан")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("BoundService", "Сервис уничтожен")
    }
}