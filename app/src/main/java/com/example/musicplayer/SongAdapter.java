package com.example.musicplayer;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import java.util.List;

public class SongAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> songs;

    public SongAdapter(Context context, List<String> songs) {
        super(context, R.layout.item_song, songs);
        this.context = context;
        this.songs = songs;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Recycle the view if necessary
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        }

        // Get the song path
        String songPath = songs.get(position);
        Log.d("SongAdapter", "Song path: " + songPath);

        // Extract the song title
        String songTitle = songPath.substring(songPath.lastIndexOf("/") + 1, songPath.lastIndexOf("."));

        // Get the song duration dynamically
        String songDuration = getSongDuration(songPath);

        // Log the song duration
        Log.d("SongAdapter", "Duration: " + songDuration);

        // Set song title and duration
        TextView titleTextView = convertView.findViewById(R.id.music_title);
        titleTextView.setText(songTitle);

        TextView durationTextView = convertView.findViewById(R.id.music_duration);
        durationTextView.setText(songDuration);

        // Set album icon
        ImageView albumIconImageView = convertView.findViewById(R.id.album_icon);
        albumIconImageView.setImageResource(R.drawable.ic_album);

        return convertView;
    }

    // Static method to format the duration in minutes:seconds format
    public static String formatDuration(String durationInMillis) {
        long duration = Long.parseLong(durationInMillis);
        int minutes = (int) (duration / 1000) / 60;
        int seconds = (int) (duration / 1000) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    // Helper method to get song duration
    private String getSongDuration(String songPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(songPath);
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return durationStr != null ? formatDuration(durationStr) : "Error";
        } catch (Exception e) {
            e.printStackTrace();
            return "0:00"; // Default value in case of errors
        }
    }
}
