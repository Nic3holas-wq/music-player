package com.example.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class ViewSongsActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private ListView listViewSongs;
    private TextView playlistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_songs);

        dbHelper = new DBHelper(this);
        listViewSongs = findViewById(R.id.listViewSongs);
        playlistName = findViewById(R.id.playlistName);

        // Get the playlist ID and name from the Intent that started this activity
        int playlistId = getIntent().getIntExtra("playlist_id", -1);
        String playlistNameText = getIntent().getStringExtra("playlist_name");

        if (playlistId == -1) {
            Toast.makeText(this, "Invalid Playlist ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set the playlist name in the TextView
        playlistName.setText(playlistNameText);

        // Get the songs in the selected playlist
        List<String> songs = dbHelper.getSongsInPlaylist(playlistId);

        if (songs.isEmpty()) {
            Toast.makeText(this, "No songs found in this playlist", Toast.LENGTH_SHORT).show();
        } else {
            // Create a list of song titles from the full paths
            List<String> songTitles = new ArrayList<>();
            for (String songPath : songs) {
                String songTitle = songPath.substring(songPath.lastIndexOf("/") + 1, songPath.lastIndexOf("."));
                songTitles.add(songTitle);
            }

            // Set the adapter for the ListView to display the song titles using the custom layout
            SongAdapter songAdapter = new SongAdapter(this, songs);
            listViewSongs.setAdapter(songAdapter);
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Set the current active tab
        bottomNavigationView.setSelectedItemId(R.id.nav_library); // Assuming 'Library' is the current tab
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Handle Home action
                Intent intent = new Intent(ViewSongsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_library) {
                // Handle Library action
                Intent intent = new Intent(ViewSongsActivity.this, ViewPlaylistsActivity.class);
                startActivity(intent);
                finish();

                return true;
            } else if (itemId == R.id.nav_settings) {
                // Handle Settings action
                Intent intent = new Intent(ViewSongsActivity.this, FavoritesActivity.class);
                startActivity(intent);
                finish();
                return true;
            }

            return false;
        });
    }




}
