package sg.rods.hitag;

import sg.rods.resources.SocketHandler;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        final SocketConnect sc = new SocketConnect();
        sc.setIntent(new Intent(SplashScreen.this, MainActivity.class));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Thread t = new Thread(sc);
                t.start();
            }
        }, 5000);
        //Intent i = new Intent(SplashScreen.this, MainActivity.class);
        //startActivity(i);
        //finish();
        // Start Nabu Connection
        // Trigger Socket and Nabu Connection
       // delayedHide(100);
    }

    class SocketConnect implements Runnable {

        private Intent i = null;

        public void setIntent(Intent i) {
            this.i = i;
        }

        @Override
        public void run() {
            Socket clientSocket = null;
            String serverHost = getString(R.string.server_hostname);
            int serverPort = Integer.parseInt(getString(R.string.server_port));
            System.out.println("Address: " + serverHost.toString() + ":" + serverPort);
            try {
                clientSocket = new Socket(serverHost, serverPort);
                clientSocket.setKeepAlive(true);
            } catch (UnknownHostException e) {
                clientSocket = null;
                System.out.println("Unknown Host: " + e.getMessage());
            } catch (IOException e) {
                clientSocket = null;
                System.out.println("IO Error: " + e.getMessage());
            }
            SocketHandler.setSocket(clientSocket);
            startActivity(i);
            finish();
        }
    }
}
