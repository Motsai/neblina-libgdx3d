package com.badlogic.invaders.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GattBroadcastReceiver extends BroadcastReceiver {
    public GattBroadcastReceiver() {
    }

    // Handles various events fired by the Service.
// ACTION_GATT_CONNECTED: connected to a GATT server.
// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
// ACTION_DATA_AVAILABLE: received data from the device. This can be a
// result of read or notification operations.
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if(BLEDeviceScanActivity.debug_mode1==true) {
            Log.w("BLUETOOTH DEBUG", "You are in BroadcastReceiver's onReceive: " + action);
        }
        if (BLEDeviceScanActivity.ACTION_GATT_CONNECTED.equals(action)) {
//            mConnected = true;
            if(BLEDeviceScanActivity.debug_mode1==true) {
                Log.w("BLUETOOTH DEBUG", "The intent action is ACTION_GATT_CONNECTED");
            }
            // updateConnectionState(R.string.connected); //commenting out so it compiles
//            invalidateOptionsMenu();
        } else if (BLEDeviceScanActivity.ACTION_GATT_DISCONNECTED.equals(action)) {
//            mConnected = false;
            if(BLEDeviceScanActivity.debug_mode1==true) {
                Log.w("BLUETOOTH DEBUG", "The intent action is ACTION_GATT_DISCONNECTED");
            }
//                updateConnectionState(R.string.disconnected);//commenting out so it compiles
//            invalidateOptionsMenu();
//                clearUI();//commenting out so it compiles
        } else if (BLEDeviceScanActivity.
                ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            if(BLEDeviceScanActivity.debug_mode1==true) {
                Log.w("BLUETOOTH DEBUG", "The intent action is ACTION_GATT_SERVICES_DISCOVERED");
            }
            // Show all the supported services and characteristics on the
            // user interface.
//                displayGattServices(mBluetoothLeService.getSupportedGattServices());//commenting out so it compiles
        } else if (BLEDeviceScanActivity.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BLEDeviceScanActivity.EXTRA_DATA));//commenting out so it compiles
            if(BLEDeviceScanActivity.debug_mode1==true) {
                Log.w("BLUETOOTH DEBUG", "The intent action is ACTION_DATA_AVAILABLE");
            }
        }
    }

    private void displayData(String stringExtra) {
        if(BLEDeviceScanActivity.debug_mode1==true) {
            Log.w("BLUETOOTH DEBUG", "Displaying the Data");
        }
    }
}
