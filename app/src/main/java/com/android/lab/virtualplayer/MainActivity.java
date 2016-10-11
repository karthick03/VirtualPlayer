package com.android.lab.virtualplayer;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

	WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }

        return true;
    }

    public void create(View view) {

        wifiManager.setWifiEnabled(false);
        
        WifiConfiguration netConfig = new WifiConfiguration();
		netConfig.SSID = "MusicSharerUser";
        netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        try {
            Method setWifiApMethod = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean apstatus = (Boolean) setWifiApMethod.invoke(wifiManager, netConfig, true);

            //wifiManager.updateNetwork()

            Method isWifiApEnabledmethod = wifiManager.getClass().getMethod("isWifiApEnabled");

            while (!(Boolean)isWifiApEnabledmethod.invoke(wifiManager));
            
			Method getWifiApStateMethod = wifiManager.getClass().getMethod("getWifiApState");
            int apstate=(Integer)getWifiApStateMethod.invoke(wifiManager);
            
			Method getWifiApConfigurationMethod = wifiManager.getClass().getMethod("getWifiApConfiguration");
            netConfig = (WifiConfiguration) getWifiApConfigurationMethod.invoke(wifiManager);
            
			Log.d("CLIENT", "\nSSID:" + netConfig.SSID + "\nPassword:" + netConfig.preSharedKey + "\n");
            Log.d("Details", wifiManager.getScanResults().toString());
        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
        }
    }

    public void join(View view) {
        wifiManager.getScanResults();

		// setup a wifi configuration
        WifiConfiguration wc = new WifiConfiguration();
        
		wc.SSID = "\"MusicSharerUser\"";
        wc.status = WifiConfiguration.Status.ENABLED;
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        
		// connect to and enable the connection
        int netId = wifiManager.addNetwork(wc);
        wifiManager.enableNetwork(netId, true);
        wifiManager.setWifiEnabled(true);
    }
}
