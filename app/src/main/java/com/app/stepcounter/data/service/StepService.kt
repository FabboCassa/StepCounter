package com.app.stepcounter.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.app.stepcounter.data.local.StepPreferences
import com.app.stepcounter.data.preferences.StepPreferencesProvider
import java.util.Timer
import java.util.TimerTask

class StepService : Service(), SensorEventListener {

    private lateinit var stepPreferences: StepPreferences

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var baseStepCount: Float = -1f

    private var timer: Timer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        stepPreferences = StepPreferencesProvider.getInstance(this)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Log.e("StepService", "Sensore TYPE_STEP_COUNTER non disponibile!")
        } else {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
            Log.d("StepService", "Sensore registrato correttamente")
        }


        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        startForegroundServiceWithNotification()
        startTimer()
    }


    private fun startTimer() {
        val startTime = stepPreferences.getStartTime()

        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val elapsedMinutes = ((currentTime - startTime) / 60000).toInt()
                stepPreferences.updateTime(elapsedMinutes)
            }
        }, 0, 60000)
    }

    private fun startForegroundServiceWithNotification() {
        val channelId = "step_counter_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Step Counter",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Contapassi attivo")
            .setContentText("Sto contando i tuoi passi")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()

        startForeground(1, notification)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val currentStepCount = event.values[0]
            if (baseStepCount < 0f) {
                baseStepCount = currentStepCount
            }
            val stepsSinceStart = (currentStepCount - baseStepCount).toInt()
            stepPreferences.updateSteps(stepsSinceStart)
            Log.d("StepService", "Steps: $stepsSinceStart")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        timer?.cancel()
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
}