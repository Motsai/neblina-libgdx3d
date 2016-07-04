package com.badlogic.invaders.android;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class BLEDeviceScanActivity extends ListActivity {


    private static final int REQUEST_ENABLE_BT = 0;
//    private BluetoothAdapter mBluetoothAdapter;
//    private Handler mHandler;
    private List<String> mDeviceNameList;
    private ArrayAdapter<String> mLeDeviceListAdapter;
    private static final long SCAN_PERIOD = 60000;
    private final Map<String,Neblina> mDeviceList = new HashMap<String,Neblina>();
    private Neblina activeDevice;
    private NebDeviceDetailFragment activeDeviceDelegate;
//    private BluetoothGatt mBluetoothGatt;
    private boolean mBluetoothGatt;
    private boolean say_once = true;
    private int periodic_print = 0;
    public static boolean debug_mode1 = true;
    public static boolean debug_mode2 = true;
    public String identityID = "";

    //GATT CALLBACK VARIABLES
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    public final static String ACTION_GATT_CONNECTED = "com.inspirationindustry.motsaibluetooth.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.inspirationindustry.motsaibluetooth.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.inspirationindustry.motsaibluetooth.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.inspirationindustry.motsaibluetooth.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.inspirationindustry.motsaibluetooth.EXTRA_DATA";
    private final static String TAG = BLEDeviceScanActivity.class.getSimpleName();
    private int mConnectionState = STATE_DISCONNECTED;
    public static final String ACTION_DATA_WRITE = "android.ble.common.ACTION_DATA_WRITE";
    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("text/x-markdown; charset=utf-8");
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    //NEBLINA CUSTOM UUIDs
    public static final UUID[] SCAN_UUID = {Neblina.NEB_SERVICE_UUID,};// UUID.fromString("0df9f021-1532-11e5-8960-0002a5d5c51b"), };
    public static final UUID NEB_SERVICE_UUID = UUID.fromString("0df9f021-1532-11e5-8960-0002a5d5c51b");
    public static final UUID NEB_DATACHAR_UUID = UUID.fromString("0df9f022-1532-11e5-8960-0002a5d5c51b");
    public static final UUID NEB_CTRLCHAR_UUID = UUID.fromString("0df9f023-1532-11e5-8960-0002a5d5c51b");

    public static final byte NEB_CTRL_PKTYPE_CMD = 2;
    public static final byte NEB_CTRL_SUBSYS_MOTION_ENG = 1;
    private boolean mTwoPane;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private Context context;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_scan_activity);
        ButterKnife.inject(this);
        activateBLE();
        initializeVariables();
        scanLeDevice(true);
    }

    public void activateBLE() {

        activeDeviceDelegate = new NebDeviceDetailFragment();
        //This should pass
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish(); //optional kill switch
        }

        //Get the Bluetooth Adapter
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();


        //Enable Bluetooth if required
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void initializeVariables() {
        mDeviceNameList = new ArrayList<String>();
//        mDeviceList = new ArrayList<BluetoothDevice>();
//        mDeviceList = new ArrayList<String,Neblina>();
        mLeDeviceListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mDeviceNameList);
        setListAdapter(mLeDeviceListAdapter);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            //stops scanning after a pre-defined period
            mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(debug_mode1==true) {
                        Log.w("BLUETOOTH DEBUG", "ending the scan!");
                    }
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);
            if(debug_mode1==true) {
                Log.w("BLUETOOTH DEBUG", "starting the scan!");
            }
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            if(debug_mode1==true) {
                Log.w("BLUETOOTH DEBUG", "ending the scan!");
            }
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }


    @Override
    public void onRestart(){
        super.onRestart();
        if(debug_mode1==true) {
            Log.w("BLUETOOTH_DEBUG", "onRestart!");
        }
        if(mConnectionState==STATE_CONNECTED) {
//            mBluetoothGatt.disconnect(); //pre-neblina class
//            mBluetoothGatt.close();//pre-neblina class
        }
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        activeDevice = mDeviceList.get(l.getItemAtPosition(position).toString());
        mBluetoothGatt = activeDevice.Connect(getBaseContext());

        Bundle arguments = new Bundle();
        arguments.putParcelable(NebDeviceDetailFragment.ARG_ITEM_ID, activeDevice);
        activeDeviceDelegate.SetItem(activeDevice);
        activeDeviceDelegate.SetContext(this);
        activeDeviceDelegate.setArguments(arguments);

        //Perform Null Checks
        if (this.getFragmentManager()==null){
            Log.w("BLUETOOTH_DEBUG", "Fragment Manager is NULL!");
        }else
        if (this.getFragmentManager().beginTransaction()==null){
            Log.w("BLUETOOTH_DEBUG", "Fragment Transaction is NULL!");
        }else
        if(activeDeviceDelegate==null){
            Log.w("BLUETOOTH_DEBUG", "Fragment is NULL!");
        }else {

            //Checks pass so build the fragment
            this.getFragmentManager().beginTransaction()
                    .add(activeDeviceDelegate, "Fun")
                    .commit();

        }

        //Create Toast Message
        String clicked_device = activeDevice.toString();
        Toast.makeText(this, "Connecting to " + clicked_device, Toast.LENGTH_LONG).show();
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback(){
                @Override
            public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //Using Hoan's Neblina Classes
                            int i = 0;
                            long deviceID = 0;

                            // Try to find our device ID
                            while (i < scanRecord.length && scanRecord[i] > 0) {
                                if (scanRecord[i] > 0) {
                                    if (scanRecord[i + 1] == -1) {
                                        ByteBuffer x = ByteBuffer.wrap(scanRecord, i + 4, 8);
                                        x.order(ByteOrder.LITTLE_ENDIAN);
                                        deviceID = x.getLong();
                                        break;
                                    }
                                    i += scanRecord[i] + 1;
                                }
                            }

                            if(device.getName() != null){

                                Neblina neblina = new Neblina(deviceID,device);
                                if (mDeviceList.containsKey(neblina.toString()) == false) {
                                    //Need to set the ID so that we can reference it when the list item is clicked
//                                    mLeDeviceListAdapter.add(Long.toString(deviceID));
                                    mLeDeviceListAdapter.add(neblina.toString());
                                    mLeDeviceListAdapter.notifyDataSetChanged();
                                    mDeviceList.put(neblina.toString(), neblina);
                                    Log.w("BLUETOOTH DEBUG", "Item added neblina.toString " + neblina.toString());
                                    Log.w("BLUETOOTH DEBUG", "Item added Long.toString(deviceID) " + Long.toString(deviceID));
                                }
                            }
                        }
                    });
                }
            };


    //ALL THE CALLBACKS FOR ACTIVE BLE OPERATION
