package edu.rose_hulman.rosierovercommsapp.rosierovercomms;

import android.util.Log;
import android.widget.Toast;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


public class ClientComms {

    public static Thread backgroundSend;
    public static Thread backgroundReceiver;
    BufferedReader reader = null;
    String text;
    public static int[] commands = new int[5];

    URL url;// = new URL("http://gcm-attempt-01.appspot.com/");
    URLConnection conn;// = url.openConnection();
    public String urlString="http://rosierover.com/coms";
    //public String urlString="http://gcm-attempt-01.appspot.com/";

    public void setup() throws IOException {
        url = new URL(urlString);
        conn= url.openConnection();
        conn.setDoOutput(true);
        //conn.setDoInput(true);
    }

    /*
    public void setReceiver(){
        backgroundReceiver=new Thread(new Runnable() {
            StringBuilder sb = new StringBuilder();
            String line = null;
            String text = "";
            @Override
            public void run () {
                //watch for incoming messages
                while (true) {
                    if (reader != null) {
                        try {
                            //reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                            if ((line = reader.readLine()) != null) {
                                //sb = new StringBuilder();
                                sb.delete(0,sb.capacity());
                                //line = null;
                                text = "";
                                sb.append(line + "\n");
                                try {
                                    while ((line = reader.readLine()) != null) {
                                        // Append server response in string
                                        sb.append(line + "\n");
                                    }
                                    text=sb.toString();
                                    Log.i("web", text);
                                    //do a thing with server message
                                } catch (IOException e) {
                                   // e.printStackTrace();
                                }
                            }
                        } catch (IOException e) {
                           // e.printStackTrace();
                        }


                    }
                }
            }
        }

        );
        backgroundReceiver.start();
    }
*/
    /*
    establish connection to server and send message
     */
    public int[] sendMsg( final double msgToSend[]) {

        //Toast.makeText(SerialPassingService.theService, msg, Toast.LENGTH_SHORT).show();


        // Create Inner Thread Class
        backgroundSend = new Thread(new Runnable() {
        @Override
         public void run() {


//            String roboBat = msgToSend[0];
//            String phoneBat = msgToSend[1];
//            String gpsX = msgToSend[2];
//            String gpsY = msgToSend[3];

            double roboBat = msgToSend[0];
            double phoneBat = msgToSend[1];
            double gpsX = msgToSend[2];
            double gpsY = msgToSend[3];
            String data = null;
            try {

//                data = URLEncoder.encode("robotBatteryLife", "UTF-8")
//                        + "=" + URLEncoder.encode(roboBat, "UTF-8");
//
//                data += "&" + URLEncoder.encode("phoneBatteryLife", "UTF-8")
//                        + "=" + URLEncoder.encode(phoneBat, "UTF-8");
//
//                data += "&" + URLEncoder.encode("GPSx", "UTF-8")
//                        + "=" + URLEncoder.encode(gpsX, "UTF-8");
//
//                data += "&" + URLEncoder.encode("GPSy", "UTF-8")
//                        + "=" + URLEncoder.encode(gpsY, "UTF-8");

                data = URLEncoder.encode("robotBatteryLife", "UTF-8")
                        + "=" + roboBat;

                data += "&" + URLEncoder.encode("phoneBatteryLife", "UTF-8")
                        + "=" + phoneBat;

                data += "&" + URLEncoder.encode("GPSx", "UTF-8")
                        + "=" + gpsX;

                data += "&" + URLEncoder.encode("GPSy", "UTF-8")
                        + "=" + gpsY;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String text = "";
            BufferedReader reader = null;

            // Send data
            try {

                // Defined URL  where to send data
                url = new URL(urlString);
                // Send POST data request
                conn = url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();
                // Get the server response
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));


                StringBuilder sb = new StringBuilder();
                String line = null;
                //Log.i("web test", "prep read");
                // Read Server Response
                while ((line = reader.readLine()) != null) {
                   // Log.i("web test", "read not null");
                    // Append server response in string
                    sb.append(line + "\n");
                    //Log.i("web crap debug", line);
                }
                try {
                    JSONObject obj = new JSONObject(sb.toString());
                    // pass on returned commands
                    text = "" + obj.toString();
                    ClientComms.commands[0] = obj.getInt("emergency");
                    ClientComms.commands[1] = obj.getInt("warning");
                    ClientComms.commands[2] = obj.getInt("leftMotor");
                    ClientComms.commands[3] = obj.getInt("rightMotor");
                    ClientComms.commands[4] = obj.getInt("fire");
                    ClientComms.commands[5] = obj.getInt("pan");
                    ClientComms.commands[6] = obj.getInt("tilt");
                }catch (Exception e){
                    text=sb.toString();
                }




                //text = sb.toString();


            } catch (Exception ex) {
                Log.i("web crap", ex.toString());
            } finally {
                try {

                    reader.close();
                } catch (Exception ex) {
                    Log.i("web crap", ex.toString());
                }
            }


            // Show response on activity
            //content.setText( text  );
            Log.i("web", text);
        }});
        backgroundSend.start();

        return commands;
    }

    public void prepNextMessage( ) {

        // Create Inner Thread Class
        Thread backgroundDelay = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(250);
                    SerialPassingService.sendToServer();
                }catch(Exception e){
                    Log.i("Post Delay","couldn't sleep or post?");
                }
            }
        });
        backgroundDelay.start();
    }

}

/* example if i need to update UI

final Handler handler = new Handler();
final Runnable updater = new Runnable() {
    public void run() {
        display3.setText("System On");
    }
};

Thread x = new Thread() {
    public void run() {
        while (boo) {
            handler.invokeLater(updater);

            try {
                // do something here
                //display3.setText("System On");

                Log.d(TAG, "local Thread sleeping");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "local Thread error", e);
            }

        }
    }
};
 */



