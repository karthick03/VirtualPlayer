package com.android.lab.virtualplayer.server;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class WifiAPManager {
    private WifiManager wifiManager;
    private Method isWifiApEnabled;
    private Method getWifiApState;
    private Method getWifiApConfiguration;
    private Method setWifiApEnabled;

    WifiAPManager(WifiManager wifiManager) {
        this.wifiManager = wifiManager;

        initMethods(wifiManager.getClass());
    }

    private void initMethods(Class<? extends WifiManager> wifiManagerClass) {
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