//    private final BluetoothGattCallback mGattCallback =
//            new BluetoothGattCallback() {
//
//                //CALLED WHEN CONNECTION STATE CHANGES
//                @Override
//                public void onConnectionStateChange(BluetoothGatt gatt, int status,
//                                                    int newState) {
//                    String intentAction;
//                    if (newState == BluetoothProfile.STATE_CONNECTED) {
//                        intentAction = ACTION_GATT_CONNECTED;
//                        mConnectionState = STATE_CONNECTED;
//                        broadcastUpdate(intentAction);
//                        boolean result= mBluetoothGatt.discoverServices();
//                        if(debug_mode1==true) {
//                            Log.w(TAG, "Connected to Gatt server and Starting discovery: " +
//                                    result);
//                        }
//
//                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                        intentAction = ACTION_GATT_DISCONNECTED;
//                        mConnectionState = STATE_DISCONNECTED;
//                        if(debug_mode1==true) {
//                            Log.i(TAG, "Disconnected from GATT server.");
//                        }
//                        broadcastUpdate(intentAction);
//                    }
//                }
//
//                //CALLED WHEN NEW SERVICES ARE DISCOVERED
//                @Override
//                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//
//                    //Broadcast the discovery of BLE services
//                    if (status == BluetoothGatt.GATT_SUCCESS) {
//                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
//                    } else {
//                        if(debug_mode1==true) {
//                            Log.w(TAG, "onServicesDiscovered received: " + status);
//                        }
//                    }
//
//                    //Get the characteristic from the discovered gatt server
//                    BluetoothGattService service = gatt.getService(NEB_SERVICE_UUID);
//                    BluetoothGattCharacteristic data_characteristic = service.getCharacteristic(NEB_DATACHAR_UUID);
//                    BluetoothGattCharacteristic ctrl_characteristic = service.getCharacteristic(NEB_CTRLCHAR_UUID);
//
//                    //Here is the code that triggers a ONE TIME read of the characteristic
////                    gatt.readCharacteristic(data_characteristic);
////                    Log.w("BLUETOOTH_DEBUG", "Data Characteristic Read Enabled");
//
//                    //Create the packet ONCE to WRITE Quaternion streaming command
//                    byte[] writeData = new byte[20];
//                    for(int i=0; i <20;i++) {
//                        writeData[i] = 0;
//                    }
//                    writeData[0] = (NEB_CTRL_PKTYPE_CMD << 5) | NEB_CTRL_SUBSYS_MOTION_ENG;
//                    writeData[1] = 16; //TODO: Replace with sizeOf(dataPortion);
//                    writeData[2] = 0;
//                    writeData[3] = 4;
//                    boolean enable = true;
//                    if (enable == true)
//                    {
//                        writeData[8] = 1;
//                    }
//                    else
//                    {
//                        writeData[8] = 0;
//                    }
//
//                    //Display the string that was built
//                    Log.w("GATT TAG", writeData.toString());
//                    if (writeData != null && writeData.length > 0) {
//                        final StringBuilder stringBuilder = new StringBuilder(writeData.length);
//                        for (byte byteChar : writeData)
//                            stringBuilder.append(String.format("%02X ", byteChar));
//                        Log.w("GATT TAG", "Hex (length=" + writeData.length + "): " + stringBuilder.toString());
//                    }
//
//
//                    //Check to see if the set worked
//                    boolean didWriteCharacteristic = ctrl_characteristic.setValue(writeData);
//                    if(!didWriteCharacteristic){
//                        Log.w("GATT TAG","Characteristic DID NOT WRITE :~( ");
//                    }
//
//                    //Write the characteristic
//                    boolean didWriteGattCharacteristic = gatt.writeCharacteristic(ctrl_characteristic);
//                    //Check to see if the write worked
//                    if(!didWriteGattCharacteristic){
//                        Log.w("GATT TAG","GATTCharacteristic FAIL :O ");
//                    }
//                }
//
//                //CALLED WHEN CHARACTERISTICS ARE READ
//                @Override
//                public void onCharacteristicRead(BluetoothGatt gatt,
//                                                 BluetoothGattCharacteristic characteristic,
//                                                 int status) {
//                    if(debug_mode1==true) {
//                        Log.w("BLUETOOTH DEBUG", "You read characteristic value = " + characteristic.getValue());
//                    }
//                    if (status == BluetoothGatt.GATT_SUCCESS) {
//                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//                    }
//                }
//
//
//                public void onCharacteristicWrite(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic, int status){
//
//                    if(debug_mode2==true) {
//                        Log.w("GATT WRITE", "GATT onCharacteristicWrite function was called. Status = " + status);
//                    }
//
//                    //Get the data_characteristic
//                    BluetoothGattService service = gatt.getService(NEB_SERVICE_UUID);
//                    BluetoothGattCharacteristic data_characteristic = service.getCharacteristic(NEB_DATACHAR_UUID);
//
//                    //Order periodic updates
//                    gatt.setCharacteristicNotification(data_characteristic, true);
//                    List<BluetoothGattDescriptor> descriptors = data_characteristic.getDescriptors();
//                    BluetoothGattDescriptor descriptor = descriptors.get(0);
//                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//
//                    if (gatt.writeDescriptor(descriptor)){
//                        if(debug_mode1==true) {
//                            Log.w("BLUETOOTH_DEBUG", "Successfully wrote descriptor, you should now receive period updates");
//                        }
//
//                    }else {
//                        if(debug_mode1==true) {
//                            Log.w("BLUETOOTH_DEBUG", "Failed to write descriptor, periodic updates should FAIL :(");
//                        }
//                    }
//
//                }
//
//
//                //CALLED WHEN SUBSCRIBED AND A NEW CHARACTERISTIC ARRIVES
//                @Override
//            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
//
//                    if(say_once==true){
//                        if(debug_mode1==true) {
//                            Log.w("BLUETOOTH DEBUG", "WOOHOO! You have started receiving periodic characteristics");
//                        }
//                        say_once=false;
//                    }
//
//                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//                }
//            };

    //BROADCAST WITHOUT CHARACTERISTIC
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        if(debug_mode1==true) {
            Log.w("BLUETOOTH DEBUG", "You are broadcasting: " + action);
        }
        sendBroadcast(intent);
    }

    //BROADCAST WITH CHARACTERISTIC
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
            Log.w("BLUETOOTH DEBUG", "You are in LONG form of onBroadcastUpdate");

        //TODO: Unwrapping utilities should be moved to the broadcast receiver functions
        final byte[] data = characteristic.getValue();


