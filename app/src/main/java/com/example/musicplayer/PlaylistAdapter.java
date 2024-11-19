package com.example.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private List<String> playlists;  // List of playlist names
    private List<Integer> playlistIds;  // List of playlist IDs
    private Context context;

    // Constructor to initialize the playlists and context
    public PlaylistAdapter(Context context, List<String> playlists, List<Integer> playlistIds) {
        this.context = context;
        this.playlists = playlists;
        this.playlistIds = playlistIds;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the custom layout for each playlist item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        // Set the playlist name for each item
        holder.playlistName.setText(playlists.get(position));

        holder.itemView.setOnClickListener(v -> {
            // Get the playlist ID and name for the clicked item
            int playlistId = playlistIds.get(position);
            String playlistName = playlists.get(position);

            // Create an intent to navigate to ViewSongsActivity
            Intent intent = new Intent(context, ViewSongsActivity.class);

            // Pass the playlistId and playlistName to the next activity
            intent.putExtra("playlist_id", playlistId);
            intent.putExtra("playlist_name", playlistName);

            // Start the new activity
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return playlists.size();
    }

    // ViewHolder class for the RecyclerView
    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView playlistName;

        public PlaylistViewHolder(View itemView) {
            super(itemView);
            // Initialize the playlist name TextView
            playlistName = itemView.findViewById(R.id.playlistTitle);
        }
    }
}
