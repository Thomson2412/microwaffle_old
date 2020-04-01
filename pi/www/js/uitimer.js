const countdownIntervalMS = 1000;
let timerIntervalFunction = null;
let timeInSeconds = 0;
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
        if(isRunning){
            console.log("Timer is already running");
            return;
        }
        timeInSeconds = ts;
    }

    add (ts){
        if(!Number.isInteger(ts)){
            console.log("Timer needs a number in seconds to be set");
            return;
        }
        timeInSeconds += ts;
    }

    subtract (ts){
        if(!Number.isInteger(ts)){
            console.log("Timer needs a number in seconds to be set");
            return;
        }
        timeInSeconds -= ts;
    }

    start (){
        if(isRunning){
            console.log("Timer is already running");
            return;
        }
        isRunning = true;
        timerIntervalFunction = setInterval(this.countdown, countdownIntervalMS);
    }

    stop (){
        if(!isRunning){
            console.log("Timer is not running");
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
        console.log("Timer remaining: " + timeInSeconds);
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