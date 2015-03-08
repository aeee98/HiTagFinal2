package sg.rods.hitag;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import sg.rods.resources.Command;
import sg.rods.resources.SocketHandler;


public class GameRoomActivity extends Activity {
    private Button startButton;
    private Socket clientSocket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_room);
        clientSocket = SocketHandler.getSocket();
        /*
        for all members in room
        List.Add
        Populate
         */
        Bundle pastBundle = getIntent().getExtras();
        String roomId = pastBundle.getString("roomId");
        String roomName = pastBundle.getString("roomName");
        String playerList = pastBundle.getString("listPlayer");
        RefreshRoom(playerList);
        boolean isHost = pastBundle.getBoolean("host"); // get role. Role is determined by how User get into the room - Create or Join
        String roomTitle = roomId + ":" + roomName;
        this.setTitle(roomTitle);
        //Check If Master, if Master, GameButton at bottom has the functionality to start the game. Otherwise, it will not be clickable
        if(isHost) {
            startButton = (Button) findViewById(R.id.startGameBtn);
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Start Game
                }
            });
        }
       /* SocketListener sl = new SocketListener();
        sl.setContextInterface(this);
        final Thread t = new Thread(sl);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                t.start();
            }
        }, 5000);*/
        /*
        getRoleOfMember
        
        if Master (setBtnClickListener to link to startGameclass, as well as servercode to deploy all members to startGame)
        else startButton.setEnabled(false);
        */

    }



    class SocketListener implements Runnable {


        private Context c;

        public void setContextInterface(Context c) {
            this.c = c;
        }

        @Override
        public void run() {
            while(true) {
                try {
                    byte[] fromClient = readBytes();
                    byte command = fromClient[0];
                    byte[] value = new byte[fromClient.length - 1];
                    for (int i = 1; i < fromClient.length; i++) {
                        value[i - 1] = fromClient[i];
                    }
                    String stringValue = new String(value);
                    switch(command) {
                        case Command.START:
                            break;
                        case Command.ROOM_REFRESH:
                            RefreshRoom(stringValue);
                            break;
                    }
                } catch (InterruptedException e) {
                    //Toast.makeText(c, "Unable to create a room.", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    //Toast.makeText(c, "Are you sure you're connected to the server?", Toast.LENGTH_LONG).show();
                }
            }
        }
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

    public void RefreshRoom(String stringData)
    {
        final String data = stringData;

        runOnUiThread(new Runnable(){
            @Override
            public void run(){
                // change UI elements here
        String[] playerList;
        if(data.contains(",")) {
            playerList = data.split(",");
        } else {
            ArrayList<String> arr = new ArrayList<String>();
            arr.add(data);
            playerList = (String[]) arr.toArray();
        }
        ListView lv = (ListView) findViewById(R.id.playerList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_1, playerList);
        lv.setAdapter(adapter);
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game_room, menu);
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
