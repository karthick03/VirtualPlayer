package com.android.lab.virtualplayer.server;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.android.lab.virtualplayer.R;
import com.android.lab.virtualplayer.constants.Constants;

import java.io.IOException;

import static com.android.lab.virtualplayer.constants.Constants.NOTIF_ID;

public class ShareService extends IntentService {

    private static final String ACTION_START_SERVER = "server.action.SERVER_START";
    private static final String ACTION_STOP_SERVER = "server.action.SERVER_STOP";
    private static final String ACTION_REFRESH = "server.action.REFRESH";

    private FileServer fileServer;
    private WifiManager wifiManager;
    private WifiAPManager wifiAPManager;
    private NotificationManager manager;
    @Override
    public void onCreate() {
        super.onCreate();
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        wifiAPManager = new WifiAPManager(wifiManager);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    public ShareService() {
        super("ServerService");
    }

    public static void startServer(Context context) {
        Intent intent = new Intent(context, ShareService.class);
        intent.setAction(ACTION_START_SERVER);
        context.startService(intent);
    }

    public static void stopServer(Context context) {
        Intent intent = new Intent(context, ShareService.class);
        intent.setAction(ACTION_STOP_SERVER);
        context.startService(intent);
    }

    public static void refreshServer(Context context) {
        Intent intent = new Intent(context, ShareService.class);
        intent.setAction(ACTION_REFRESH);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null)
            switch (intent.getAction()) {
                case ACTION_START_SERVER:
                    createHotspot();
                    break;
                case ACTION_STOP_SERVER:
                    stopHotspot();
                    break;
                case ACTION_REFRESH:
                    refreshCache();
                    break;
            }
    }

    private void startServer() throws IOException {
        fileServer = new FileServer(Constants.PORT, true);
        fileServer.start();
    }

    private void refreshCache() {
        if (fileServer != null)
            fileServer.refreshCache();
    }

    private void stopServer() {
        if (fileServer != null)
            fileServer.stop();

        manager.cancel(NOTIF_ID);
        stopSelf();
    }

    public void createHotspot() {

        wifiManager.setWifiEnabled(false);

        WifiConfiguration netConfig = new WifiConfiguration();
        netConfig.SSID = "VP_" + Constants.SSID;
        netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        try {
            wifiAPManager.setWifiApEnabled(netConfig, true);
            Notification.Builder mBuilder = new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Share is ON.")
                    .setOngoing(true)
                    .setContentText("Click to view who's sharing!");

            Intent resultIntent = new Intent(this, ShareActivity.class);
            resultIntent.putExtra("started", true);

            PendingIntent i = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentIntent(i);

            manager.notify(NOTIF_ID, mBuilder.build());
            //startForeground(notificationId, mBuilder.build());

            while (wifiAPManager.isWifiApEnabled());

            startServer();


        } catch (Exception e) {
            Log.e(this.getClass().toString(), "", e);
        }
    }

    public void stopHotspot() {
        try {
            wifiAPManager.setWifiApEnabled(null, false);
            stopServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
