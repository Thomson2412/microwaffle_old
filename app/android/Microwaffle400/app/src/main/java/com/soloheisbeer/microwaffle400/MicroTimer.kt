package com.soloheisbeer.microwaffle400

import android.os.CountDownTimer

class MicroTimer(val onTickCb: (time: Int) -> Unit, val onFinishCb: () -> Unit) {

    private var timer: CountDownTimer? = null
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
                onTickCb(time)
            }

            override fun onFinish() {
                onFinishCb()
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
        onTickCb(0)
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