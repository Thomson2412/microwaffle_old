const TAG = 'Timer';
const utils = require('../utils/Utils');
const countdownIntervalMS = 1000;
let timerIntervalFunction = null;
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
        if(isRunning){
            utils.log(TAG, "Timer is already running");
            return;
        }
        timeInSeconds = ts;
    }

    add (ts){
        if(!Number.isInteger(ts)){
            utils.log(TAG, "Timer needs a number in seconds to be set");
            return;
        }
        timeInSeconds += ts;
    }

    subtract (ts){
        if(!Number.isInteger(ts)){
            utils.log(TAG, "Timer needs a number in seconds to be set");
            return;
        }
        timeInSeconds -= ts;
    }

    start (){
        if(isRunning){
            utils.log(TAG, "Timer is already running");
            return;
        }
        isRunning = true;
        this.countdown();
        timerIntervalFunction = setInterval(this.countdown, countdownIntervalMS);
    }

    stop (){
        if(!isRunning){
            utils.log(TAG, "Timer is not running");
            return;
        }
        clearInterval(timerIntervalFunction);
        isRunning = false;
    }

    reset (){
        this.stop();
        timeInSeconds = 0;
    }

    getCurrentTime(){
        return timeInSeconds;
    }

    isRunning(){
        return isRunning;
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