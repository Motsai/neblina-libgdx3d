package com.badlogic.invaders.android;

/**
 * Created by scott on 2016-06-30.
 */

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Arrays;

public class NebDeviceDetailFragment extends Fragment implements NeblinaDelegate {

     //The fragment argument representing the item ID that this fragment represents.
    public static final String ARG_ITEM_ID = "item_id";

    //The dummy content this fragment is presenting.
    private Neblina mItem;

    //Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes)
    public static float latest_Q0 = 0.0f;
    public static float latest_Q1 = 0.0f;
    public static float latest_Q2 = 0.0f;
    public static float latest_Q3 = 0.0f;
    public static String Q0_string = "";
    public static String Q1_string = "";
    public static String Q2_string = "";
    public static String Q3_string = "";
    public static long timestamp_N =0;

    public static TextView q1_text;
    public static TextView q2_text;
    public static TextView q3_text;
    public static TextView q4_text;

    public NebDeviceDetailFragment() {
    }

    public void SetItem(Neblina item) {

        mItem = item;
        mItem.SetDelegate(this);
//        mItem.Connect(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = (Neblina) getArguments().getParcelable(ARG_ITEM_ID);
            mItem.SetDelegate(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.ble_scan_activity, container, true);//Not sure why we need root view
        q1_text = (TextView)getActivity().findViewById(R.id.Q1_TEXT);
        q2_text = (TextView)getActivity().findViewById(R.id.Q2_TEXT);
        q3_text = (TextView)getActivity().findViewById(R.id.Q3_TEXT);
        q4_text = (TextView)getActivity().findViewById(R.id.Q4_TEXT);

        q1_text.setText("Hello Earthling!");
        return rootView;
    }


    public void initializeNeblina() {
        //By default start streaming quaternions
        mItem.streamQuaternion(true);
        BLEDeviceScanActivity.is_QUATERNION_BUTTON_on = true;

        //Set the quaternion button to on so the user can see that quaternions are streaming
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.w("BLUETOOTH_DEBUG", "Setting the button checked!");
              ToggleButton toggleButton = (ToggleButton)getActivity().findViewById(R.id.QUATERNION_BUTTON);
                toggleButton.setChecked(true);
            }
        });
    }

    public void didReceiveRSSI(int rssi) {

    }

    public void didReceiveFusionData(int type , byte[] data, boolean errFlag) {
        switch (type) {
            case Neblina.MOTION_CMD_QUATERNION:

                //Puts the characteristic values into the intent
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    for (byte byteChar : data)
                        stringBuilder.append(String.format("%02X ", byteChar));
                }

                //TODO: Fix timestamping
//            timestamp_N = (timestamp[3]&0xff)<<24 | (timestamp[2]&0xff)<<16 | (timestamp[1]&0xff)<<8 | (timestamp[0]&0xff)<<0;
                //TODO: Fix and Test Sending Data To The Cloud
//          sendQuaternionsToCloudRESTfully(Q0_string, Q1_string, Q2_string, Q3_string); //The pitcher works, the catcher fails
//            new getAWSID().execute("gogogo!"); //Uses the AWS Android SDK -> Seems to work

                //Unwrap Data Based on Motsai's Neblina Protocol
                if (data.length == 16) {
                    //Plus 1 is to remind me that the end of the range is non-inclusive
                    //Minus 4 since the header and timestamp are chopped off
//                    final byte[] header = Arrays.copyOfRange(data, 0, 3 + 1); //Bytes 0-3 are the header
//                    final byte[] timestamp = Arrays.copyOfRange(data, 4, 7 + 1); //Bytes 4-7 are the timestamp
                    final byte[] q0 = Arrays.copyOfRange(data, 8-4, 9-4 + 1); // Bytes 8-9 are Q0 value
                    final byte[] q1 = Arrays.copyOfRange(data, 10-4, 11-4 + 1); // Bytes 10-11 are Q1 value
                    final byte[] q2 = Arrays.copyOfRange(data, 12-4, 13-4 + 1); // Bytes 12-13 are Q2 value
                    final byte[] q3 = Arrays.copyOfRange(data, 14-4, 15-4 + 1); // Bytes 14-15 are Q3 value
                    final byte[] reserved = Arrays.copyOfRange(data, 16-4, 19-4 + 1); // Bytes 16-19 are reserved

                    //Convert to big endian
                    latest_Q0 = normalizedQ(q0);
                    latest_Q1 = normalizedQ(q1);
                    latest_Q2 = normalizedQ(q2);
                    latest_Q3 = normalizedQ(q3);

                    //Create a string version
                    Q0_string = String.valueOf(latest_Q0);
                    Q1_string = String.valueOf(latest_Q1);
                    Q2_string = String.valueOf(latest_Q2);
                    Q3_string = String.valueOf(latest_Q3);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            q1_text.setText(Q0_string);
                            q2_text.setText(Q1_string);
                            q3_text.setText(Q2_string);
                            q4_text.setText(Q3_string);
                        }
                    });

                break;
        }
    }
    }

    public void didReceiveDebugData(int type, byte[] data, boolean errFlag) {

    }
    public void didReceivePmgntData(int type, byte[] data, boolean errFlag) {

    }
    public void didReceiveStorageData(int type, byte[] data, boolean errFlag) {
        BLEDeviceScanActivity.playbackNumber = 0;    //TODO: Get the sessionID from the storageData and feed it to Flash Playback button

    }
    public void didReceiveEepromData(int type, byte[] data, boolean errFlag) {

    }
    public void didReceiveLedData(int type, byte[] data, boolean errFlag) {

    }

    private float normalizedQ(byte[] q) {
        if(q.length==2){
            int val = ((q[1]&0xff)<<8)|(q[0]&0xff); //concatenate the byte array into an int
            float normalized = (float) val / 32768; //normalize by dividing by 2^15
            if (normalized > 1.0) normalized = normalized-2;
            return normalized;
        }else return -1;
    }
}
