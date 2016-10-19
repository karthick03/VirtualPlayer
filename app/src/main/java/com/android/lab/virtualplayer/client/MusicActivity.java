package com.android.lab.virtualplayer.client;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.android.lab.virtualplayer.R;
import com.android.lab.virtualplayer.constants.Constants;
import com.android.lab.virtualplayer.data.MusicTrack;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicActivity extends Activity {

    private static RequestQueue requestQueue = null;
    private MusicAdapter musicAdapter;
    private MediaPlayer mediaPlayer;

    private ImageButton playButton;
    private TextView playingName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        mediaPlayer = new MediaPlayer();

        playButton = (ImageButton) findViewById(R.id.play);
        playingName = (TextView) findViewById(R.id.playing_name);

        ListView musicListView = (ListView) findViewById(R.id.musicList);
        musicAdapter = new MusicAdapter(getApplicationContext());

        musicListView.setAdapter(musicAdapter);

        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MusicTrack track = musicAdapter.getItem(position);
                updateNowPlaying(track);
                loadSong(track.get_id());
            }
        });

        final JsonObjectRequest getMusicList = new JsonObjectRequest(
                "http://192.168.43.1:" + Constants.PORT,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseMusicList(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(Constants.TAG, error.toString());
                    }
                }
        );

        getMusicList.setRetryPolicy(new DefaultRetryPolicy(2000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, 1));

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                getRequestQueue(getApplicationContext()).add(getMusicList);
            }
        }, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = null;
    }

    public void updateNowPlaying(MusicTrack playingTrack) {
        playButton.setImageResource(R.drawable.ic_play);
        playingName.setText(playingTrack.getName());
    }

    public void loadSong(int id) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource("http://192.168.43.1:" + Constants.PORT + "/files?id=" + id);
            mediaPlayer.setLooping(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static RequestQueue getRequestQueue(Context mContext) {
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(mContext);
        return requestQueue;
    }

    public void parseMusicList(JSONObject jsonObject) {
        try {
            List<MusicTrack> tracks = new ArrayList<>();
            JSONArray files = jsonObject.getJSONArray("files");

            for (int i = 0; i < files.length(); i++)
                tracks.add(MusicTrack.fromJSON(files.getJSONObject(i)));

            musicAdapter.setMusicTracks(tracks);
            musicAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(Constants.TAG, e.getMessage());
        }
    }
}
