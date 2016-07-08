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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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

import butterknife.ButterKnife;
import butterknife.OnClick;

public class BLEDeviceScanActivity extends ListActivity {

    //List Variables
    private static final int REQUEST_ENABLE_BT = 0;
    private List<String> mDeviceNameList;
    private ArrayAdapter<String> mLeDeviceListAdapter;
    private static final long SCAN_PERIOD = 60000;
    private final Map<String,Neblina> mDeviceList = new HashMap<String,Neblina>();
    private Neblina activeDevice;
    private NebDeviceDetailFragment activeDeviceDelegate;
    private boolean mBluetoothGatt;
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

    //Button state variables
    boolean is_BLE_BUTTON_on           = false;
    boolean is_UART_BUTTON_on          = false;
    boolean is_QUATERNION_BUTTON_on    = false;
    boolean is_MAG_BUTTON_on           = false;
    boolean is_LOCK_BUTTON_on          = false;
    boolean is_ERASE_BUTTON_on         = false;
    boolean is_RECORD_BUTTON_on        = false;
    boolean is_PLAYBACK_BUTTON_on      = false;
    boolean is_LED0_BUTTON_state_on    = false;
    boolean is_LED1_BUTTON_on          = false;
    boolean is_EEPROM_BUTTON_on        = false;
    boolean is_CHARGE_INPUT_on         = false;

