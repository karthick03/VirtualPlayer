package com.android.lab.virtualplayer.server;

import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.android.lab.virtualplayer.StorageHelper;
import com.android.lab.virtualplayer.data.MusicTrack;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import static android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST;
import static android.media.MediaMetadataRetriever.METADATA_KEY_DURATION;

class FileServer extends NanoHTTPD {

    private List<MusicTrack> mediaFiles;
    private StorageHelper helper;

    FileServer(int port) {
        super(port);
        mediaFiles = new ArrayList<>();
        helper = new StorageHelper();
    }

    public void refreshCache() {
        mediaFiles = getMusicTracks(getMusicFiles(helper.getInternalSDPath(), helper.getExternalSDPath()));
    }

    @Override
    public void start() throws IOException {
        super.start();
        refreshCache();
    }

    @Override
    public Response serve(IHTTPSession session) {
        String ip = session.getRemoteIpAddress();
        String request = session.getUri();
        Map<String, List<String>> params = session.getParameters();

        Log.d("NanoHTTPD", ip + "  " + request + " " + params);

        try {
            if (request.contentEquals("/files"))
                return params.containsKey("id") ? streamMusic(params.get("id").get(0)) : serveFiles();
        } catch (Exception e) {
            Log.e("NanoHTTPD", e.toString());
        }

        return NanoHTTPD.newFixedLengthResponse("OK");
    }

    private Response streamMusic(String file) throws FileNotFoundException {

        if (file.isEmpty()) return null;

        int id = Integer.parseInt(file);
        if (id > mediaFiles.size() || id <= 0) return null;

        MusicTrack toBePlayed = mediaFiles.get(id - 1);

        return NanoHTTPD.newChunkedResponse(Response.Status.OK, "audio/mpeg3", new FileInputStream(toBePlayed.getRawFile()));
    }

    private Response serveFiles() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for(MusicTrack track: mediaFiles)
            jsonArray.put(track.toJSON());

        return NanoHTTPD.newFixedLengthResponse(jsonArray.toString());
    }

    private List<MusicTrack> getMusicTracks(List<File> mediaFiles) {
        List<MusicTrack> musicTracks = new ArrayList<>();

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        int index = 1;
        for (File file : mediaFiles) {
            retriever.setDataSource(file.getAbsolutePath());
            musicTracks.add(new MusicTrack (
                    index,
                    file.getName(),
                    retriever.extractMetadata(METADATA_KEY_ARTIST),
                    retriever.extractMetadata(METADATA_KEY_DURATION),
                    file)
            );
            index++;
        }

        return musicTracks;
    }

    private List<File> getMusicFiles(String... paths) {
        List<File> music = new ArrayList<>();

        for(String path: paths) {
            File root = new File(path).getAbsoluteFile();
            getFilesRecursively(root, music);
        }
        return music;
    }

    private void getFilesRecursively(File root, List<File> musicList) {
        if(!root.exists()) return;

        if(root.isDirectory())
            for (File f : root.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return true;
                }
            }))
                getFilesRecursively(f, musicList);
        else if (root.getName().contains(".mp3"))
            musicList.add(root);
    }
}

