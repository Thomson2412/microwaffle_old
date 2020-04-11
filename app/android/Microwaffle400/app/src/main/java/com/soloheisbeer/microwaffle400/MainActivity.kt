package com.soloheisbeer.microwaffle400

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.soloheisbeer.microwaffle400.audio.AudioManager
import com.soloheisbeer.microwaffle400.network.ConnectionUpdateInterface
import com.soloheisbeer.microwaffle400.network.NetworkManager
import com.soloheisbeer.microwaffle400.service.MicroService
import com.soloheisbeer.microwaffle400.service.ServiceUICommunicationInterface
import com.soloheisbeer.microwaffle400.utils.MicroUtils

class MainActivity : AppCompatActivity(),
    ConnectionUpdateInterface,
    ServiceUICommunicationInterface {

    private val TAG = "MAIN"
    private var timeInSeconds = 0
    private lateinit var timerText: TextView
    private val audioManager =
        AudioManager(this@MainActivity)
    private val networkManager = NetworkManager
    private var connectingDialog: AlertDialog? = null

    private var microService: MicroService? = null
    private val microServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            microService = (iBinder as MicroService.LocalBinder).service
            microService!!.setServiceUICommunicationCallback(this@MainActivity)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            microService = null
        }
    }

    private val minSteps = 60 * 1
    private val secSteps = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            if(timeInSeconds > 0) {
                MicroService.startService(this, timeInSeconds)
                val intent = Intent(this, MicroService::class.java)
                bindService(intent, microServiceConnection, Context.BIND_AUTO_CREATE)
                audioManager.play("loop", true, 0.5f)
            }
            audioManager.play("up")
        }

        stopButton.setOnClickListener {
            MicroService.stopService(this)
            if(microService != null)
                unbindService(microServiceConnection)
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

        showConnectingDialog()
        networkManager.addConnectionUpdateCallback(this)
        networkManager.connectToMicrowave()
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
            connectingDialog!!.cancel()
        }
    }

    override fun disconnectedToMicrowave() {
        this@MainActivity.runOnUiThread {
            showConnectingDialog()
        }
    }

    override fun onServiceTimerTick(tis: Int){
        timeInSeconds = tis
        updateTimerText()
        audioManager.play("down")
    }

    override fun onServiceTimerFinish(){
        audioManager.play("alarm", true)
        audioManager.stop("loop")
    }

    private fun updateTimerText(){
        timerText.text = MicroUtils.secondsToTimeString(this, timeInSeconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        microService?.setServiceUICommunicationCallback(null)
        audioManager.cleanUp()
    }
}
