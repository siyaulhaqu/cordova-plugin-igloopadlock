package com.igloo.padlock;

import android.util.Log;
import android.content.Context;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

import co.igloohome.ble.lock.BleManager;
import co.igloohome.ble.lock.IglooLock;
import co.igloohome.ble.lock.callbacks.LockCallback;
import co.igloohome.ble.lock.model.ActivityLog;

import java.util.List;

public class IglooPlugin extends CordovaPlugin {

    private BleManager bleManager;
    private IglooLock currentLock;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Context context = cordova.getActivity().getApplicationContext();
        bleManager = new BleManager(context);
        bleManager.setDebug(true);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        switch (action) {
            case "connect":
                String bluetoothId = args.getString(0);
                connect(bluetoothId, callbackContext);
                return true;
            case "readLogs":
                readLogs(callbackContext);
                return true;
            case "unlockWithPin":
                String pin = args.getString(0);
                unlockWithPin(pin, callbackContext);
                return true;
            case "disconnect":
                disconnect(callbackContext);
                return true;
            default:
                return false;
        }
    }

    private void connect(String bluetoothId, CallbackContext callbackContext) {
        currentLock = new IglooLock(bluetoothId);

        bleManager.connect(currentLock, new LockCallback() {
            @Override
            public void onSuccess() {
                Log.d("IglooPlugin", "Connected to lock: " + bluetoothId);
                callbackContext.success("Connected to " + bluetoothId);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e("IglooPlugin", "Failed to connect: " + throwable.getMessage());
                callbackContext.error("Connection failed: " + throwable.getMessage());
            }
        });
    }

    private void readLogs(CallbackContext callbackContext) {
        if (currentLock == null) {
            callbackContext.error("No lock connected.");
            return;
        }

        currentLock.readActivityLogs(new co.igloohome.ble.lock.callbacks.ActivityLogCallback() {
            @Override
            public void onSuccess(List<ActivityLog> logs) {
                JSONArray logArray = new JSONArray();
                for (ActivityLog log : logs) {
                    logArray.put(log.toString()); // You may format this as needed
                }
                callbackContext.success(logArray);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e("IglooPlugin", "Failed to read logs: " + throwable.getMessage());
                callbackContext.error("Log read failed: " + throwable.getMessage());
            }
        });
    }

    private void unlockWithPin(String pin, CallbackContext callbackContext) {
        if (currentLock == null) {
            callbackContext.error("No lock connected.");
            return;
        }

        currentLock.unlockWithPin(pin, new LockCallback() {
            @Override
            public void onSuccess() {
                Log.d("IglooPlugin", "Unlocked with PIN.");
                callbackContext.success("Unlocked with PIN.");
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e("IglooPlugin", "Unlock failed: " + throwable.getMessage());
                callbackContext.error("Unlock failed: " + throwable.getMessage());
            }
        });
    }

    private void disconnect(CallbackContext callbackContext) {
        if (currentLock != null) {
            bleManager.disconnect(currentLock);
            currentLock = null;
            callbackContext.success("Disconnected.");
        } else {
            callbackContext.error("No lock connected.");
        }
    }
}
