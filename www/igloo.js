var exec = require('cordova/exec');

module.exports = {
  connect: function(bluetoothId, success, error) {
    exec(success, error, "IglooPlugin", "connect", [bluetoothId]);
  },
  unlockWithPin: function(data, success, error) {
    exec(success, error, "IglooPlugin", "unlockWithPin", [data]);
  },
  readLogs: function(data, success, error) {
    exec(success, error, "IglooPlugin", "readLogs", [data]);
  },
  disconnect: function(success, error) {
    exec(success, error, "IglooPlugin", "disconnect", []);
  }
};
