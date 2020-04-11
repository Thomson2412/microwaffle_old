package com.soloheisbeer.microwaffle400.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.soloheisbeer.microwaffle400.MainActivity
import com.soloheisbeer.microwaffle400.R
import com.soloheisbeer.microwaffle400.network.NetworkManager
import com.soloheisbeer.microwaffle400.network.StatusUpdateInterface
import com.soloheisbeer.microwaffle400.timer.MicroTimer
import com.soloheisbeer.microwaffle400.utils.MicroUtils
import org.json.JSONObject


interface ServiceUICommunicationInterface {
    fun onServiceTimerTick(tis: Int)
    fun onServiceTimerFinish()
}

class MicroService : Service(),
    StatusUpdateInterface {

    private val notificationID = 1001
    private val channelID = "MicroService"
    private val channelName = "$channelID channel"

    private val networkManager = NetworkManager
    private val microTimer = MicroTimer(
        ::onTimerTick,
        ::onTimerFinish
    )

    private var isRunning = false

    private var serviceUICommunicationCallback: ServiceUICommunicationInterface? = null
    private lateinit var notificationManager: NotificationManager

    companion object {
        fun startService(context: Context, timeInSeconds: Int) {
            val startIntent = Intent(context, MicroService::class.java)
            startIntent.putExtra("timeInSeconds", timeInSeconds)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, MicroService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        notificationManager = getSystemService(NotificationManager::class.java)!!
        val tis = intent.getIntExtra("timeInSeconds", 0)

        networkManager.addStatusUpdateCallback(this)

        startForeground(notificationID, createNotification(tis))
        microTimer.start(tis)
        isRunning = true

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return LocalBinder()
    }

    private fun onTimerTick(tis: Int){
        serviceUICommunicationCallback?.onServiceTimerTick(tis)
        notificationManager.notify(notificationID, createNotification(tis))
    }

    private fun onTimerFinish(){
        serviceUICommunicationCallback?.onServiceTimerFinish()
        notificationManager.cancel(notificationID)
        isRunning = false;
        stopSelf()
    }

    override fun onStatusUpdate(status: JSONObject){
        if (status["running"] as Boolean) {
            isRunning = true
            val tis = status["timeInSeconds"] as Int
            if (microTimer.isRunning) {
                if (microTimer.time - tis > 10) {
                    val diff = tis - microTimer.time
                    microTimer.add(diff)
                }
            }
        }
    }

    fun isRunning(): Boolean {
        return isRunning
    }

    private fun createNotification(timeInSeconds: Int): Notification? {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(channelID, channelName,
                NotificationManager.IMPORTANCE_DEFAULT)

            notificationManager.createNotificationChannel(serviceChannel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )

        return NotificationCompat.Builder(this, channelID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(
                MicroUtils.secondsToTimeString(
                    this,
                    timeInSeconds
                )
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    fun setServiceUICommunicationCallback(suic: ServiceUICommunicationInterface?){
        serviceUICommunicationCallback = suic
    }

    override fun onDestroy() {
        super.onDestroy()
        networkManager.removeStatusUpdateCallback(this)
    }

    inner class LocalBinder : Binder(){
        val service: MicroService
            get()= this@MicroService
    }
}
