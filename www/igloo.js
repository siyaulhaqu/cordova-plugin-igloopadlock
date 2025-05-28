var exec = require('cordova/exec');

var IglooPlugin = {
  /**
   * Connect to the lock with given Bluetooth ID.
   * @param {string} bluetoothId - The Bluetooth MAC address of the lock.
   * @param {function} success - Callback when connected.
   * @param {function} error - Callback on error.
   */
  connect: function (bluetoothId, success, error) {
    exec(success, error, 'IglooPlugin', 'connect', [bluetoothId]);
  },

  /**
   * Unlock the lock using unlock credentials (eKey).
   * @param {object} data - The unlock data including pin, aesKey, lockData, lockVersion, timestamp.
   * @param {function} success - Callback when unlocked successfully.
   * @param {function} error - Callback on failure.
   */
  unlockWithPin: function (data, success, error) {
    exec(success, error, 'IglooPlugin', 'unlockWithPin', [data]);
  },

  /**
   * Read the activity logs from the lock.
   * @param {object} data - The log request data including aesKey, lockData, lockVersion, timestamp.
   * @param {function} success - Callback with logs as array.
   * @param {function} error - Callback on failure.
   */
  readLogs: function (data, success, error) {
    exec(success, error, 'IglooPlugin', 'readLogs', [data]);
  },

  /**
   * Disconnect from the lock.
   * @param {function} success - Callback when disconnected.
   * @param {function} error - Callback on error.
   */
  disconnect: function (success, error) {
    exec(success, error, 'IglooPlugin', 'disconnect', []);
  }
};

module.exports = IglooPlugin;
