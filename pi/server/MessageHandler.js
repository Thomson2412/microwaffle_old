const TAG = 'MessageHandler';
const utils = require('../utils/Utils');
const hardwareController = require('../hardware/HardwareController');

module.exports = class MessageHandler {
    constructor(io){
        hardwareController.init();

        io.on("connection", function(socket){

            utils.log(TAG, "a user connected");
            socket.emit("statusUpdate", hardwareController.getStatus());

            hardwareController.addStatusUpdateCallback(socket.id,
                function (status) {
                    socket.emit("statusUpdate", status);
                });

            socket.on("disconnect", function(){
                utils.log(TAG, "user disconnected");
                hardwareController.removeStatusUpdateCallback(socket.id);
            });


            socket.on("start", function(timeInSeconds){
                utils.log(TAG, "start: " + timeInSeconds);
                if(timeInSeconds > 0){
                    hardwareController.start(timeInSeconds);
                }
            });

            socket.on("pause", function(){
                utils.log(TAG, "pause");
                hardwareController.pause();
            });

            socket.on("stop", function(){
                utils.log(TAG, "stop");
                hardwareController.stop();
            });

            socket.on("getStatus", function(){
                let status = hardwareController.getStatus();
                utils.log(TAG, "getStatus: " + JSON.stringify(status));
                socket.emit("statusUpdate", status);
            });
        });
    }
}