    //Code Variables
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_scan_activity);
        ButterKnife.inject(this);
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

        activateBLE();
        mDeviceNameList = new ArrayList<String>();
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
        }
    }


    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        //Get the NEBLINA device and setup the NEBLINA interface
        activeDevice = mDeviceList.get(l.getItemAtPosition(position).toString());
        mBluetoothGatt = activeDevice.Connect(getBaseContext());

        Bundle arguments = new Bundle();
        arguments.putParcelable(NebDeviceDetailFragment.ARG_ITEM_ID, activeDevice);
        activeDeviceDelegate.SetItem(activeDevice);
        activeDeviceDelegate.setArguments(arguments);
        this.getFragmentManager().beginTransaction()
                    .add(activeDeviceDelegate, "Fun")
                    .commit();

        //Tell the user he's connected
        Toast.makeText(this, "Connecting to " + activeDevice.toString(), Toast.LENGTH_LONG).show();
    }

    //Callback for when a BLE device is found
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback(){
                @Override
            public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //Get the ID of the discovered device
                            int i = 0;
                            long deviceID = 0;
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

                            //Add the device to the list if it isn't there already
                            if(device.getName() != null){
                                Neblina neblina = new Neblina(deviceID,device);
                                if (mDeviceList.containsKey(neblina.toString()) == false) {
                                    mLeDeviceListAdapter.add(neblina.toString());
                                    mLeDeviceListAdapter.notifyDataSetChanged();
                                    mDeviceList.put(neblina.toString(), neblina);
                                }
                            }
                        }
                    });
                }
            };


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

            sendBroadcast(intent);
        }


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            if (BLEDeviceScanActivity.ACTION_GATT_CONNECTED.equals(action)) {
                mConnectionState = STATE_CONNECTED;
                if(debug_mode1==true) {
                    Log.w("BLUETOOTH DEBUG", "The intent action is ACTION_GATT_CONNECTED");
                }
                invalidateOptionsMenu();
            } else if (BLEDeviceScanActivity.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnectionState = STATE_DISCONNECTED;
                if(debug_mode1==true) {
                    Log.w("BLUETOOTH DEBUG", "The intent action is ACTION_GATT_DISCONNECTED");
                }
                invalidateOptionsMenu();
            } else if (BLEDeviceScanActivity.
                    ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                if(debug_mode1==true) {
                    Log.w("BLUETOOTH DEBUG", "The intent action is ACTION_GATT_SERVICES_DISCOVERED");
                }
            } else if (BLEDeviceScanActivity.ACTION_DATA_AVAILABLE.equals(action)) {
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


    @OnClick(R.id.BLE_BUTTON)
    public void onBLEButtonClick(View view){
            Log.w("BLUETOOTH_DEBUG", "BLE BUTTON PRESSED!");

        if(activeDevice!=null){
            activeDevice.setDataPort(0, (byte) 1);
        }
        else{
            Log.w("BLUETOOTH_DEBUG", "DEVICE NOT READY");
        }

//        sendQuaternionsToCloudRESTfully("1", "1", "1", "1");
    }

    @OnClick(R.id.UART_BUTTON)
    public void onUARTButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "UART BUTTON PRESSED!");
        if(activeDevice!=null){
            activeDevice.setDataPort(1, (byte) 1);
        }else{
            Log.w("BLUETOOTH_DEBUG", "DEVICE NOT READY");
        }
    }

    @OnClick(R.id.QUATERNION_BUTTON)
    public void onQuaternionButtonClick(View view) {
        Log.w("BLUETOOTH_DEBUG", "QUATERNION BUTTON PRESSED!");
        if(activeDevice!=null){
            activeDevice.streamQuaternion(true);
        }else{
            Log.w("BLUETOOTH_DEBUG", "DEVICE NOT READY");
        }
    }

    @OnClick(R.id.MAG_BUTTON)
    public void onMAGButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "MAG BUTTON PRESSED!");

        if(activeDevice!=null){
            activeDevice.streamMAG(true);
        }else{
            Log.w("BLUETOOTH_DEBUG", "DEVICE NOT READY");
        }

    }

    @OnClick(R.id.LOCK_BUTTON)
    public void onLOCKButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "LOCK BUTTON PRESSED!");

        if(activeDevice!=null){
            activeDevice.setLockHeadingReference(true);
        }else{
            Log.w("BLUETOOTH_DEBUG", "DEVICE NOT READY");
        }


    }

    @OnClick(R.id.ERASE_BUTTON)
    public void onERASEButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "ERASE BUTTON PRESSED!");

        if(activeDevice!=null){
            activeDevice.eraseStorage(true);
        }else{
            Log.w("BLUETOOTH_DEBUG", "DEVICE NOT READY");
        }
    }

    @OnClick(R.id.RECORD_BUTTON)
    public void onRECORDButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "RECORD BUTTON PRESSED!");

        if(activeDevice!=null){
            activeDevice.sessionRecord(true);
        }else{
            Log.w("BLUETOOTH_DEBUG", "DEVICE NOT READY");
        }

    }

    @OnClick(R.id.PLAYBACK_BUTTON)
    public void onPLAYBACKButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "PLAYBACK BUTTON PRESSED!");

        if(activeDevice!=null){
            activeDevice.sessionPlayback(true,0001); //TODO: What should be the sessionID here???
        }else{
            Log.w("BLUETOOTH_DEBUG", "DEVICE NOT READY");
        }

    }

    @OnClick(R.id.LED0_BUTTON)
    public void onLED0ButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "LED0 BUTTON PRESSED!");
        if(activeDevice!=null){
            activeDevice.setLed((byte) 0, (byte) 1);
        }else{
            Log.w("BLUETOOTH_DEBUG", "DEVICE NOT READY");
        }
    }

    @OnClick(R.id.LED1_BUTTON)
    public void onLED1ButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "LED1 BUTTON PRESSED!");
        if(activeDevice!=null){
            activeDevice.setLed((byte) 1, (byte) 1);
        }else{
            Log.w("BLUETOOTH_DEBUG", "DEVICE NOT READY");
        }
    }

    @OnClick(R.id.EEPROM_BUTTON)
    public void onEEPROMButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "EEPROM BUTTON PRESSED!");
        if(activeDevice!=null){
            activeDevice.eepromRead(1);
        }else{
            Log.w("BLUETOOTH_DEBUG", "DEVICE NOT READY");
        }
    }

    @OnClick(R.id.CHARGE_INPUT)
    public void onCHARGEButtonClick(View view){
        Log.w("BLUETOOTH_DEBUG", "CHARGE BUTTON PRESSED!");
        TextView mEdit = (EditText)findViewById(R.id.CHARGE_INPUT);
        String value = mEdit.getText().toString();

        if(activeDevice!=null){
            if(isInteger(value, 10)){
            activeDevice.setBatteryChargeCurrent(Integer.parseInt(value));
            }else{
                Toast.makeText(this, "Please enter an integer value", Toast.LENGTH_LONG).show();
            }
        }else{
            Log.w("BLUETOOTH_DEBUG", "DEVICE NOT READY");
        }
    }

    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
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


