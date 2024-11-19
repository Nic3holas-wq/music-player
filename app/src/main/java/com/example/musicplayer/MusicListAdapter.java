package com.example.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.ViewHolder> {

    ArrayList<AudioModel> songsList;
    Context context;

    public MusicListAdapter(ArrayList<AudioModel> songsList, Context context) {
        this.songsList = songsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AudioModel songData = songsList.get(position);
        holder.titleTextView.setText(songData.getTitle());

        // Convert duration from String to long before formatting
        long durationInMillis = Long.parseLong(songData.getDuration());
        String formattedDuration = formatDuration(durationInMillis);
        holder.durationTextView.setText(formattedDuration);

        // Highlight the currently playing song
        if (MyMediaPlayer.currentIndex == position) {
            holder.titleTextView.setTextColor(Color.parseColor("#FF0000"));
            holder.iconImageView.setImageResource(R.drawable.ic_album); // Set a different icon when playing
        } else {
            holder.titleTextView.setTextColor(Color.parseColor("#000000"));
            holder.iconImageView.setImageResource(R.drawable.ic_album); // Default icon for non-playing items
        }

        // Set click listener for item to play the song and show the Now Playing bar
        holder.itemView.setOnClickListener(v -> {
            // Reset the media player
            MyMediaPlayer.getInstance().reset();
            MyMediaPlayer.currentIndex = position;

            // Call playSong from MainActivity to play the song and show the Now Playing bar
            if (context instanceof MainActivity) {
                ((MainActivity) context).playSong(songData);  // Play the selected song
            }

            // Optionally, open MusicPlayerActivity if needed
            Intent intent = new Intent(context, MusicPlayerActivity.class);
            intent.putExtra("LIST", songsList);
            intent.putExtra("SONG_INDEX", position); // Pass the song index for better tracking
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            // Refresh the RecyclerView to update the highlighted song
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }

    // Helper method to format duration in milliseconds to minutes:seconds format
    public static String formatDuration(long durationInMillis) {
        int minutes = (int) (durationInMillis / 1000) / 60;
        int seconds = (int) (durationInMillis / 1000) % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView durationTextView;
        ImageView iconImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.music_title_text);
            durationTextView = itemView.findViewById(R.id.music_duration_text);
            iconImageView = itemView.findViewById(R.id.icon_view);
        }
    }
}
