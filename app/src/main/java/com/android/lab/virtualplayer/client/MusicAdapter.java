package com.android.lab.virtualplayer.client;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.lab.virtualplayer.R;
import com.android.lab.virtualplayer.data.MusicTrack;

import java.util.ArrayList;
import java.util.List;

class MusicAdapter extends BaseAdapter {

    void setMusicTracks(List<MusicTrack> musicTracks) {
        this.musicTracks = musicTracks;
    }

    private List<MusicTrack> musicTracks;
    private LayoutInflater layoutInflater;

    MusicAdapter(Context context) {
        this.musicTracks = new ArrayList<>();
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return musicTracks.size();
    }

    @Override
    public MusicTrack getItem(int position) {
        return musicTracks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MusicTrack track = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.music_item, null, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.name);
        name.setText(track.getName());

        TextView artist = (TextView) convertView.findViewById(R.id.artist);
        artist.setText(track.getArtistName());

        TextView duration = (TextView) convertView.findViewById(R.id.duration);
        duration.setText(prettyTime(track.getDuration()));

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return musicTracks.size() == 0;
    }

    private String prettyTime(String milliseconds) {
        int sec = Integer.parseInt(milliseconds) / 1000;
        return "" + (sec / 60) + ":" + (sec % 60);
    }
}
