package com.zjgsu.yzwtodocalendar

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri


object yzwAlarmController {
    private var mMediaPlayer: MediaPlayer? = null

    //开始播放
    fun playRing(context: Context?) {
        try {
            //用于获取手机默认铃声的Uri
            val alert: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            mMediaPlayer = MediaPlayer()
            if (context != null) {
                mMediaPlayer!!.setDataSource(context, alert)
            }
            mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_RING)
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.prepare()
            mMediaPlayer!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //停止播放
    fun stopRing() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer!!.isPlaying) {
                mMediaPlayer!!.stop()
                mMediaPlayer!!.release()
            }
        }
    }


}