class CommunicationManager{

    constructor(io, callback) {
        this.socket = io();

        this.socket.on("statusUpdate", function (json) {
            callback(json);
        });
    }

    start(timeInSeconds) {
        this.socket.emit("start", timeInSeconds);
    }

    stop() {
        this.socket.emit("stop");
    }

    getStatus(){
        this.socket.emit("getStatus");
    }
}