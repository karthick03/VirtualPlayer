package com.android.lab.virtualplayer.server;

import android.media.MediaMetadataRetriever;

import com.android.lab.virtualplayer.data.MusicTrack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST;
import static android.media.MediaMetadataRetriever.METADATA_KEY_DURATION;

public class MusicManager {

    private List<MusicTrack> mediaFiles = null;

    MusicManager() {
        mediaFiles = new ArrayList<>();
    }

    public List<MusicTrack> getList() {
        return mediaFiles;
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
            for (File f : root.listFiles())
                getFilesRecursively(f, musicList);
        else if (root.getName().contains(".mp3"))
            musicList.add(root);
    }

    public void refreshCache(String... paths) {
        mediaFiles = getMusicTracks(getMusicFiles(paths));
    }

}