//            //TODO: Timestamp prints out as a compliment... so it appears to count down... I should fix this eventually
//            timestamp_N = (timestamp[3]&0xff)<<24 | (timestamp[2]&0xff)<<16 | (timestamp[1]&0xff)<<8 | (timestamp[0]&0xff)<<0;


            //TODO: Send Data To The Cloud
//          sendQuaternionsToCloudRESTfully(Q0_string, Q1_string, Q2_string, Q3_string); //The pitcher works, the catcher fails
//            new getAWSID().execute("gogogo!"); //Uses the AWS Android SDK -> Seems to work

            sendBroadcast(intent);
        }






    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(debug_mode1==true) {
//                Log.w("BLUETOOTH DEBUG", "You are in BroadcastReceiver's onReceive: " + action);
            }
            if (BLEDeviceScanActivity.ACTION_GATT_CONNECTED.equals(action)) {
                mConnectionState = STATE_CONNECTED;
                if(debug_mode1==true) {
                    Log.w("BLUETOOTH DEBUG", "The intent action is ACTION_GATT_CONNECTED");
                }
                // updateConnectionState(R.string.connected); //commenting out so it compiles
                invalidateOptionsMenu();
            } else if (BLEDeviceScanActivity.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnectionState = STATE_DISCONNECTED;
                if(debug_mode1==true) {
                    Log.w("BLUETOOTH DEBUG", "The intent action is ACTION_GATT_DISCONNECTED");
                }
//                updateConnectionState(R.string.disconnected);//commenting out so it compiles
                invalidateOptionsMenu();
//                clearUI();//commenting out so it compiles
            } else if (BLEDeviceScanActivity.
                    ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                if(debug_mode1==true) {
                    Log.w("BLUETOOTH DEBUG", "The intent action is ACTION_GATT_SERVICES_DISCOVERED");
                }
// displayGattServices(mBluetoothLeService.getSupportedGattServices());//commenting out so it compiles
            } else if (BLEDeviceScanActivity.ACTION_DATA_AVAILABLE.equals(action)) {
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));//commenting out so it compiles
                if(debug_mode1==true) {
                    Log.w("BLUETOOTH DEBUG", "The intent action is ACTION_DATA_AVAILABLE");
                }
            }
        }
    };


    //THESE FUNCTIONS ARE CALLED WHEN WE CLICK A BUTTON
    @OnClick(R.id.refreshButton)
    public void onRefreshButtonClick(View view){
        if(debug_mode1==true) {
            Log.w("BLUETOOTH_DEBUG", "REFRESHING!");
        }
        scanLeDevice(false);
        mDeviceList.clear();
        mDeviceNameList.clear();
        mLeDeviceListAdapter.notifyDataSetChanged();
        scanLeDevice(true);
    }

    //TODO: Write the handlers for all of these buttons
    @OnClick(R.id.BLE_BUTTON)
    public void onBLEButtonClick(View view){
            Log.w("BLUETOOTH_DEBUG", "BLE BUTTON PRESSED!");

        sendQuaternionsToCloudRESTfully("1", "1", "1", "1");

    }

    @OnClick(R.id.UART_BUTTON)
    public void onUARTButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "UART BUTTON PRESSED!");

    }

    @OnClick(R.id.QUATERNION_BUTTON)
    public void onQuaternionButtonClick(View view) {
        Log.w("BLUETOOTH_DEBUG", "QUATERNION BUTTON PRESSED!");
        activeDevice.streamQuaternion(true);
    }

    @OnClick(R.id.MAG_BUTTON)
    public void onMAGButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "MAG BUTTON PRESSED!");

    }

    @OnClick(R.id.LOCK_BUTTON)
    public void onLOCKButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "LOCK BUTTON PRESSED!");

    }

    @OnClick(R.id.ERASE_BUTTON)
    public void onERASEButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "ERASE BUTTON PRESSED!");

    }

    @OnClick(R.id.RECORD_BUTTON)
    public void onRECORDButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "RECORD BUTTON PRESSED!");

    }

    @OnClick(R.id.PLAYBACK_BUTTON)
    public void onPLAYBACKButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "PLAYBACK BUTTON PRESSED!");

    }

    @OnClick(R.id.LED0_BUTTON)
    public void onLED0ButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "LED0 BUTTON PRESSED!");

    }

    @OnClick(R.id.LED1_BUTTON)
    public void onLED1ButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "LED1 BUTTON PRESSED!");

    }

    @OnClick(R.id.EEPROM_BUTTON)
    public void onEEPROMButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "EEPROM BUTTON PRESSED!");

    }

    @OnClick(R.id.CHARGE_INPUT)
    public void onCHARGEButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "CHARGE BUTTON PRESSED!");

    }

    @OnClick(R.id.GAME_BUTTON)
    public void onGAMEButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "GAME BUTTON PRESSED!");
        Intent intent = new Intent(this,AndroidLauncher.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);

    }

    //************************************ HTTP NETWORKING CODE *****************************************************//
    private void sendQuaternionsToCloudRESTfully(String q0_string, String q1_string, String q2_string, String q3_string) {
        //Example GET URL - This worked!
//        String databaseURL = "https://api.thingspeak.com/update?api_key=E3VK2KDK3IBGK8HT&field1=1";

        //Example POST URL - This worked!
//        String databaseURL ="https://api.thingspeak.com/update.json";

        //Example AWS IoT URL - Returns "Missing Authentication Token" error
//        String databaseURL = "https://A13X9WUMZAX5RM.iot.us-east-1.amazonaws.com/things/Neblina_Test1/shadow";

        String databaseURL = "https://j4pguaz22a.execute-api.us-east-1.amazonaws.com/prod/dynamodump";

        //Send Quaternions
//        String apiKey = "b7721b89f28c6045846cfbc72c2c545c";
//        String databaseURL = "https://api.forecast.io/forecast/" + apiKey +
//                "/" + q0_string + "," + q1_string + "," + q2_string + "," + q3_string;

        if (isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();

            String postBody = "{" +
                    " \"timestamp\":\"00000069\"," +
                    " \"q1\":\"1\"," +
                    " \"q2\":\"2\"," +
                    " \"q3\":\"3\"," +
                    " \"q4\":\"4\"" +
                    "}";

            Log.w("HTTP_DEBUG", "sending: " + postBody);

            Request request = new Request.Builder()
                    .url(databaseURL)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(JSON, postBody))
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.w("HTTP_DEBUG", "onFailure's runOnUiThread was called");
                        }
                    });
                    Log.w("HTTP_DEBUG", "onFailure was called");
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                    try {
                        String jsonData = response.body().string();
                        Log.w("HTTP_DEBUG", jsonData);
//                        Log.i(TAG, response.body().string()); //This was the offending clause
                        if (response.isSuccessful()) {
                            int i = parseJSONResponse(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.w("HTTP_DEBUG", "Makes it to the second run()");
                                }
                            });

                        } else {
                           Log.w("HTTP_DEBUG", "HMMMMM Something bad happened here :(");
                        }

                    } catch (IOException e) {
                        Log.e(TAG, "IOException caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSONException caught: ", e);
                    }
                }
            });
        }
        else {
            Toast.makeText(this, "Network Is Unavailable!!!", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean isNetworkAvailable() {

        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;

        }
        else {
            Toast.makeText(this,getString(R.string.network_unavailable_message),Toast.LENGTH_LONG).show();
        }
        return isAvailable;
    }

    private int parseJSONResponse(String jsonData)throws JSONException{

//        Example JSON Parsing Code
//        JSONObject response = new JSONObject(jsonData);
//        String timezone = response.getString("timezone");
//        JSONObject daily = response.getJSONObject("daily");
//        JSONArray data = daily.getJSONArray("data");
//
//        String[] days = new String[data.length()];
//
//        for (int i = 0; i < data.length(); i++){
//            JSONObject jsonDay = data.getJSONObject(i);
//            String value = new String();
//
//            value = jsonDay.getString("summary");
//            value = jsonDay.getString("icon");
//            value = jsonDay.getDouble("temperatureMax");
//            value = (jsonDay.getLong("time");
//            value = (timezone);
//
//            days[i] = value;
        return 0;
        }

    private class getAWSID extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getApplicationContext(),
                    "us-east-1:6e702b0c-80ab-4461-9ec3-239f1d163cd5", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );

            identityID = credentialsProvider.getIdentityId();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.w("LogTag", "my ID is " + identityID);
                        }
            });

            // Initialize the Cognito Sync client
        CognitoSyncManager syncClient = new CognitoSyncManager(
                getApplicationContext(),
                Regions.US_EAST_1, // Region
                credentialsProvider);

