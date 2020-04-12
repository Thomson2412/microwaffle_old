package com.soloheisbeer.microwaffle400.timer

import android.os.CountDownTimer

interface TimerStatusInterface{
    fun onTimerTick(timeLeftInSeconds: Int)
    fun onTimerFinish()
}

class MicroTimer(tsu: TimerStatusInterface) {

    private var timer: CountDownTimer? = null
    private val timerStatusCallback: TimerStatusInterface = tsu

    var time = 0
        private set
    var isRunning = false
        private set

    fun start(sec: Int) {
        if(sec <= 0)
            return

        timer = object: CountDownTimer((1000 * sec).toLong(), 1000) {

            override fun onTick(millisUntilFinished: Long) {
                time = (millisUntilFinished / 1000).toInt()
                timerStatusCallback.onTimerTick(time)
            }

            override fun onFinish() {
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
        time = 0
        isRunning = false
    }

    fun add(sec: Int){
        val newTime = time + sec
        if(newTime > 0) {
            stop()
            start(newTime)
        }
        else{
            stop()
        }
    }
}