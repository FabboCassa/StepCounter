package com.app.stepcounter.com.app.stepcounter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import java.util.Timer
import java.util.TimerTask

class StepService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var baseStepCount: Float = -1f

    // Timer per il conteggio dei minuti
    private var timer: Timer? = null
    private var startTime: Long = 0L
    private lateinit var prefs: SharedPreferences

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences("prefs", MODE_PRIVATE)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        startForegroundServiceWithNotification()
        startTimer()
    }

    private fun startTimer() {
        // Recupera il tempo di inizio salvato o imposta quello attuale
        startTime = prefs.getLong("start_time", System.currentTimeMillis())

        // Se è la prima volta, salva il tempo di inizio
        if (!prefs.contains("start_time")) {
            prefs.edit { putLong("start_time", startTime) }
        }

        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                updateTime()
            }
        }, 0, 60000) // Aggiorna ogni minuto (60000 ms)
    }

    private fun updateTime() {
        val currentTime = System.currentTimeMillis()
        val elapsedMinutes = ((currentTime - startTime) / 60000).toInt()

        prefs.edit { putInt("time", elapsedMinutes) }

        Log.d("StepService", "Tempo aggiornato: $elapsedMinutes minuti")
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

            prefs.edit { putInt("steps", stepsSinceStart) }

            Log.d("StepService", "Passi aggiornati: $stepsSinceStart")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        timer?.cancel()
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
}