// Create a record in a dataset and synchronize with the server
        com.amazonaws.mobileconnectors.cognito.Dataset dataset = syncClient.openOrCreateDataset("myDataset");
        dataset.put("myKey", "myValue");
        dataset.synchronize(new DefaultSyncCallback() {
            @Override
            public void onSuccess(com.amazonaws.mobileconnectors.cognito.Dataset dataset, List newRecords) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.w("LogTag", "Creating a Record was successful!" + identityID);
                    }
                });
            }
        });


            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

            Quaternions quaternions = new Quaternions();
            quaternions.setQ1(NebDeviceDetailFragment.latest_Q0);
            quaternions.setQ2(NebDeviceDetailFragment.latest_Q1);
            quaternions.setQ3(NebDeviceDetailFragment.latest_Q2);
            quaternions.setQ4(NebDeviceDetailFragment.latest_Q3);
            quaternions.setTimestamp(Long.toString(NebDeviceDetailFragment.timestamp_N));

            mapper.save(quaternions);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.w("LogTag", "so basically the problem is here");
                }
            });

            //READ an object from DynamoDB
//            final Book selectedBook = mapper.load(Book.class, 001);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Log.w("LogTag", "Book Value: " + selectedBook.getAuthor());
//                }
//            });
            return null;
        }
    }
    }


