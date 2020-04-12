package com.soloheisbeer.microwaffle400

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.soloheisbeer.microwaffle400.audio.AudioManager
import com.soloheisbeer.microwaffle400.network.ConnectionUpdateInterface
import com.soloheisbeer.microwaffle400.network.NetworkManager
import com.soloheisbeer.microwaffle400.network.StatusUpdateInterface
import com.soloheisbeer.microwaffle400.service.MicroService
import com.soloheisbeer.microwaffle400.utils.MicroUtils
import org.json.JSONObject


class MainActivity : AppCompatActivity(),
    ConnectionUpdateInterface,
    StatusUpdateInterface {

    private val TAG = "MAIN"
    private var timeInSeconds = 0
    private var isRunning = false
    private lateinit var timerText: TextView
    private val audioManager = AudioManager(this@MainActivity)
    private val networkManager = NetworkManager
    private  val microService = MicroService
    private var connectingDialog: AlertDialog? = null

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            if (action == microService.ACTION_TIMER_TICK) {
                val tis = intent.getIntExtra(microService.DATA_TIMER_TIME_LEFT, 0)
                onTimerTick(tis)
            }
            else if (action == microService.ACTION_TIMER_FINISH) {
                onTimerFinish()
            }
        }
    }

    private val minSteps = 60 * 1
    private val secSteps = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        networkManager.addConnectionUpdateCallback(this)
        networkManager.addStatusUpdateCallback(this)
        networkManager.connectToMicrowave()

        audioManager.init()
        audioManager.play("boot")

        timerText = findViewById(R.id.timer_text)
        updateTimerText()

        val startButton = findViewById<Button>(R.id.button_start)
        val stopButton = findViewById<Button>(R.id.button_stop)

        val mpButton = findViewById<Button>(R.id.button_mp)
        val spButton = findViewById<Button>(R.id.button_sp)
        val mmButton = findViewById<Button>(R.id.button_mm)
        val smButton = findViewById<Button>(R.id.button_sm)

        // set on-click listener
        startButton.setOnClickListener {
            if(timeInSeconds > 0 && !isRunning) {
                microService.startService(this, timeInSeconds)
                isRunning = true
                audioManager.play("loop", true, 0.5f)
            }
            audioManager.play("up")
        }

        stopButton.setOnClickListener {
            microService.stopService(this)
            isRunning = false
            audioManager.play("down")
            audioManager.stop("loop")
            audioManager.stop("alarm")
        }

        mpButton.setOnClickListener {
            if(timeInSeconds < minSteps * 99) {
                timeInSeconds += minSteps
            }
            updateTimerText()
            audioManager.play("up")
        }

        spButton.setOnClickListener {
            if(timeInSeconds < minSteps * 99) {
                timeInSeconds += secSteps
            }
            audioManager.play("up")
            updateTimerText()
        }

        mmButton.setOnClickListener {
            if(timeInSeconds - minSteps >= 0) {
                timeInSeconds -= minSteps
            }
            audioManager.play("down")
            updateTimerText()
        }


        smButton.setOnClickListener {
            if(timeInSeconds - secSteps >= 0) {
                timeInSeconds -= secSteps
            }
            audioManager.play("down")
            updateTimerText()
        }

        if(!networkManager.isConnected)
            showConnectingDialog()
    }

    override fun onStart() {
        super.onStart()

        audioManager.mute(false)

        val filter = IntentFilter()
        filter.addAction(microService.ACTION_TIMER_TICK)
        filter.addAction(microService.ACTION_TIMER_FINISH)
        registerReceiver(receiver, filter)
    }

    private fun showConnectingDialog(){
        connectingDialog = AlertDialog.Builder(this@MainActivity).create()
        connectingDialog!!.setTitle(
            getString(R.string.connecting_dialog_title, getString(R.string.app_name)))
        connectingDialog!!.setMessage(
            getString(R.string.connecting_dialog_content, getString(R.string.app_name)))
        connectingDialog!!.setCancelable(false)
        connectingDialog!!.setButton(
            AlertDialog.BUTTON_NEUTRAL, getString(R.string.connecting_dialog_dismiss)) {
                dialog, _ -> dialog.dismiss(); this.finishAffinity()
        }
        connectingDialog!!.show()
    }

    override fun connectedToMicrowave() {
        this@MainActivity.runOnUiThread {
            connectingDialog?.cancel()
        }
    }

    override fun disconnectedToMicrowave() {
        this@MainActivity.runOnUiThread {
            showConnectingDialog()
        }
    }

    override fun onStatusUpdate(status: JSONObject){
        if (isRunning != (status["running"] as Boolean)) {
            timeInSeconds = status["timeInSeconds"] as Int
            isRunning = (status["running"] as Boolean)
            if(isRunning){
                microService.startService(this, timeInSeconds)
            }
        }
    }

    private fun onTimerTick(tis: Int){
        timeInSeconds = tis
        updateTimerText()
        audioManager.play("down")
    }

    private fun onTimerFinish(){
        if(timeInSeconds == 0) {
            audioManager.play("alarm", true)
            audioManager.stop("loop")
        }
        timeInSeconds = 0
        updateTimerText()

    }

    private fun updateTimerText(){
        timerText.text = MicroUtils.secondsToTimeString(this, timeInSeconds)
    }

    override fun onPause() {
        super.onPause()
        audioManager.mute(true)
        unregisterReceiver(receiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        audioManager.cleanUp()
    }
}
