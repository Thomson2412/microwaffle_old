const TAG = 'Timer';
const utils = require('../utils/Utils');
const timerState = require('../hardware/TimerState')

const countdownIntervalMS = 1000;
let timerIntervalFunction = null;
let state = timerState.NOT_SET
let timeInSeconds = 0;
let isRunning = false;
let timerUpdateCallback = null;
let timerDoneCallback = null;

module.exports = class Timer {

    constructor(tdcb, tucb) {
        if(typeof tdcb != "function"){
            utils.log(TAG, "Timer done callback not set");
        }
        if(typeof tucb != "function"){
            utils.log(TAG, "Timer update callback not set");
        }
        timerUpdateCallback = tucb;
        timerDoneCallback = tdcb;
    }

    set (ts) {
        if(!Number.isInteger(ts)){
            utils.log(TAG, "Timer needs a number in seconds to be set");
            return;
        }
        if(state === timerState.RUNNING || state === timerState.PAUSED){
            utils.log(TAG, "Timer is already running or paused");
            return;
        }

        timeInSeconds = ts;
        state = timerState.SET
    }

    add (ts){
        if(!Number.isInteger(ts)){
            utils.log(TAG, "Timer needs a number in seconds to be set");
            return;
        }
        timeInSeconds += ts;
    }

    start (){
        if(state === timerState.RUNNING){
            utils.log(TAG, "Timer is already running");
            return;
        }
        if(state === timerState.NOT_SET){
            utils.log(TAG, "Timer is not set");
            return;
        }
        state = timerState.RUNNING;
        this.countdown();
        timerIntervalFunction = setInterval(this.countdown, countdownIntervalMS);
    }

    pause (){
        if(state !== timerState.RUNNING){
            utils.log(TAG, "Timer is not running");
            return;
        }
        clearInterval(timerIntervalFunction);
        state = timerState.PAUSED
    }

    reset (){
        clearInterval(timerIntervalFunction);
        timeInSeconds = 0;
        state = timerState.NOT_SET
    }

    getCurrentTime(){
        return timeInSeconds;
    }

    getState(){
        return state;
    }

    countdown() {
        timeInSeconds--;
        utils.log(TAG, "Timer remaining: " + timeInSeconds);
        if(typeof timerUpdateCallback == "function"){
            timerUpdateCallback(timeInSeconds);
        }
        if(timeInSeconds <= 0){
            timeInSeconds = 0;
            clearInterval(timerIntervalFunction);
            isRunning = false;
            if(typeof timerDoneCallback == "function"){
                timerDoneCallback();
            }
        }
    }
};