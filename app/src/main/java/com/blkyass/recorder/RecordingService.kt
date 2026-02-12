package com.blkyass.recorder

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.File

class RecordingService : Service() {
    private lateinit var mediaProjection: MediaProjection
    private lateinit var mediaRecorder: MediaRecorder
    private var virtualDisplay: VirtualDisplay? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channel = NotificationChannel(
            "recorder_channel", "Screen Recorder", NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, "recorder_channel")
            .setContentTitle("Recording in progress")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
        startForeground(1, notification)

        val resultCode = intent?.getIntExtra("code", Activity.RESULT_OK)!!
        val data = intent.getParcelableExtra<Intent>("data")!!
        val projectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)

        startRecording()
        return START_STICKY
    }

    private fun startRecording() {
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

        val file = File(
            getExternalFilesDir(Environment.DIRECTORY_MOVIES),
            "recording_${System.currentTimeMillis()}.mp4"
        )

        mediaRecorder.setOutputFile(file.absolutePath)
        mediaRecorder.setVideoSize(width, height)
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder.setVideoEncodingBitRate(8 * 1000 * 1000)
        mediaRecorder.setVideoFrameRate(30)

        mediaRecorder.prepare()

        virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenRecorder", width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder.surface, null, null
        )

        mediaRecorder.start()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
