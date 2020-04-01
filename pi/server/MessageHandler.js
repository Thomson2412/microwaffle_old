const TAG = 'MessageHandler';
const utils = require('../utils/Utils');
const timer = require('../utils/Timer');
const hardwareController = require('../hardware/HardwareController');

module.exports = class MessageHandler {
    constructor(io){
        hardwareController.init();

        io.on("connection", function(socket){

            utils.log(TAG, "a user connected");
            socket.emit("statusUpdate", hardwareController.getStatus());
            let cbf =
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
                utils.log(TAG, "typeof msg:" + typeof timeInSeconds);
                if(timeInSeconds > 0){
                    hardwareController.start(timeInSeconds);
                }
            });

            socket.on("stop", function(){
                utils.log(TAG, "stop");
                hardwareController.stop();
            });

            socket.on("getStatus", function(){
                utils.log(TAG, "stop");
                socket.emit("statusUpdate", hardwareController.getStatus());
            });
        });
    }
}