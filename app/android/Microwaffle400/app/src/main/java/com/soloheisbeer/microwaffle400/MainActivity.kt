package com.soloheisbeer.microwaffle400

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private val TAG = "MAIN"
    private var timeInSeconds = 0
    private val networkManager = NetworkManager()
    private lateinit var timerText: TextView
    private val microTimer = MicroTimer(::onTimerTick, ::onTimerFinish)
    private val audioManager = AudioManager(this@MainActivity)

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

        updateSate()

        // set on-click listener
        startButton.setOnClickListener {
            if(timeInSeconds > 0) {
                networkManager.startMicrowave(timeInSeconds)
                microTimer.start(timeInSeconds)
                audioManager.play("loop", true, 0.5f)
            }
            audioManager.play("up")
        }

        stopButton.setOnClickListener {
            networkManager.stopMicrowave()
            microTimer.stop()
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
    }

    fun updateSate(){
        val status = networkManager.getStatus()
        if(status == null){
            val alertDialog: AlertDialog = AlertDialog.Builder(this@MainActivity).create()
            alertDialog.setTitle("Microwaffle not found")
            alertDialog.setMessage("Clossing you mofo")
            alertDialog.setCancelable(false)
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ -> dialog.dismiss(); this.finishAffinity() }
            alertDialog.show()

            return
        }


        val statusJson = JSONObject(status)
        if(statusJson["busy"] as Boolean) {
            timeInSeconds = statusJson["timer"] as Int
            if(microTimer.isRunning){
                if(microTimer.time - timeInSeconds > secSteps) {
                    val diff = timeInSeconds - microTimer.time
                    microTimer.add(diff)
                }
            }
            else {
                microTimer.start(timeInSeconds)
            }
            updateTimerText()
        }
    }

    fun onTimerTick(tis: Int){
        if(microTimer.time > 0 && microTimer.time % 60 == 0){
            updateSate()
        }
        else {
            timeInSeconds = tis
            updateTimerText()
        }
        audioManager.play("down")
    }

    fun onTimerFinish(){
        audioManager.play("alarm", true)
        audioManager.stop("loop")
    }


    fun updateTimerText(){
        timerText.setText(secondsToTimeString(timeInSeconds))
    }

    fun secondsToTimeString(tis: Int): String{
        val min  = tis / 60
        val sec = tis % 60
        return getString(R.string.timer, min, sec)
    }

    override fun onDestroy() {
        super.onDestroy()
        microTimer.stop()
        audioManager.cleanUp()
    }
}
