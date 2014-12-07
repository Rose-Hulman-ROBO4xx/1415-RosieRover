

package edu.rose_hulman.rosierovercommsapp.rosierovercomms;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class MainActivity extends Activity {

    private Button buttonScan;
    private Button buttonConnect;
    private ListView btListView;

    private BleWrapper mBleWrapper = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //open SerialPassingService
        Context context = getApplicationContext();
        //SerialPassingService.setMain(this);
        Intent serialPassingServiceIntent= new Intent(context, SerialPassingService.class);
        // potentially add data to the intent
        //serviceIntent.putExtra("KEY1", "Value to be used by the service");
        startService(serialPassingServiceIntent);
        SerialPassingService.setMain(this);




        //TextView mTextView = (TextView) findViewById(R.id.text);

        final RequestQueue queue = Volley.newRequestQueue(this);
        //String url ="http://www.google.com";
        String url ="http://www.rosierover.com/roverControlPage";

        // Request a string response from the provided URL.
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        // Display the first 500 characters of the response string.
                        //mTextView.setText("Response is: "+ response.substring(0,500));

                        Log.i("VOLLEY", response.toString().substring(0, 500));
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //mTextView.setText("That didn't work!");
                Log.i("VOLLEY", "volley failed?");
            }
        });




        buttonScan = (Button) findViewById(R.id.button_scan); // initial
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                SerialPassingService.theService.initialize();
               SerialPassingService.mBleWrapper.startScanning();
            }
        });

        buttonConnect=(Button) findViewById(R.id.button_connectToTarget);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // btSelected = (BluetoothDevice) btListView.getAdapter().getItem(selectId);
                //SerialPassingService.serialSend("100,100,100,2000");
                // Add the request to the RequestQueue.
                queue.add(stringRequest);
            }
        });

        /*
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        */



    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        switch(item.getItemId()) {
            case R.id.button_scan:
                mBleWrapper.startScanning();
                break;
            case R.id.button_connectToTarget:
                //really just sends a move command. connection happens automatically.
                SerialPassingService.serialSend("COMMAND:100,200,200,3000");
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }





    @Override
    protected void onResume() {
        super.onResume();
        SerialPassingService.onResume();

    }


    @Override
    protected void onPause() {
        super.onPause();
        if(mBleWrapper!=null) {
            SerialPassingService.mBleWrapper.stopScanning();
        }
    }
    @Override
    protected void onStop(){
        super.onStop();
        if(mBleWrapper!=null) {
            SerialPassingService.mBleWrapper.stopScanning();
        }
    }

}

