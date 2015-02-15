

package edu.rose_hulman.rosierovercommsapp.rosierovercomms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

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
                SerialPassingService.serialSend("100,100,100,2000");
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
        SerialPassingService.mBleWrapper.stopScanning();

    }
    @Override
    protected void onStop(){
        super.onStop();
        SerialPassingService.mBleWrapper.stopScanning();
    }

}

