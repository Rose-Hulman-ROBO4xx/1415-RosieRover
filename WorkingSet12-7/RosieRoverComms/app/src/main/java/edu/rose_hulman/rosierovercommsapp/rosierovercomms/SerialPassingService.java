package edu.rose_hulman.rosierovercommsapp.rosierovercomms;

//import android.content.ServiceConnection;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by naylorbl on 10/26/2014.
 */
public final class SerialPassingService extends Service {
//    static String urlString = "http://www.rosierover.com/coms/";
//    static BluetoothAdapter mBluetoothAdapter;
//    static BluetoothManager bluetoothManager;
//    static boolean mScanning;
    static MainActivity mainAct;
    public static SerialPassingService theService;
    public static ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
/*
    public static final UUID SerialPortUUID = UUID.fromString("0000dfb1-0000-1000-8000-00805f9b34fb");
    public static final UUID CommandUUID = UUID.fromString("0000dfb2-0000-1000-8000-00805f9b34fb");
    public static final UUID ModelNumberStringUUID = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
    public static final UUID blunoServ = UUID.fromString("7be9e813-3181-48b9-ad66-a63f4d9174d8");
*/

    //UUID's for Bluno
    public static final String SerialPortUUID = "0000dfb1-0000-1000-8000-00805f9b34fb";
    public static final String CommandUUID = "0000dfb2-0000-1000-8000-00805f9b34fb";
    public static final String ModelNumberStringUUID = "00002a24-0000-1000-8000-00805f9b34fb";

//    private static final int REQUEST_ENABLE_BT = 1;
//    // Stops scanning after 10 seconds.
//    private static final long SCAN_PERIOD = 10000;
    static BleWrapper mBleWrapper;
    private static BluetoothGattCharacteristic mModelNumberCharacteristic;
    private static BluetoothGattCharacteristic mSerialPortCharacteristic;
    private static BluetoothGattCharacteristic mCommandCharacteristic;
    private static ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

//    //String SENDER_ID = "813526336477";  //sender ID for old GCM test website
//    String SENDER_ID = "471883007186"; //sender ID for rosierover.appspot.com
//
//    public static final String EXTRA_MESSAGE = "message";
//    public static final String PROPERTY_REG_ID = "registration_id";
//    private static final String PROPERTY_APP_VERSION = "appVersion";
//    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
//
//    static final String TAG = "GCM Demo";
//
//    private final static int SERVER_PORT = 8006;
//    public final static int RECEIVING_TIMEOUT_SERVER = 3000;
//    static DatagramSocket socket;
//    DatagramPacket packetOut;
//    DatagramPacket packetIn;
//    byte[] DataIn;
//    byte[] DataOut;
//
//    TextView mDisplay;
//    GoogleCloudMessaging gcm;
//    AtomicInteger msgId = new AtomicInteger();
    Context context;
//    String regid;
//    static String IP_ADR = "64.233.183.141";
    static ClientComms comms;

    public static GPSTracker gps;
    //public static String[] msgToSend = new String[4];
    public static double[] msgToSend = new double[4];
    public static boolean openServerComms=false;




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        theService=this; //declares a static instance of this class. Used as a substitute for not
        //being able to make an entire class static. Easier for MainActivity to reference it.
        Log.i("DEBUG","starting SerialPassingService");
        gps=new GPSTracker(this);


