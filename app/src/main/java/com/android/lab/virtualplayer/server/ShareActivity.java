package com.android.lab.virtualplayer.server;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.lab.virtualplayer.R;
import com.android.lab.virtualplayer.client.MusicActivity;
import com.android.lab.virtualplayer.constants.Constants;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class ShareActivity extends Activity {

    TextView status;
    ListView clients;
    ClientsAdapter clientsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        status = (TextView) findViewById(R.id.people_using);

        clients = (ListView) findViewById(R.id.clients);

        clientsAdapter = new ClientsAdapter(getApplicationContext());

        clients.setAdapter(clientsAdapter);


        Intent intent = getIntent();
        boolean started = intent.getBooleanExtra("started", false);

        if(started) {
            ((ToggleButton)findViewById(R.id.hotspotToggle)).setChecked(true);
        }

        final Handler handler = new Handler();

        final Runnable b = new Runnable() {
            @Override
            public void run() {
                MusicActivity.getRequestQueue(getApplicationContext()).add(new JsonArrayRequest(
                        "http://192.168.43.1:" + Constants.PORT + "/clients",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                List<String[]> cl = new ArrayList<>();
                                try {
                                    for (int i =0; i < response.length(); i++) {
                                        JSONObject jsonObject = response.getJSONObject(i);
                                        cl.add(new String[] {jsonObject.getString("ip"), jsonObject.getString("song")});
                                    }
                                    clientsAdapter.setClients(cl);
                                    clientsAdapter.notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        }
                ));
                handler.postDelayed(this, 8000);
            }
        };
        handler.postDelayed(b, 10000);

    }

    public void toggleHotspot(View v) {
        ToggleButton btn = (ToggleButton) v;
        if (!btn.isChecked())
            stop();
        else
            create();
    }

    public void create() {
        ShareService.startServer(this);
        status.setText(R.string.people_listening_to_your_music);
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

    class ClientsAdapter extends BaseAdapter {

        private List<String[]> clients;
        private LayoutInflater inflater;

        ClientsAdapter(Context mContext) {

            this.inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
            this.clients = new ArrayList<>();
        }

        void setClients(List<String[]> clients) {
            this.clients = clients;
        }

        @Override
        public int getCount() {
            return clients.size();
        }

        @Override
        public String[] getItem(int position) {
            return clients.get(position);
        }

        @Override
        public long getItemId(int position) {
            return Arrays.hashCode(getItem(position));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            String[] details = getItem(position);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.music_item, null, false);
            }

            TextView name = (TextView) convertView.findViewById(R.id.name);
            name.setText(details[1]);

            TextView artist = (TextView) convertView.findViewById(R.id.artist);
            artist.setText(details[0]);

            convertView.findViewById(R.id.duration).setVisibility(View.GONE);

            return convertView;
        }
    }
}