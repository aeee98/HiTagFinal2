package sg.rods.hitag;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import sg.rods.resources.Command;
import sg.rods.resources.SocketHandler;

public class CreateRoom extends Activity {
    //instantiate SDK
    static NabuOpenSDK nabuSDK = null;
    private Socket clientSocket;
    private String name = "Guest";
    String roomName = "Room Placeholder";
    StringBuilder builder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);
        Button createButton = (Button) findViewById(R.id.roomCreateBtn);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText et = (EditText) findViewById(R.id.RoomNameTextEdit);
                roomName = et.getText().toString();
                if(roomName.isEmpty() == false) {
                    try {
                        sendBytes((byte) 01, roomName);
                    } catch (IOException err) {
                        Toast.makeText(getApplicationContext(), "Unable to reached designated server.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please input a room name.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //get Nabu Data
        nabuSDK = NabuOpenSDK.getInstance(this);
        clientSocket = SocketHandler.getSocket();
        if(clientSocket != null) {
            SocketListener sl = new SocketListener();
            sl.setContextInterface(this);
            new Thread(sl).start();
        } else {
            // Disable the create button.
        }
    }

    class SocketListener implements Runnable {

        private Context c;

        public void setContextInterface(Context c) {
            this.c = c;
        }

        @Override
        public void run() {
            Intent intentAction = new Intent(c, GameRoomActivity.class);
            boolean running = true;
            while(running) {
                try {
                    byte[] fromClient = readBytes();
                    byte command = fromClient[0];
                    byte[] value = new byte[fromClient.length - 1];
                    for (int i = 1; i < fromClient.length; i++) {
                        value[i - 1] = fromClient[i];
                    }
                    String stringValue = new String(value);
                    switch(command) {
                        case Command.CREATE:
                            intentAction.putExtra("roomId", stringValue);
                            intentAction.putExtra("roomName", roomName);
                            intentAction.putExtra("host", true);
                            break;
                        case Command.SETNAME:
                            break;
                        case Command.ROOM_REFRESH:
                            intentAction.putExtra("listPlayer", new String(value));
                            break;
                    }
                } catch (InterruptedException e) {
                   // Toast.makeText(c, "Unable to create a room.", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    //Toast.makeText(c, "Are you sure you're connected to the server?", Toast.LENGTH_LONG).show();
                }
                if((intentAction.hasExtra("roomId"))&&(intentAction.hasExtra("roomName"))&&(intentAction.hasExtra("listPlayer"))&&(intentAction.hasExtra("host"))) {
                    running = false;
                }
            }
            startActivity(intentAction);
            finish();
        }
    }

    public void setName(byte[] value) {
        this.name = new String(value);
    }

    public void sendBytes(byte byteCode, String message) throws IOException {
        OutputStream os = clientSocket.getOutputStream();
        byte[] tempToSendBytes = message.getBytes();
        byte[] toSendBytes = new byte[tempToSendBytes.length + 1];
        toSendBytes[0] = byteCode;
        for (int i = 0; i < tempToSendBytes.length; i++) {
            toSendBytes[i + 1] = tempToSendBytes[i];
        }
        int toSendLen = toSendBytes.length;
        byte[] toSendLenBytes = new byte[4];
        toSendLenBytes[0] = (byte) (toSendLen & 0xff);
        toSendLenBytes[1] = (byte) ((toSendLen >> 8) & 0xff);
        toSendLenBytes[2] = (byte) ((toSendLen >> 16) & 0xff);
        toSendLenBytes[3] = (byte) ((toSendLen >> 24) & 0xff);
        os.write(toSendLenBytes);
        os.write(toSendBytes);
    }

    public byte[] readBytes() throws IOException, InterruptedException {
        InputStream in = clientSocket.getInputStream();;
        byte[] lenBytes = new byte[4];
        in.read(lenBytes, 0, 4);
        int len = (((lenBytes[3] & 0xff) << 24) | ((lenBytes[2] & 0xff) << 16)
                | ((lenBytes[1] & 0xff) << 8) | (lenBytes[0] & 0xff));
        byte[] receivedBytes = new byte[len];
        in.read(receivedBytes, 0, len);
        // String received = new String(receivedBytes, 0, len);
        return receivedBytes;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
