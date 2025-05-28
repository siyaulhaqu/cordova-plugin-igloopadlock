package com.igloo.padlock;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;

import com.igloohome.ble.lock.BleManager;
import com.igloohome.ble.lock.base.BaseIglooLock;
import com.igloohome.ble.lock.model.IglooPadLock;
import com.igloohome.ble.lock.callback.BleScanCallback;
import com.igloohome.ble.lock.callback.ConnectionCallback;
import com.igloohome.ble.lock.callback.UnlockCallback;
import com.igloohome.ble.lock.callback.ActivityLogCallback;
import com.igloohome.ble.lock.request.EKeyRequest;
import com.igloohome.ble.lock.request.ActivityLogRequest;
import com.igloohome.ble.lock.model.LogEntry;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class IglooPlugin extends CordovaPlugin {

    private BleManager bleManager;
    private IglooPadLock connectedLock;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        bleManager = BleManager.getInstance(cordova.getContext().getApplicationContext());
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Context context = cordova.getActivity();

        switch (action) {
            case "connect":
                String bluetoothId = args.getString(0);
                connectToLock(bluetoothId, context, callbackContext);
                return true;

            case "unlockWithPin":
                JSONObject unlockData = args.getJSONObject(0);
                unlockWithPin(unlockData, context, callbackContext);
                return true;

            case "readLogs":
                JSONObject logData = args.getJSONObject(0);
                readLogs(logData, context, callbackContext);
                return true;

            case "disconnect":
                disconnect(callbackContext);
                return true;

            default:
                return false;
        }
    }

    private void connectToLock(final String bluetoothId, final Context context, final CallbackContext callbackContext) {
        bleManager.scanForLocks(context, new BleScanCallback() {
            @Override
            public void onLockFound(BaseIglooLock lock) {
                if (lock instanceof IglooPadLock && lock.getBluetoothDevice().getAddress().equalsIgnoreCase(bluetoothId)) {
                    connectedLock = (IglooPadLock) lock;
                    connectedLock.connect(context, new ConnectionCallback() {
                        @Override
                        public void onConnected() {
                            callbackContext.success("Connected to lock.");
                        }

                        @Override
                        public void onDisconnected() {
                            // Optional: handle disconnect feedback
                        }

                        @Override
                        public void onConnectionFailed(String error) {
                            callbackContext.error("Connection failed: " + error);
                        }
                    });
                }
            }

            @Override
            public void onScanFailed(String error) {
                callbackContext.error("Scan failed: " + error);
            }
        });
    }

    private void unlockWithPin(JSONObject data, Context context, final CallbackContext callbackContext) throws JSONException {
        if (connectedLock == null) {
            callbackContext.error("Lock not connected.");
            return;
        }

        EKeyRequest request = new EKeyRequest(
            data.getString("pin"),
            data.getString("lockData"),
            data.getString("aesKey"),
            data.getString("lockVersion"),
            data.getString("timestamp")
        );

        connectedLock.unlockWithEKey(request, new UnlockCallback() {
            @Override
            public void onUnlockSuccess() {
                callbackContext.success("Unlock successful.");
            }

            @Override
            public void onUnlockFailed(String error) {
                callbackContext.error("Unlock failed: " + error);
            }
        });
    }

    private void readLogs(JSONObject data, Context context, final CallbackContext callbackContext) throws JSONException {
        if (connectedLock == null) {
            callbackContext.error("Lock not connected.");
            return;
        }

        ActivityLogRequest logRequest = new ActivityLogRequest(
            data.getString("lockData"),
            data.getString("aesKey"),
            data.getString("lockVersion"),
            data.getString("timestamp")
        );

        connectedLock.getActivityLogs(logRequest, new ActivityLogCallback() {
            @Override
            public void onActivityLogReceived(List<LogEntry> logs) {
                JSONArray result = new JSONArray();
                for (LogEntry entry : logs) {
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("type", entry.getType());
                        obj.put("timestamp", entry.getTimestamp());
                    } catch (JSONException e) {
                        // Skip malformed entry
                    }
                    result.put(obj);
                }
                callbackContext.success(result);
            }

            @Override
            public void onFailed(String error) {
                callbackContext.error("Failed to retrieve logs: " + error);
            }
        });
    }

    private void disconnect(final CallbackContext callbackContext) {
        if (connectedLock != null) {
            connectedLock.disconnect();
            connectedLock = null;
        }
        callbackContext.success("Disconnected.");
    }
}
