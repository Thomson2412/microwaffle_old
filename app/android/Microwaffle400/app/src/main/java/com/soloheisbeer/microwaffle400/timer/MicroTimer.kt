package com.soloheisbeer.microwaffle400.timer

import android.os.CountDownTimer

interface TimerStatusInterface{
    fun onTimerTick(timeLeftInSeconds: Int)
    fun onTimerFinish()
}

class MicroTimer(tsu: TimerStatusInterface) {

    private var timer: CountDownTimer? = null
    private val timerStatusCallback: TimerStatusInterface = tsu

    var timeInSeconds = 0
        private set
    var isRunning = false
        private set

    fun start(sec: Int) {
        if(sec <= 0 || isRunning)
            return

        timer = object: CountDownTimer((1000 * sec).toLong(), 1000) {

            override fun onTick(millisUntilFinished: Long) {
                timeInSeconds = (millisUntilFinished / 1000).toInt()
                timerStatusCallback.onTimerTick(timeInSeconds)
            }

            override fun onFinish() {
                timeInSeconds = 0
                timerStatusCallback.onTimerFinish()
                isRunning = false
            }

        }

        timer?.start()
        isRunning = true
    }

    fun stop(){
        timer?.cancel()
        timer = null
        timeInSeconds = 0
        isRunning = false
    }

    fun add(sec: Int){
        val newTime = timeInSeconds + sec
        if(newTime > 0) {
            stop()
            start(newTime)
        }
        else{
            stop()
        }
    }
}