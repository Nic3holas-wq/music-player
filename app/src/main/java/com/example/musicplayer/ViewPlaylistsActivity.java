package com.example.musicplayer;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class ViewPlaylistsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPlaylists;
    private PlaylistAdapter playlistAdapter;
    private DBHelper dbHelper;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_playlists);

        // Initialize the DBHelper to access the database
        dbHelper = new DBHelper(this);

        // Set linear background
        RelativeLayout layout = findViewById(R.id.view_playlist); // Replace with your root layout ID

        // Create a gradient drawable programmatically with blue and white colors
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.BL_TR,  // Diagonal gradient from bottom-left to top-right
                new int[]{0xFFFF5733, 0xFF4A90E2} // Blue to White gradient
        );

        layout.setBackground(gradientDrawable);

        // Initialize RecyclerView
        recyclerViewPlaylists = findViewById(R.id.recyclerViewPlaylists);
        recyclerViewPlaylists.setLayoutManager(new LinearLayoutManager(this));

        //going back icon funcionality
        back = findViewById(R.id.back_icon);
        back.setOnClickListener(v -> {
            Intent intent = new Intent(ViewPlaylistsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
        // Get playlists and playlist IDs from the database
        List<String> playlists = dbHelper.getPlaylists(); // Get playlist names
        List<Integer> playlistIds = dbHelper.getPlaylistIds(); // Get playlist IDs

        // Check if there are playlists
        if (playlists != null && !playlists.isEmpty()) {
            // Set the adapter to the RecyclerView
            playlistAdapter = new PlaylistAdapter(this, playlists, playlistIds);
            recyclerViewPlaylists.setAdapter(playlistAdapter);
        } else {
            // Show a message if no playlists are found
            Toast.makeText(this, "No playlists available", Toast.LENGTH_SHORT).show();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Set the current active tab
        bottomNavigationView.setSelectedItemId(R.id.nav_library); // Assuming 'Library' is the current tab
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Handle Home action
                Intent intent = new Intent(ViewPlaylistsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_library) {
                // Handle Library action
                Intent intent = new Intent(ViewPlaylistsActivity.this, ViewPlaylistsActivity.class);
                startActivity(intent);
                finish();

                return true;
            } else if (itemId == R.id.nav_settings) {
                // Handle Settings action
                Intent intent = new Intent(ViewPlaylistsActivity.this, FavoritesActivity.class);
                startActivity(intent);
                finish();
                return true;
            }

            return false;
        });
    }

}
