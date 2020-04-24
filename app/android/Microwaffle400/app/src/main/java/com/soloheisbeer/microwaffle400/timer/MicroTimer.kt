package com.soloheisbeer.microwaffle400.timer

import java.util.*


interface TimerStatusInterface{
    fun onTimerTick(timeLeftInSeconds: Int)
    fun onTimerFinish()
}

class MicroTimer(tsc: TimerStatusInterface) {

    var timeInSeconds = 0
        private set
    var state = MicroTimerState.NOT_SET
        private set

    private val countDownIntervalMS: Long = 1000
    private val timerStatusCallback: TimerStatusInterface = tsc
    private var timer = Timer()

    fun set(tis: Int){
        if(state == MicroTimerState.RUNNING || state == MicroTimerState.PAUSED){
            return
        }

        timeInSeconds = tis
        state = MicroTimerState.SET
    }

    fun add(tis: Int){
        if(state == MicroTimerState.RUNNING || state == MicroTimerState.PAUSED){
            timeInSeconds += tis
        }
    }

    fun start() {
        synchronized(timer) {
            if (timeInSeconds <= 0 ||
                state == MicroTimerState.RUNNING ||
                state == MicroTimerState.NOT_SET
            )
                return

            timer = Timer()
            state = MicroTimerState.RUNNING
            timer.scheduleAtFixedRate(CountDownTask(), 0, countDownIntervalMS)
        }
    }

    fun pause(){
        synchronized(timer) {
            if (state != MicroTimerState.RUNNING)
                return

            timer.cancel()
            timer.purge()
            state = MicroTimerState.PAUSED
        }
    }

    fun reset(){
        synchronized(timer) {
            timer.cancel()
            timer.purge()
            timeInSeconds = 0
            state = MicroTimerState.NOT_SET
        }
    }

    private fun countDown(){
        if(timeInSeconds <= 0) {
            timerStatusCallback.onTimerFinish()
        }
        else {
            timeInSeconds--
            timerStatusCallback.onTimerTick(timeInSeconds)
        }
    }

    inner class CountDownTask : TimerTask() {
        override fun run() {
            Thread {
                run {
                    countDown()
                }
            }.start()
        }
    }
}