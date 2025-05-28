var exec = require('cordova/exec');

module.exports = {
    connect: function(bluetoothId, success, error) {
        exec(success, error, "IglooPlugin", "connect", [bluetoothId]);
    },
    readLogs: function(success, error) {
        exec(success, error, "IglooPlugin", "readLogs", []);
    },
    unlockWithPin: function(pin, success, error) {
        exec(success, error, "IglooPlugin", "unlockWithPin", [pin]);
    },
    disconnect: function(success, error) {
        exec(success, error, "IglooPlugin", "disconnect", []);
    }
};
