const minuteStepping = 60;
const secondStepping = 10;
const maxSeconds = 60 * 99;

let timeInSeconds = 0;
let working = true;
let cm;
let am;
let tm;

let hardwareState = {
    IDLE: 0,
    RUNNING: 1,
    PAUSE: 2
}

$( function() {
    am = new AudioManager();
    cm = new CommunicationManager(io, statusUpdateCallback);
    tm = new UITimer(onTimerDone, onTimerTick);

    $( "#startButton" ).on("click", function() {
        if(timeInSeconds > 0){
            cm.start(timeInSeconds);
            am.play("loop");
            tm.set(timeInSeconds);
            tm.start();
        }
        am.play("up");
    });

    $( "#stopButton" ).on("click", function() {
        cm.stop();
        am.play("down");
        am.stop("loop");
        am.stop("alarm");
        tm.reset();
        working = false;
    });

    $( "#plusMinute" ).on("click", function() {
        if(timeInSeconds + minuteStepping < maxSeconds) {
            timeInSeconds += minuteStepping;
            am.play("up");
            updateTimerUI();
        }
    });

    $( "#plusSecond" ).on("click", function() {
        if(timeInSeconds + secondStepping < maxSeconds) {
            timeInSeconds += secondStepping;
            am.play("up");
            updateTimerUI();
        }
    });

    $( "#minusMinute" ).on("click", function() {
        if(timeInSeconds - minuteStepping >= 0) {
            timeInSeconds -= minuteStepping;
            am.play("down");
            updateTimerUI();
        }
    });

    $( "#minusSecond" ).on("click", function() {
        if(timeInSeconds - secondStepping >= 0) {
            timeInSeconds -= secondStepping
            am.play("down");
            updateTimerUI();
        }
    });

    am.init([
        {"filename": "up.mp3", "loop": false, "volume": 1},
        {"filename": "down.mp3", "loop": false, "volume": 1},
        {"filename": "alarm.mp3", "loop": true, "volume": 1},
        {"filename": "loop.mp3", "loop": true, "volume": 0.5},
    ]);
});

function onTimerTick(tis){
    timeInSeconds = tis;
    am.play("down");
    updateTimerUI()
}

function onTimerDone(){
    am.stop("loop");
    am.play("alarm");
}

function updateTimerUI(){
    let min = Math.floor(timeInSeconds / 60);
    let sec = timeInSeconds - (min * 60);

    let timeString =
        min.toLocaleString(undefined, {minimumIntegerDigits: 2}) +
        ":" +
        sec.toLocaleString(undefined, {minimumIntegerDigits: 2});

    $("#timer").text(timeString);
}

function updateUI(){
    $("#timer-container").children().prop('disabled', working);
}

function statusUpdateCallback(status) {
    if(Math.abs(status.timeInSeconds - timeInSeconds) > 2)
        timeInSeconds = status.timeInSeconds;
    working = status.state === hardwareState.RUNNING;

    updateTimerUI();
    updateUI();
}