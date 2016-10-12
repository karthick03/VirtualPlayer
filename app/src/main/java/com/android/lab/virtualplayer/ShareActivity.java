package com.android.lab.virtualplayer;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.lab.virtualplayer.constants.Constants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;

public class ShareActivity extends AppCompatActivity {

    WifiManager wifiManager;
    WifiApManager wifiApManager;
    FileServer fileServer;
    TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiApManager = new WifiApManager(wifiManager);

        status = (TextView) findViewById(R.id.status);
    }

    public void toggleHotspot(View v) {
        ToggleButton btn = (ToggleButton) v;
        if (!btn.isChecked())
            stop();
        else
            create();
    }

    public void create() {

        wifiManager.setWifiEnabled(false);

        WifiConfiguration netConfig = new WifiConfiguration();
        netConfig.SSID = "VP_Sharer";
        netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        try {
            wifiApManager.setWifiApEnabled(netConfig, true);

            while (wifiApManager.isWifiApEnabled());

            status.setText(R.string.start_server);

            int apstate = wifiApManager.getWifiApState();
            netConfig = wifiApManager.getWifiApConfiguration();

            fileServer = new FileServer(Constants.PORT);
            fileServer.start();

            status.setText(String.format(Locale.ENGLISH, "Server running at http://%s:%d", getHostAddress(), Constants.PORT));
            Log.d("VP_Sharer", String.format("SSID = %s Password = %s APState = %d Hostname = %s", netConfig.SSID, netConfig.preSharedKey, apstate, getHostAddress()));

        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
            status.setText(R.string.error_server);
        }
    }

    public void stop() {
        try {
            wifiApManager.setWifiApEnabled(null, false);
            fileServer.stop();
            status.setText(R.string.stop_server);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String getHostAddress() throws SocketException {
        Enumeration<NetworkInterface> network = NetworkInterface.getNetworkInterfaces();

        while(network.hasMoreElements()) {
            Enumeration<InetAddress> addresses =  network.nextElement().getInetAddresses();
            while(addresses.hasMoreElements()) {
                String hostAddress = addresses.nextElement().getHostAddress();
                if(hostAddress.startsWith("192"))
                    return hostAddress;
            }
        }

        return null;
    }

    private class WifiApManager {
        WifiManager wifiManager;
        Method isWifiApEnabled;
        Method getWifiApState;
        Method getWifiApConfiguration;
        Method setWifiApEnabled;

        WifiApManager(WifiManager wifiManager) {
            this.wifiManager = wifiManager;

            initMethods((Class<WifiManager>) wifiManager.getClass());
        }

        void initMethods(Class<WifiManager> wifiManagerClass) {
            try {
                setWifiApEnabled = wifiManagerClass.getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                isWifiApEnabled = wifiManagerClass.getMethod("isWifiApEnabled");
                getWifiApState = wifiManagerClass.getMethod("getWifiApState");
                getWifiApConfiguration = wifiManagerClass.getMethod("getWifiApConfiguration");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void setWifiApEnabled(WifiConfiguration configuration, boolean enable) throws InvocationTargetException, IllegalAccessException {
            setWifiApEnabled.invoke(wifiManager, configuration, enable);
        }

        boolean isWifiApEnabled() throws InvocationTargetException, IllegalAccessException {
            return (boolean) isWifiApEnabled.invoke(wifiManager);
        }

        int getWifiApState() throws InvocationTargetException, IllegalAccessException {
            return (int) getWifiApState.invoke(wifiManager);
        }

        WifiConfiguration getWifiApConfiguration() throws InvocationTargetException, IllegalAccessException {
            return (WifiConfiguration) getWifiApConfiguration.invoke(wifiManager);
        }
    }
}