let timerState = {
    NOT_SET: 0,
    SET: 1,
    RUNNING: 2,
    PAUSED: 3
}

const countdownIntervalMS = 1000;
let timerIntervalFunction = null;
let state = timerState.NOT_SET
let timeInSecondsInTimer = 0;
let isRunning = false;
let timerUpdateCallback = null;
let timerDoneCallback = null;

class UITimer {

    constructor(tdcb, tucb) {
        if(typeof tdcb != "function"){
            console.log("Timer done callback not set");
        }
        if(typeof tucb != "function"){
            console.log("Timer update callback not set");
        }
        timerUpdateCallback = tucb;
        timerDoneCallback = tdcb;
    }

    set (ts) {
        if(!Number.isInteger(ts)){
            console.log("Timer needs a number in seconds to be set");
            return;
        }
        if(state === timerState.RUNNING || state === timerState.PAUSED){
            console.log("Timer is already running or paused");
            return;
        }

        timeInSecondsInTimer = ts;
        state = timerState.SET
    }

    add (ts){
        if(!Number.isInteger(ts)){
            console.log("Timer needs a number in seconds to be set");
            return;
        }
        timeInSecondsInTimer += ts;
    }

    start (){
        if(state === timerState.RUNNING){
            console.log("Timer is already running");
            return;
        }
        if(state === timerState.NOT_SET){
            console.log("Timer is not set");
            return;
        }
        state = timerState.RUNNING;
        this.countdown();
        timerIntervalFunction = setInterval(this.countdown, countdownIntervalMS);
    }

    pause (){
        if(state !== timerState.RUNNING){
            console.log("Timer is not running");
            return;
        }
        clearInterval(timerIntervalFunction);
        state = timerState.PAUSED
    }

    reset (){
        clearInterval(timerIntervalFunction);
        timeInSecondsInTimer = 0;
        state = timerState.NOT_SET
    }

    getCurrentTime(){
        return timeInSecondsInTimer;
    }

    getState(){
        return state;
    }

    countdown() {
        timeInSecondsInTimer--;
        console.log("Timer remaining: " + timeInSecondsInTimer);
        if(typeof timerUpdateCallback == "function"){
            timerUpdateCallback(timeInSecondsInTimer);
        }
        if(timeInSecondsInTimer <= 0){
            timeInSecondsInTimer = 0;
            clearInterval(timerIntervalFunction);
            isRunning = false;
            if(typeof timerDoneCallback == "function"){
                timerDoneCallback();
            }
        }
    }
}