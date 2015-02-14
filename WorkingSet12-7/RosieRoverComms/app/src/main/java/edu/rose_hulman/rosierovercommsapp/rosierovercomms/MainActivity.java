

package edu.rose_hulman.rosierovercommsapp.rosierovercomms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class MainActivity extends Activity {

    private Button buttonBtConnect;
    private Button buttonServerConnect;
    private Button buttonServerDisconnect;
    private Button buttonBtDisconnect;
    private Button buttonClose;

    private ImageView roverConnectivity;
    private ImageView serverConnectivity;

    private BleWrapper mBleWrapper = null;
    private WifiManager mWifi;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Log.d("WTF", "running right code");

        roverConnectivity=(ImageView)findViewById(R.id.roverConnectionImage);
        serverConnectivity=(ImageView)findViewById(R.id.serverConnectionImage);

        //open SerialPassingService
        Context context = getApplicationContext();
        final Intent serialPassingServiceIntent= new Intent(context, SerialPassingService.class);
        // potentially add data to the intent

        startService(serialPassingServiceIntent);
        SerialPassingService.setMain(this);

        buttonBtConnect = (Button) findViewById(R.id.button_connectRover); // initial
        buttonBtConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                SerialPassingService.theService.initialize();
               SerialPassingService.mBleWrapper.startScanning();
                roverConnectivity.setImageResource(R.drawable.checkmark_image);
            }
        });

        buttonServerConnect=(Button) findViewById(R.id.button_connectServer);
        buttonServerConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SerialPassingService.openServerComms=true;
                SerialPassingService.sendToServer();
                serverConnectivity.setImageResource(R.drawable.checkmark_image);
                //SerialPassingService.gps.getLocation();
                //Log.d("GPS", "" + SerialPassingService.gps.getLatitude());

            }
        });

        buttonServerDisconnect=(Button) findViewById(R.id.button_disconnectServer);
        buttonServerDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SerialPassingService.openServerComms=false;
                serverConnectivity.setImageResource(R.drawable.xmark_image);

            }
        });

        buttonBtDisconnect=(Button) findViewById(R.id.button_disconnectRover);
        buttonBtDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SerialPassingService.mBleWrapper.stopScanning();
                roverConnectivity.setImageResource(R.drawable.xmark_image);
                //

            }
        });

        buttonClose=(Button) findViewById(R.id.button_close);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SerialPassingService.openServerComms=false;
                SerialPassingService.mBleWrapper.stopScanning();
                SerialPassingService.theService.stopSelf();
                roverConnectivity.setImageResource(R.drawable.xmark_image);
                serverConnectivity.setImageResource(R.drawable.xmark_image);

            }
        });
    }

/*
//not used
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
*/




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

