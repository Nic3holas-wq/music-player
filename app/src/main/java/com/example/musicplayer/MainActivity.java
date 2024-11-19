package com.example.musicplayer;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView noMusicTextView, sortText, songsText, playlistLink, favorites;
    ArrayList<AudioModel> songsList = new ArrayList<>();
    ArrayList<AudioModel> originalSongsList = new ArrayList<>(); // Unsorted list backup

    ImageView sortIcon, searchIcon, settingIcon;
    SearchView searchBar;

    MediaPlayer mediaPlayer;
    boolean isPlaying = false;

    // SharedPreferences to store sorting preference
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        noMusicTextView = findViewById(R.id.no_songs_text);
        sortIcon = findViewById(R.id.sort_icon);
        searchIcon = findViewById(R.id.search_icon);
        searchBar = findViewById(R.id.search_bar);
        sortText = findViewById(R.id.sort_text);
        songsText = findViewById(R.id.songs_text);
        settingIcon = findViewById(R.id.setting_icon);
        playlistLink = findViewById(R.id.playlistLink);
        favorites = findViewById(R.id.favorites);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("SortPreferences", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if (!checkPermission()) {
            requestPermission();
            return;
        }

        // Query to fetch songs
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_MODIFIED
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null
        );

        // Set gradient background for Now Playing Bar
        LinearLayout layout = findViewById(R.id.now_playing_bar);
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                new int[]{0xFF4A90E2, 0xFFFF5733}
        );
        layout.setBackground(gradientDrawable);

        // Populate songsList
        while (cursor.moveToNext()) {
            AudioModel songData = new AudioModel(cursor.getString(1), cursor.getString(0), cursor.getString(2));
            if (new File(songData.getPath()).exists())
                songsList.add(songData);
        }

        // navigating to favorites screen
        favorites.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        //navigating to playlist screen
        playlistLink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ViewPlaylistsActivity.class);
            startActivity(intent);
            finish();
        });

        // Backup the unsorted list
        originalSongsList.addAll(songsList);

        if (songsList.isEmpty()) {
            noMusicTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new MusicListAdapter(songsList, getApplicationContext()));
        }

        // Set the default sorting based on saved preference
        setDefaultSorting();

        // Sort icon click listener
        sortIcon.setOnClickListener(v -> showSortOptionsDialog());

        // Search icon click listener
        searchIcon.setOnClickListener(v -> {
            if (searchBar.getVisibility() == View.GONE) {
                searchBar.setVisibility(View.VISIBLE);
                songsText.setVisibility(View.GONE);
                playlistLink.setVisibility(View.GONE);
                settingIcon.setVisibility(View.GONE);
                favorites.setVisibility(View.GONE);
                searchIcon.setImageResource(R.drawable.close); // Change to close icon
            } else {
                searchBar.setVisibility(View.GONE);
                songsText.setVisibility(View.VISIBLE);
                playlistLink.setVisibility(View.VISIBLE);
                settingIcon.setVisibility(View.VISIBLE);
                favorites.setVisibility(View.VISIBLE);
                searchIcon.setImageResource(R.drawable.search_black); // Change to search icon
            }
        });

        // SearchView query listener
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterSongs(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSongs(newText);
                return false;
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Handle Home action
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_library) {
                // Handle Library action
                Intent intent = new Intent(MainActivity.this, ViewPlaylistsActivity.class);
                startActivity(intent);
                finish();

                return true;
            } else if (itemId == R.id.nav_settings) {
                // Handle Settings action
                Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
                startActivity(intent);
                finish();
                return true;
            }

            return false;
        });


    }

    // Display a dialog with sorting options
    private boolean reverseOrder = false;

    // Display a dialog with sorting options
    private void showSortOptionsDialog() {
        // Inflate custom layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sort_options, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Get references to UI elements in the dialog
        RadioGroup sortOptionsGroup = dialogView.findViewById(R.id.sort_options_group);
        CheckBox reverseCheckBox = dialogView.findViewById(R.id.reverse_order_checkbox);
        Button okButton = dialogView.findViewById(R.id.ok_button);

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // OK button click listener to apply sorting
        okButton.setOnClickListener(v -> {
            boolean reverseOrder = reverseCheckBox.isChecked();
            int selectedOptionId = sortOptionsGroup.getCheckedRadioButtonId();

            // Determine sorting based on the selected radio button
            if (selectedOptionId == R.id.sort_date_modified) {
                sortByDateModified(reverseOrder);
                saveSortingPreference("date_modified", reverseOrder);
            } else if (selectedOptionId == R.id.sort_length) {
                sortByLength(reverseOrder);
                saveSortingPreference("length", reverseOrder);
            } else if (selectedOptionId == R.id.sort_alphabetically) {
                sortAlphabetically(reverseOrder);
                saveSortingPreference("alphabetically", reverseOrder);
            }

            dialog.dismiss(); // Close dialog after sorting
        });
    }

    // Modified sort methods to accept a reverseOrder parameter

    // Sort by Date Modified
    private void sortByDateModified(boolean reverseOrder) {
        Collections.sort(songsList, (song1, song2) -> {
            int compareResult = Long.compare(
                    new File(song2.getPath()).lastModified(),
                    new File(song1.getPath()).lastModified()
            );
            return reverseOrder ? -compareResult : compareResult;
        });
        sortText.setText("Sorted by Date Modified" + (reverseOrder ? " (Reversed)" : ""));
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    // Sort by Length of Song
    private void sortByLength(boolean reverseOrder) {
        Collections.sort(songsList, (song1, song2) -> {
            int compareResult = Integer.compare(
                    Integer.parseInt(song1.getDuration()),
                    Integer.parseInt(song2.getDuration())
            );
            return reverseOrder ? -compareResult : compareResult;
        });
        sortText.setText("Sorted by Length" + (reverseOrder ? " (Reversed)" : ""));
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    // Sort Alphabetically
    private void sortAlphabetically(boolean reverseOrder) {
        Collections.sort(songsList, (song1, song2) -> {
            int compareResult = song1.getTitle().compareToIgnoreCase(song2.getTitle());
            return reverseOrder ? -compareResult : compareResult;
        });
        sortText.setText("Sorted Alphabetically" + (reverseOrder ? " (Reversed)" : ""));
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    // Save the sorting preference in SharedPreferences
    private void saveSortingPreference(String sortBy, boolean reverseOrder) {
        editor.putString("sort_by", sortBy);
        editor.putBoolean("reverse_order", reverseOrder);
        editor.apply();
    }

    // Set default sorting based on saved preferences
    private void setDefaultSorting() {
        String sortBy = sharedPreferences.getString("sort_by", "date_modified");
        boolean reverseOrder = sharedPreferences.getBoolean("reverse_order", false);

        if (sortBy.equals("date_modified")) {
            sortByDateModified(reverseOrder);
        } else if (sortBy.equals("length")) {
            sortByLength(reverseOrder);
        } else if (sortBy.equals("alphabetically")) {
            sortAlphabetically(reverseOrder);
        }
    }

    // Play the selected song
    public void playSong(AudioModel song) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, Uri.parse(song.getPath()));
        mediaPlayer.start();
        isPlaying = true;
    }

    // Filter songs based on search query
    private void filterSongs(String query) {
        ArrayList<AudioModel> filteredList = new ArrayList<>();
        for (AudioModel song : originalSongsList) {
            if (song.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(song);
            }
        }
        songsList.clear();
        songsList.addAll(filteredList);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    // Permission check and request methods
    private boolean checkPermission() {
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return permissionStatus == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }
}
