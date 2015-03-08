package sg.rods.hitag;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;


public class SettingsActivity extends Activity {

    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        (new Thread(new TCPClientConnection())).start();
    }

    class TCPClientConnection implements Runnable {
        @Override
        public void run() {
            try {
                Socket newSocket = new Socket("cynxtech-sql.cloudapp.net", 3853);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView tvStatus = (TextView) findViewById(R.id.onlineStatusView);
                        tvStatus.setText("Online");
                        tvStatus.setTextColor(getResources().getColor(R.color.green));
                    }
                });
                newSocket.close();
            } catch (UnknownHostException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView tvStatus = (TextView) findViewById(R.id.onlineStatusView);
                        tvStatus.setText("Offline");
                        tvStatus.setTextColor(getResources().getColor(R.color.red));
                    }
                });
            } catch (IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView tvStatus = (TextView) findViewById(R.id.onlineStatusView);
                        tvStatus.setText("Offline");
                        tvStatus.setTextColor(getResources().getColor(R.color.red));
                    }
                });
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SharedPreferences sharedPref = getSharedPreferences("prefsValue", MODE_PRIVATE);
        String displayName = sharedPref.getString("displayName", "Guest");
        btnSave = (Button) findViewById(R.id.savePrefBtn);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor spEdit = getSharedPreferences("prefsValue", MODE_PRIVATE).edit();
                spEdit.clear();
                EditText etDName = (EditText) findViewById(R.id.fullNameTextEdit);
                String text = etDName.getText().toString();
                if(text.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Input your name before saving.", Toast.LENGTH_SHORT).show();
                } else {
                    spEdit.putString("displayName", text);
                    if(spEdit.commit()) {
                        Toast.makeText(getApplicationContext(), "Setting saved.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Try again later.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


}
