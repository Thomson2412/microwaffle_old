const TAG = 'Server';
const config = require('../config.json');
const utils = require('../utils/Utils');
const messageHandler = require('../server/MessageHandler');
const express = require('express');
const app = express();
const http = require('http').Server(app);
const io = require('socket.io')(http);

module.exports = class Server {
    start(){
        app.use(express.static(config.server.web_dir));
        http.listen(config.server.node_port, function(){
            utils.log(TAG,"listening on *:" + config.server.node_port);
            new messageHandler(io);
        });
    }
}
