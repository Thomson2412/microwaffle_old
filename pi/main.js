const TAG = 'Main';
const server = require('./server/Server');
const utils = require('./utils/Utils');

let sv = new server();
utils.log(TAG, "Server start");
sv.start();