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

import com.razer.android.nabuopensdk.NabuOpenSDK;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import sg.rods.resources.Command;
import sg.rods.resources.SocketHandler;


public class JoinRoom extends Activity {

    private Socket clientSocket;
    private Thread NetworkListener;
    private String roomId;
    private String name = "Guest";
    static NabuOpenSDK nabuSDK = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_room);
        clientSocket = SocketHandler.getSocket();
        name = getSharedPreferences("prefsValue", MODE_PRIVATE).getString("displayName", "Guest");
        Button startButton = (Button) findViewById(R.id.startGameBtn);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText et = (EditText) findViewById(R.id.roomIdText);
                roomId = et.getText().toString();
                if(roomId.isEmpty() == false) {
                    try {
                        sendBytes((byte) 02, roomId);
                    } catch (IOException err) {
                        Toast.makeText(getApplicationContext(), "Unable to reached designated server.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please input a room name.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        nabuSDK = NabuOpenSDK.getInstance(this);
        if(clientSocket != null) {
            SocketListener sl = new SocketListener();
            sl.setContextInterface(this);
            NetworkListener = new Thread(sl);
            NetworkListener.start();
        } else {
            startButton.setEnabled(false);
            // Disable the create button.
        }
        /*
        get array of rooms from server to join, as well as add buttonListener to all of them. Not sure of your structure so you have to do it yourself
       Alternatively, remove the listview from activity_join_room.xml, create button for each room in your server

         */
    }

    public void showToastMessage(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
    }

    class SocketListener implements Runnable {

        private Context c;
        private boolean running = true;

        public SocketListener() {
            running = true;
        }

        public void setContextInterface(Context c) {
            this.c = c;
        }

        private boolean firstRun = true;

        @Override
        public void run() {
            Intent intentAction = new Intent(c, GameRoomActivity.class);
            while(running) {
                try {
                    if(firstRun) {
                        sendBytes((byte)100, name);
                        firstRun = false;
                    }
                    byte[] fromClient = readBytes();
                    byte command = fromClient[0];
                    byte[] value = new byte[fromClient.length - 1];
                    for (int i = 1; i < fromClient.length; i++) {
                        value[i - 1] = fromClient[i];
                    }
                    String stringValue = new String(value);
                    System.out.println("Command #" + command + " - Value: " + stringValue);
                    switch(command) {
                        case Command.JOIN:
                            if(stringValue.equalsIgnoreCase("Error")) {
                                showToastMessage("Unable to locate room");
                            } else {
                                intentAction.putExtra("roomId", roomId);
                                intentAction.putExtra("roomName", stringValue);
                                intentAction.putExtra("host", false);
                            }
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
                System.out.println("Loop");
                if((intentAction.hasExtra("roomId"))&&(intentAction.hasExtra("roomName"))&&(intentAction.hasExtra("listPlayer"))&&(intentAction.hasExtra("host"))) {
                    running = false;
                    System.out.println("Intent extra");
                }
            }
            startActivity(intentAction);
            finish();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        NetworkListener.interrupt();
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
        InputStream in = clientSocket.getInputStream();
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
        getMenuInflater().inflate(R.menu.menu_join_room, menu);
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
