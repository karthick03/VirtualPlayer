package com.android.lab.virtualplayer.server;

import android.util.Log;

import com.android.lab.virtualplayer.StorageHelper;
import com.android.lab.virtualplayer.constants.Constants;
import com.android.lab.virtualplayer.data.MusicTrack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

class FileServer extends NanoHTTPD {

    private MusicManager musicManager;
    private StorageHelper helper;
    private Map<String, Integer> clients;

    FileServer(int port) {
        super(port);
        musicManager = new MusicManager();
        helper = new StorageHelper();
        clients = new HashMap<>();
    }

    void refreshCache() {
        musicManager.refreshCache(helper.getInternalSDPath(), helper.getExternalSDPath());
    }

    @Override
    public void start() throws IOException {
        super.start();
        refreshCache();
    }

    @Override
    public Response serve(IHTTPSession session) {
        try {
            switch (session.getUri()) {
                case "/files":
                    Log.d(Constants.TAG, "Serving files");
                    Map<String, List<String>> params = session.getParameters();
                    if (params.containsKey("id")) {
                        int fid = Integer.parseInt(params.get("id").get(0));
                        clients.put(session.getRemoteIpAddress(), fid);
                        return streamMusic(fid);
                    } else
                        return serveFiles();
                case "/clients":
                    return getClients();
                default:
                    return NanoHTTPD.newFixedLengthResponse("OK");
            }
        } catch (Exception e) {
            Log.e("NanoHTTPD", e.toString());
        }

        return NanoHTTPD.newFixedLengthResponse("OK");
    }

    private Response streamMusic(int fid) throws FileNotFoundException {

        if (fid > musicManager.getList().size() || fid <= 0) return null;

        MusicTrack toBePlayed = musicManager.getList().get(fid - 1);

        return NanoHTTPD.newChunkedResponse(Response.Status.OK, "audio/mpeg3", new FileInputStream(toBePlayed.getRawFile()));
    }

    private Response serveFiles() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        Log.d(Constants.TAG, musicManager.getList().size() + "");
        for(MusicTrack track: musicManager.getList())
            jsonArray.put(track.toJSON());

        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt("files", jsonArray);

        Log.d(Constants.TAG, jsonObject.toString());
        return NanoHTTPD.newFixedLengthResponse(jsonObject.toString());
    }

    private Response getClients() throws JSONException {
        JSONArray jsonArray = new JSONArray();

        Log.d(Constants.TAG, "Serving /clients");

        for (String key : clients.keySet()) {
            JSONObject client = new JSONObject();
            client.put("ip", key);
            String song_name = musicManager.getList().get(clients.get(key)).getName();
            client.put("song", song_name);
            jsonArray.put(client);
        }

        Log.d(Constants.TAG, jsonArray.toString());

        return NanoHTTPD.newFixedLengthResponse(jsonArray.toString());
    }
}

