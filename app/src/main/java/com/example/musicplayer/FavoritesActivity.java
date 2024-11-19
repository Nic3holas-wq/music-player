package com.example.musicplayer;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private TextView nowPlayingTitle;
    public String songFilename;
    private ImageView btnPlayPause, btnNext, btnPrevious, back;
    private RecyclerView recyclerViewFavorites;
    private FavoriteSongsAdapter favoriteSongsAdapter;
    private List<AudioModel> favoriteSongs;
    private MediaPlayer mediaPlayer;
    private int currentSongIndex = 0;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        recyclerViewFavorites = findViewById(R.id.recyclerViewFavorites);
        nowPlayingTitle = findViewById(R.id.nowPlayingTitle);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        back = findViewById(R.id.back);

        dbHelper = new DBHelper(this);
        favoriteSongs = dbHelper.getFavoriteSongs();

        //navigate back to main screen
        back.setOnClickListener(v -> {
            Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        favoriteSongsAdapter = new FavoriteSongsAdapter(this,favoriteSongs, (song, songTitle) -> {
            currentSongIndex = favoriteSongs.indexOf(song);
            songFilename = songTitle;
            playSong(currentSongIndex);

            // Set the nowPlayingTitle to the song title from the adapter
            nowPlayingTitle.setText(songFilename);
            findViewById(R.id.nowPlayingBar).setVisibility(View.VISIBLE);
        });


        // Display the number of songs
        int songCount = favoriteSongsAdapter.getItemCount();
        TextView songCountTextView = findViewById(R.id.favoriteSongsCount);
        songCountTextView.setText(songCount + " Songs");

        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFavorites.setAdapter(favoriteSongsAdapter);

        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnNext.setOnClickListener(v -> playNextSong());
        btnPrevious.setOnClickListener(v -> playPreviousSong());

        // Set linear background
        LinearLayout layout = findViewById(R.id.activity_favorites); // Replace with your root layout ID

        // Create a gradient drawable programmatically with blue and white colors
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.BL_TR,  // Diagonal gradient from bottom-left to top-right
                new int[]{0xFFFF5733, 0xFF4A90E2} // Blue to White gradient
        );

        layout.setBackground(gradientDrawable);

        // Set linear background
        LinearLayout layout2 = findViewById(R.id.nowPlayingBar); // Replace with your root layout ID

        // Create a gradient drawable programmatically with blue and white colors
        GradientDrawable gradientDrawable2 = new GradientDrawable(
                GradientDrawable.Orientation.BL_TR,  // Diagonal gradient from bottom-left to top-right
                new int[]{0xFF4A90E2, 0xFFFF5733} // Blue to White gradient
        );

        layout2.setBackground(gradientDrawable2);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Set the current active tab
        bottomNavigationView.setSelectedItemId(R.id.nav_settings); // Assuming 'Library' is the current tab
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Handle Home action
                Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_library) {
                // Handle Library action
                Intent intent = new Intent(FavoritesActivity.this, ViewPlaylistsActivity.class);
                startActivity(intent);
                finish();

                return true;
            } else if (itemId == R.id.nav_settings) {
                // Handle Settings action
                Intent intent = new Intent(FavoritesActivity.this, FavoritesActivity.class);
                startActivity(intent);
                finish();
                return true;
            }

            return false;
        });
    }

    private void playSong(int index) {

        currentSongIndex = index;

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(favoriteSongs.get(index).getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            nowPlayingTitle.setText(songFilename);
            btnPlayPause.setImageResource(R.drawable.pause);

            mediaPlayer.setOnCompletionListener(mp -> playNextSong());

        } catch (IOException e) {
            Toast.makeText(this, "Error playing song", Toast.LENGTH_SHORT).show();
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setImageResource(R.drawable.play);
        } else if (mediaPlayer != null) {
            mediaPlayer.start();
            btnPlayPause.setImageResource(R.drawable.pause);
        }
    }

    private void playNextSong() {
        if (favoriteSongs != null && !favoriteSongs.isEmpty()) {
            currentSongIndex = (currentSongIndex + 1) % favoriteSongs.size();
            playSong(currentSongIndex);
            nowPlayingTitle.setText(songFilename);

        }
    }

    private void playPreviousSong() {
        if (favoriteSongs != null && !favoriteSongs.isEmpty()) {
            currentSongIndex = (currentSongIndex - 1 + favoriteSongs.size()) % favoriteSongs.size();
            playSong(currentSongIndex);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}


