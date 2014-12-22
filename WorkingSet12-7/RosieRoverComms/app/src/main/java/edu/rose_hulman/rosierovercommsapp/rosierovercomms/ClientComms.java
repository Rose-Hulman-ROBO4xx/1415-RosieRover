package edu.rose_hulman.rosierovercommsapp.rosierovercomms;

import android.util.Log;
import android.widget.Toast;



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

    URL url;// = new URL("http://gcm-attempt-01.appspot.com/");
    URLConnection conn;// = url.openConnection();

    public void setup() throws IOException {
         url = new URL("http://gcm-attempt-01.appspot.com/");
        conn= url.openConnection();
        conn.setDoOutput(true);
        //conn.setDoInput(true);
    }

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

    /*
    establish connection to server and send message
     */
    public void sendMsg() {

        //Toast.makeText(SerialPassingService.theService, msg, Toast.LENGTH_SHORT).show();


        // Create Inner Thread Class
        backgroundSend = new Thread(new Runnable() {
            @Override
            public void run() {


                String name = "nameMessage";
                String email = "emailMessage";
                String Login = "login message";
                String Pass = "pass message";

                String data = null;
                try {
                    data = URLEncoder.encode("name", "UTF-8")
                            + "=" + URLEncoder.encode(name, "UTF-8");

                    data += "&" + URLEncoder.encode("email", "UTF-8")
                            + "=" + URLEncoder.encode(email, "UTF-8");

                    data += "&" + URLEncoder.encode("user", "UTF-8")
                            + "=" + URLEncoder.encode(Login, "UTF-8");

                    data += "&" + URLEncoder.encode("pass", "UTF-8")
                            + "=" + URLEncoder.encode(Pass, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                //String text = "";
                //BufferedReader reader = null;

                // Send data
                try {

                    // Defined URL  where to send data
                    //URL url = new URL(urlString);
                    url = new URL("http://gcm-attempt-01.appspot.com/");
                    // Send POST data request

                    conn = url.openConnection();
                    conn.setDoOutput(true);
                    //conn.setDoInput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                    wr.write(data);
                    wr.flush();

                    // Get the server response

                        reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    /*
                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        // Append server response in string
                        sb.append(line + "\n");
                    }


                    text = sb.toString();
                    */

                } catch (Exception ex) {
                    Log.i("web crap", ex.toString());
                }
                    /*
                 finally {
                    try {

                        reader.close();
                    } catch (Exception ex) {
                        Log.i("web crap", ex.toString());
                    }
                }
                */

                // Show response on activity
                //content.setText( text  );

                //Log.i("web", text);
            }});

        backgroundSend.start();

    }}

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



