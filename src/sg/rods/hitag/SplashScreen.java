package sg.rods.hitag;

import sg.rods.hitag.util.SystemUiHider;
import sg.rods.resources.SocketHandler;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import java.net.InetSocketAddress;
import java.net.Socket;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
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
        Socket clientSocket = null;
        try {
            String serverHost = getString(R.string.server_hostname);
            int serverPort = Integer.parseInt(getString(R.string.server_port));
            InetSocketAddress address = new InetSocketAddress(serverHost, serverPort);
            clientSocket = new Socket(address.getAddress(), serverPort);
        } catch (Exception err) {
            clientSocket = null;
            System.out.println("Error: " + err.getLocalizedMessage());
        }
        SocketHandler.setSocket(clientSocket);
        // Start Nabu Connection
        // Trigger Socket and Nabu Connection
       // delayedHide(100);
    }
}
