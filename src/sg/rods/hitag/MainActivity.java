package sg.rods.hitag;

import com.razer.android.nabuopensdk.AuthCheckCallback;
import com.razer.android.nabuopensdk.NabuOpenSDK;
import com.razer.android.nabuopensdk.interfaces.BandListListener;
import com.razer.android.nabuopensdk.interfaces.FitnessHistoryListener;
import com.razer.android.nabuopensdk.interfaces.FitnessListener;
import com.razer.android.nabuopensdk.interfaces.Hi5Listener;
import com.razer.android.nabuopensdk.interfaces.LiveDataListener;
import com.razer.android.nabuopensdk.interfaces.NabuAuthListener;
import com.razer.android.nabuopensdk.interfaces.PulseListener;
import com.razer.android.nabuopensdk.interfaces.SendNotificationListener;
import com.razer.android.nabuopensdk.interfaces.SleepHistoryListener;
import com.razer.android.nabuopensdk.interfaces.SleepTrackerListener;
import com.razer.android.nabuopensdk.interfaces.UserIDListener;
import com.razer.android.nabuopensdk.interfaces.UserProfileListener;
import com.razer.android.nabuopensdk.models.Hi5Data;
import com.razer.android.nabuopensdk.models.NabuBand;
import com.razer.android.nabuopensdk.models.NabuFitness;
import com.razer.android.nabuopensdk.models.NabuFitnessHistory;
import com.razer.android.nabuopensdk.models.NabuNotification;
import com.razer.android.nabuopensdk.models.NabuSleepHistory;
import com.razer.android.nabuopensdk.models.NabuSleepTracker;
import com.razer.android.nabuopensdk.models.PulseData;
import com.razer.android.nabuopensdk.models.Scope;
import com.razer.android.nabuopensdk.models.UserProfile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import sg.rods.resources.SocketHandler;


public class MainActivity extends Activity {
	static NabuOpenSDK nabuSDK = null;
    Thread socketThread;
	StringBuilder builder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    	nabuSDK = NabuOpenSDK.getInstance(this);
        socketThread = new Thread() {
            @Override
            public void run() {
                    createSocketThread();
                    socketThread.interrupt();
            }
        };
        socketThread.start();
    	nabuSDK.initiate(this, "ec47877454906fd268286676ef549d0736965485", new String[] { Scope.COMPLETE }, new NabuAuthListener() {
    		@Override
            public void onAuthSuccess(String arg0) {
    		Log.e("Authentication Success", arg0);
    		builder = new StringBuilder();
    		builder.append(arg0);
    		setResult(builder.toString());
    		}
            @Override
    		public void onAuthFailed(String arg0) {
    		Log.e("Authentication Failed", arg0);
    		builder = new StringBuilder();
    		builder.append(arg0);
    		setResult(builder.toString());
    		}
    		});

        nabuSDK.checkAppAuthorized(this.getApplicationContext(), new AuthCheckCallback() {
            @Override
            public void onSuccess(boolean isAuthorized) {
// LOGIN SUCCESSFUL
                builder = new StringBuilder();
                builder.append("isAuthorized:");
                builder.append(Boolean.toString(isAuthorized));
                if (builder.length() == 0)
                    builder.append("No Result");
                setResult(builder.toString());

            }
            @Override
            public void onFailed(String errorMessage) {
// LOGIN FAILED
                builder = new StringBuilder();
                builder.append(errorMessage);
                if (builder.length() == 0)
                    builder.append("No Result");
                setResult(builder.toString());
            }
        });
    }

    public void setResult(String s) {
        System.out.println(s);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void createSocketThread() {
        if((SocketHandler.getSocket() == null)||(SocketHandler.getSocket().isConnected() == false)||(SocketHandler.getSocket().isClosed())) {
            try {
                Socket newSocket = new Socket("cynxtech-sql.cloudapp.net", 3853);
                SocketHandler.setSocket(newSocket);
            } catch (UnknownHostException err) {
                showCloseDialog();
            } catch (IOException err) {
                showCloseDialog();
            }
        }
    }

    public void showCloseDialog() {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Unable to connect to server. Try again later.");
        dlgAlert.setTitle("Error");
        dlgAlert.setPositiveButton("OK",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("Resuming MainActivity");
    }

    public void createRoom(View view)
    {
        Intent i = new Intent(getApplicationContext(), CreateRoom.class);
        startActivity(i);
    }

    public void joinRoom(View view)
    {
        Intent i = new Intent(getApplicationContext(), JoinRoom.class);
        startActivity(i);
    }
}
