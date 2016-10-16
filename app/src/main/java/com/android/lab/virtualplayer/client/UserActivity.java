package com.android.lab.virtualplayer.client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.lab.virtualplayer.R;
import com.android.lab.virtualplayer.SettingsActivity;
import com.android.lab.virtualplayer.constants.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserActivity extends Activity {

    WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
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
//
//        switch (item.getItemId()) {
//            case R.id.action_settings:
//                startActivity(new Intent(UserActivity.this, SettingsActivity.class));
//        }
//
//        return true;
//    }


    public void join(View view) {

        wifiManager.setWifiEnabled(true);

        List<ScanResult> scanResultList =   wifiManager.getScanResults();
        List<ScanResult> finalList = new ArrayList<>();

        for(ScanResult scanResult: scanResultList)
            if (scanResult.SSID.startsWith("VP_"))
                finalList.add(scanResult);

        if(finalList.size() == 1) {
            WifiConfiguration wc = new WifiConfiguration();

            ScanResult s = finalList.get(0);

            wc.SSID = s.SSID;

            wc.status = WifiConfiguration.Status.ENABLED;
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

            int netId = wifiManager.addNetwork(wc);
            wifiManager.enableNetwork(netId, true);
            Log.d(Constants.TAG, String.format(Locale.ENGLISH, "Connected %s %s", s.BSSID, s.toString()));

            startActivity(new Intent(UserActivity.this, MusicActivity.class));
        } else {
            Toast.makeText(getApplicationContext(), "No VP hotspot running!", Toast.LENGTH_LONG).show();
        }

    }
}
