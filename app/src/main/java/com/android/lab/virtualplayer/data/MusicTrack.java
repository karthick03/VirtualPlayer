package com.android.lab.virtualplayer.data;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class MusicTrack {
    private int _id;
    private File rawFile;
    private String name;
    private String artistName;
    private String duration;

    public MusicTrack(int _id, String name, String artistName, String duration, File rawFile) {
        this._id = _id;
        this.name = name;
        this.artistName = artistName;
        this.duration = duration;
        this.rawFile = rawFile;
    }

    public JSONObject toJSON() throws JSONException {
        return new JSONObject()
                .put("_id", this._id)
                .put("name", this.name)
                .put("artist", this.artistName)
                .put("duration", this.duration);
    }

    public static MusicTrack fromJSON(JSONObject jsonObject) throws JSONException {
        String artist = jsonObject.isNull("artist") ? null : jsonObject.getString("artist");
        String duration = jsonObject.getString("duration");
        String name = jsonObject.getString("name");
        int id = jsonObject.getInt("_id");

        return new MusicTrack(
                id,
                name,
                artist,
                duration,
                null
        );
    }

    public int get_id() {
        return _id;
    }

    public File getRawFile() {
        return rawFile;
    }

    public String getName() {
        return name;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getDuration() {
        return duration;
    }
}
