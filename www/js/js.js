var timerValues = [0, 0]
var minuteStepping = 1;
var secondStepping = 10;
var statusTimeout;
var working = true;
var am;

$( document ).ready(function() {
    $( "#startButton" ).click(function() {
        timeInSeconds = 60 * timerValues[0] + timerValues[1];
        if(statusTimeout){
            clearTimeout(statusTimeout);
        }
        statusTimeout = setInterval(getStatus, 500);
        $.get( "/start?seconds=" + timeInSeconds);
        getStatus();
        if(timerValues[0] > 0 || timerValues[1] > 0){
            am.play("loop");
        }
        am.play("up");
    });
    $( "#stopButton" ).click(function() {
        $.get( "/stop");
        if(statusTimeout){
            clearTimeout(statusTimeout);
            statusTimeout = undefined;
        }
        getStatus();
        am.play("down");
        //am.stop("loop");
    });

    $( "#plusMinute" ).click(function() {
        min = timerValues[0];
        if(min < 99){
            timerValues[0] += minuteStepping;
        }
        am.play("up");
        updateTimerUI();
    });
    $( "#plusSecond" ).click(function() {
        min = timerValues[0];
        sec = timerValues[1];
        if(sec < 60 && min < 99){
            timerValues[1] += secondStepping;
        }
        if(sec + secondStepping >= 60){
            if(min < 99){
                timerValues[0] += minuteStepping;
            }
            timerValues[1] = 0;
        }
        am.play("up");
        updateTimerUI();
    });
    $( "#minusMinute" ).click(function() {
        min = timerValues[0];
        if(min > 0){
            timerValues[0] -= 1;
        }
        am.play("down");
        updateTimerUI();
    });
    $( "#minusSecond" ).click(function() {
        min = timerValues[0];
        sec = timerValues[1];
        if(min > 0 || sec > 0){
            if(sec > 0){
                timerValues[1] -= secondStepping;
            }
            if(sec - secondStepping < 0){
                if(min > 0){
                    timerValues[0] -= minuteStepping;
                }
                timerValues[1] = 60 - secondStepping;
            }
        }
        am.play("down");
        updateTimerUI();
    });
    am = new AudioManager();
    am.init([
        {"filename": "up.mp3", "loop": false, "volume": true},
        {"filename": "down.mp3", "loop": false, "volume": true},
        {"filename": "alarm.mp3", "loop": false, "volume": true},
        {"filename": "loop.mp3", "loop": false, "volume": true},
    ]);
    getStatus();
});

function updateTimerUI(){
    var timeString = timerValues[0].toLocaleString(undefined, {minimumIntegerDigits: 2}) + ":" + timerValues[1].toLocaleString(undefined, {minimumIntegerDigits: 2});
    $("#timer").text(timeString);
}

function updateUI(){
    $("#timer-container").children().prop('disabled', working);
}

function getStatus(){
    $.getJSON( "/status", processStatus);
}

function processStatus(data){
    working = data.busy;

    timeUpdate = parseInt(data.timer);

    min = Math.trunc(timeUpdate / 60)
    sec = timeUpdate - min * 60

    if(timeUpdate < (timerValues[0] + timerValues[1])){
        am.play("down");
    }

    timerValues[0] = min;
    timerValues[1] = sec;

    if(min + sec <= 0){
        if(statusTimeout){
            clearTimeout(statusTimeout);
            statusTimeout = undefined;
            am.play("alarm");
            //am.stop("loop");
        }
    }

    updateUI();
    updateTimerUI();
}


//function updateTimerValues(){
//    timerValues[0];
//    timerValues[1] -= 1;
//    if(timerValues[0] <= 0 && timerValues[1] <= 0){
//        //timerDone
//        clearTimeout(timerTimeout);
//        updateTimerUI();
//        return;
//    }
//    if(timerValues[1] < 0){
//        timerValues[1] = 59;
//        timerValues[0] -= 1;
//    }
//    if(timerTimeout){
//        timerTimeout = setTimeout(updateTimerValues, 1000);
//    }
//    updateTimerUI();
//}