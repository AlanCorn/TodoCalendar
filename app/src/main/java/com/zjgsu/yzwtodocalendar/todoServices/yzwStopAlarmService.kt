package com.zjgsu.yzwtodocalendar.todoServices


import android.app.*
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.zjgsu.yzwtodocalendar.yzwAlarmController

class yzwStopAlarmService:Service (){
    override fun onBind(intent: Intent): IBinder {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        Log.d("onStartCommand","ssss")
        yzwAlarmController.stopRing()
        return super.onStartCommand(intent, flags, startId)
    }
}