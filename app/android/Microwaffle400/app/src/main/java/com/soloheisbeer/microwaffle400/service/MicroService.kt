package com.soloheisbeer.microwaffle400.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.soloheisbeer.microwaffle400.MainActivity
import com.soloheisbeer.microwaffle400.R
import com.soloheisbeer.microwaffle400.network.NetworkManager
import com.soloheisbeer.microwaffle400.network.StatusUpdateInterface
import com.soloheisbeer.microwaffle400.timer.MicroTimer
import com.soloheisbeer.microwaffle400.timer.TimerStatusInterface
import com.soloheisbeer.microwaffle400.utils.MicroUtils
import org.json.JSONObject

class MicroService : Service(),
    StatusUpdateInterface,
    TimerStatusInterface {

    companion object {

        const val ACTION_TIMER_TICK = "ACTION_TIMER_TICK"
        const val ACTION_TIMER_FINISH = "ACTION_TIMER_FINISH"
        const val DATA_TIMER_TIME_LEFT = "DATA_TIMER_TIME_LEFT"

        private const val DATA_TIME = "DATA_TIME"

        fun startService(context: Context, timeInSeconds: Int) {
            val startIntent = Intent(context, MicroService::class.java)
            startIntent.putExtra(DATA_TIME, timeInSeconds)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, MicroService::class.java)
            context.stopService(stopIntent)
        }
    }

    private val notificationID = 1001
    private val channelID = "MicroService"
    private val channelName = "$channelID channel"

    private val networkManager = NetworkManager
    private val microTimer = MicroTimer(this)

    private lateinit var notificationManager: NotificationManager


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        notificationManager = getSystemService(NotificationManager::class.java)!!
        val tis = intent.getIntExtra(DATA_TIME, 0)


        networkManager.addStatusUpdateCallback(this)
        networkManager.startMicrowave(tis)
        microTimer.start(tis)

        startForeground(notificationID, createNotification(tis))


        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null

    }

    override fun onTimerTick(timeLeftInSeconds: Int){
        notificationManager.notify(notificationID, createNotification(timeLeftInSeconds))
        Intent().also { intent ->
            intent.action = ACTION_TIMER_TICK
            intent.putExtra(DATA_TIMER_TIME_LEFT, timeLeftInSeconds)
            intent.setPackage(this.packageName)
            sendBroadcast(intent)
        }
    }

    override fun onTimerFinish(){
        stopForeground(true)
        Intent().also { intent ->
            intent.action = ACTION_TIMER_FINISH
            intent.setPackage(this.packageName)
            sendBroadcast(intent)
        }
        stopSelf()
    }

    override fun onStatusUpdate(status: JSONObject){
        if (status["running"] as Boolean) {
            val tis = status["timeInSeconds"] as Int
            if (microTimer.isRunning) {
                if (microTimer.time - tis > 10) {
                    val diff = tis - microTimer.time
                    microTimer.add(diff)
                }
            }
        }
    }

    private fun createNotification(timeInSeconds: Int): Notification? {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(channelID, channelName,
                NotificationManager.IMPORTANCE_LOW)

            notificationManager.createNotificationChannel(serviceChannel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )

        return NotificationCompat.Builder(this, channelID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(MicroUtils.secondsToTimeString(this, timeInSeconds))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setColor(getColor(R.color.colorAccent))
            .setColorized(true)
            .setSound(null)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(microTimer.isRunning) {
            microTimer.stop()
            networkManager.stopMicrowave()
        }
        networkManager.removeStatusUpdateCallback(this)
        Intent().also { intent ->
            intent.action = ACTION_TIMER_FINISH
            intent.setPackage(this.packageName)
            sendBroadcast(intent)
        }
    }
}