        return Service.START_STICKY; //service will continue to run if application is closed. Very important
    }

    @Override
    public void onCreate() {

        comms=new ClientComms();
        try {
            comms.setup();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        msgToSend[0]="test1";
//        msgToSend[1]="test2";
//        msgToSend[2]="test3";
//        msgToSend[3]="test4";
//        comms.sendMsg(msgToSend);
        //comms.setReceiver();
    }



    static public void sendToServer() {
        if(openServerComms) {
            try {
                gps.getLocation();
                msgToSend[0] = .97;
                msgToSend[1] = .82;
                msgToSend[2] = gps.getLongitude();
                msgToSend[3] = gps.getLatitude();
//            msgToSend[0]="1";
//            msgToSend[1]="2";
//            msgToSend[2]="3";//gps.getLongitude();
//            msgToSend[3]="4";//gps.getLatitude();
                serialSend("" + comms.sendMsg(msgToSend));
                comms.prepNextMessage();
            } catch (Exception e) {
                Log.d("Server", e.toString());
            }
        }else{
            Log.i("Server","Message not sent. OpenServerComms = false");
        }
    }




    public void initialize(){
        Log.i("DEBUG","initializing SerialPassingService");


        context=mainAct.getApplicationContext();

        ////////////////////////////////////////////////////////////////////////////
        //taken from gcm demo. Starts the process of connecting or registering with gcm server
       /* if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid=getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }*/
        ///////////////////////////////////////////////////////////////////////////////////////




        /*
        declare a static instance of BleWrapper, which handles all of our bluetooth stuff
         */
        mBleWrapper=new BleWrapper(this, new BleWrapperUiCallbacks.Null(){
            @Override
            public void uiDeviceFound(final BluetoothDevice device, final int rssi, final byte[] record){
                String msg = "uiDeviceFound: "+device.getName()+", " +rssi;//+", "+rssi.toString();
                Log.d("BT", "uiDeviceFound: " + msg); //lists found things in logcat

                //if find Bluno, connect, stop searching for more devices
                if(device.getName().equals("Bluno")==true){
                   // bool status;
                    if(mBleWrapper.connect(device.getAddress().toString())){
                        Log.d("BT", "CONNECTION SUCCESSFUL");
                        mBleWrapper.stopScanning();

                    }else{
                        Log.d("BT", "CONNECTION FAILED");
                    }

                }
            }




            /*
            lists all available services for a device in logcat and passes them with the device to
            defineCharacteristics which searches them for the services/characteristics we care about
             */
            @Override
            public void uiAvailableServices(BluetoothGatt gatt, BluetoothDevice device, List<BluetoothGattService> services){
                for(BluetoothGattService service : services){
                    String serviceName;
                    serviceName = BleNamesResolver.resolveUuid(service.getUuid().toString());
                    Log.d("DEBUG", serviceName);
                }
                defineCharacteristics(services,device);
            }



            //I don't think this is doing anything important for us.
            @Override
        public void uiNewValueForCharacteristic(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service,
                                                BluetoothGattCharacteristic ch, String strValue,
                                                int intValue ,byte[] rawValue, String timestamp){
                super.uiNewValueForCharacteristic(gatt,device,service,ch,strValue,intValue,rawValue,timestamp);
                Log.d("LOGTAG", "uiNewValueForCharacteristic");
                for(byte b:rawValue){
                    Log.d("LOGTAG","Val: "+b);
                }
            }

        });//end of BleWrapper class

        //do we even have BT hardware?
        if(mBleWrapper.checkBleHardwareAvailable()==false){
            Toast.makeText(this, "No BLE-compatible hardware detected", Toast.LENGTH_SHORT).show();
            //finish();
        }

        //I commented this next line our because I don't think we need this recursive call. undo if it breaks
        mBleWrapper.initialize();
    }


    /*
    Some sort of service crap. i have no idea what it does, but it seems to be important.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /*
    MainActivity calls this so SerialPassingService has a callback to the visual interface, context, etc
     */
    public static void setMain(MainActivity act){
        mainAct=act;
    }

  /*  *//*
    Taken from GCM demo, establishes GCM connection.
    *//*
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, mainAct,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else {
                Log.i(TAG, "This device is not supported.");

            }
            return false;
        }
        return true;
    }

    *//**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     *//*
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    *//**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     *//*
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        Log.i(TAG, registrationId);
        return registrationId;
    }

    *//**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     *//*
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
             //   mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }

    *//**
     * @return Application's version code from the {@code PackageManager}.
     *//*
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


    *//**
     * @return Application's {@code SharedPreferences}.
     *//*
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    *//**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     *//*
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }*/

    public static void onResume(){

    }






    /*
    passes the command to the Bluno
     */
    public static void serialSend(String command){
        //mSCharacteristic.setValue(command);
		//mBluetoothLeService.writeCharacteristic(mSCharacteristic);
        try {
            mBleWrapper.writeDataToCharacteristic(mSerialPortCharacteristic, command.getBytes());
        }catch(NullPointerException e){
            Log.d("SerialSend", "Failed to send command");
        };
        //mBleWrapper.writeDataToCharacteristic(mCommandCharacteristic,command.getBytes());
        //mBleWrapper.writeDataToCharacteristic(mModelNumberCharacteristic,command.getBytes());
    }

    /*
    Taken from BlunoBasicDemo (on the Bluno wiki), loops through the available UUID's to find the ones we care about
     */
    public static void defineCharacteristics(List<BluetoothGattService> gattServices, BluetoothDevice device){
        for (BluetoothGattService gattService : gattServices) {
            String uuid = gattService.getUuid().toString();
            System.out.println("displayGattServices + uuid=" + uuid);

            List<BluetoothGattCharacteristic> gattCharacteristics = gattService
                    .getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                uuid = gattCharacteristic.getUuid().toString();
                if (uuid.equals(ModelNumberStringUUID)) {
                    mModelNumberCharacteristic = gattCharacteristic;
                    System.out.println("mModelNumberCharacteristic  "
                            + mModelNumberCharacteristic.getUuid().toString());
                    Log.i("DEBUG", "Found Model Number");
                } else if (uuid.equals(SerialPortUUID)) {
                    mSerialPortCharacteristic = gattCharacteristic;
                    System.out.println("mSerialPortCharacteristic  "
                            + mSerialPortCharacteristic.getUuid().toString());
                    Log.i("DEBUG", "Found SerialPort");
                    // updateConnectionState(R.string.comm_establish);
                } else if (uuid.equals(CommandUUID)) {
                    mCommandCharacteristic = gattCharacteristic;
                    System.out.println("mSerialPortCharacteristic  "
                            + mSerialPortCharacteristic.getUuid().toString());
                    Log.i("DEBUG", "Found Command Char");
                    // updateConnectionState(R.string.comm_establish);
                }
            }
            mGattCharacteristics.add(charas);
        }
        if (!(mModelNumberCharacteristic == null || mSerialPortCharacteristic == null || mCommandCharacteristic == null)) {
            /*
            mBluetoothLeService.connect(device.getAddress());
            mSCharacteristic = mModelNumberCharacteristic;
            mBluetoothLeService.setCharacteristicNotification(mSCharacteristic,
                    true);
            mBluetoothLeService.readCharacteristic(mSCharacteristic);
            */
            mBleWrapper.setNotificationForCharacteristic(mSerialPortCharacteristic, true);
           // mBleWrapper.writeDataToCharacteristic(mSerialPortCharacteristic, new byte[]{0x01});
            mBleWrapper.setNotificationForCharacteristic(mModelNumberCharacteristic, true);
            //mBleWrapper.writeDataToCharacteristic(mModelNumberCharacteristic, new byte[]{0x01});
            mBleWrapper.setNotificationForCharacteristic(mCommandCharacteristic, true);
            //mBleWrapper.writeDataToCharacteristic(mCommandCharacteristic, new byte[]{0x01});
        }
    }



}



