package com.zjgsu.yzwtodocalendar.todoServices

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.zjgsu.yzwtodocalendar.yzwAlarmController
import com.zjgsu.yzwtodocalendar.R

class yzwNormalNotifyService:Service (){
    override fun onBind(intent: Intent): IBinder {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val bundle = intent.extras
        Log.d("NormalNotifyServicebundle",bundle.toString())
        val name = bundle!!.getString("name")
        val persons = bundle.getString("persons")
        val location = bundle.getString("location")
        val notificationType = bundle.getString("notification")
        Log.d("notificationType",notificationType.toString())
        // 通知管理器
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel1 = NotificationChannel("alarmNotify", "日程响铃提醒", NotificationManager.IMPORTANCE_HIGH)
        val channel2 = NotificationChannel("normalNotify", "日程通知提醒", NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(channel1)
        manager.createNotificationChannel(channel2)
        if (notificationType.toString() == "响铃提醒") {
            // 如果响铃通知就让铃声响起
            Log.d("notificationType","响铃提醒")
            val stopRing = Intent(this, yzwStopAlarmService::class.java)
            val stopRingPI = PendingIntent.getService(this, 0, stopRing, 0)
            yzwAlarmController.playRing(this)
            val notification = NotificationCompat.Builder(this, "normalNotify")
                .setContentTitle("日程：$name")
                .setContentText("参与人:$persons 地点:$location")
                .setSubText("参与人:$persons 地点:$location")
                .setSmallIcon(R.drawable.todoicon_128)
                .setLargeIcon(BitmapFactory.decodeResource(resources,R.drawable.todoicon_128))
                .setSound(null)
                .addAction(2,"关闭",stopRingPI)
                .build()
            manager.notify(2, notification)
        }else{
            val notification = NotificationCompat.Builder(this, "normalNotify")
                .setContentTitle("日程：$name")
                .setContentText("参与人:$persons 地点:$location")
                .setSubText("参与人:$persons 地点:$location")
                .setSmallIcon(R.drawable.todoicon_128)
                .setLargeIcon(BitmapFactory.decodeResource(resources,R.drawable.todoicon_128))
                .build()
            manager.notify(2, notification)
        }
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyService", "onDestroy executed")
    }
}