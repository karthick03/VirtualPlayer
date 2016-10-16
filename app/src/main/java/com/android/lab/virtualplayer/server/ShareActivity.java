package com.android.lab.virtualplayer.server;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.lab.virtualplayer.R;
import com.android.lab.virtualplayer.SettingsActivity;
import com.android.lab.virtualplayer.constants.Constants;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;

public class ShareActivity extends Activity {

    TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        status = (TextView) findViewById(R.id.status);

        Intent intent = getIntent();
        boolean started = intent.getBooleanExtra("started", false);

        if(started) {
            ((ToggleButton)findViewById(R.id.hotspotToggle)).setChecked(true);
            status.setText(String.format(Locale.ENGLISH, "Server running at http://%s:%d", getHostAddress(), Constants.PORT));
        }

    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = this.getMenuInflater();
//        inflater.inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_settings:
//                startActivity(new Intent(ShareActivity.this, SettingsActivity.class));
//        }
//        return true;
//    }

    public void toggleHotspot(View v) {
        ToggleButton btn = (ToggleButton) v;
        if (!btn.isChecked())
            stop();
        else
            create();
    }

    public void create() {
        ShareService.startServer(this);
    }

    public void stop() {
        ShareService.stopServer(this);
        status.setText(R.string.stop_server);
    }

    String getHostAddress() {
        try {
            Enumeration<NetworkInterface> network = NetworkInterface.getNetworkInterfaces();

            while(network.hasMoreElements()) {
                Enumeration<InetAddress> addresses =  network.nextElement().getInetAddresses();
                while(addresses.hasMoreElements()) {
                    String tAddress = addresses.nextElement().getHostAddress();
                    if(tAddress.startsWith("192"))
                        return tAddress;
                }
            }
        } catch (SocketException e) {
            return null;
        }
        return null;
    }
}