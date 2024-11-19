package com.example.musicplayer;
import android.widget.LinearLayout;
import android.graphics.drawable.GradientDrawable;
import android.content.SharedPreferences;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MusicPlayerActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MusicPlayerPrefs";
    private static final String REPEAT_KEY = "repeat";
    private static final String SHUFFLE_KEY = "shuffle";

    TextView titleTv, currentTimeTv, totalTimeTv;
    SeekBar seekBar;
    ImageView pausePlay, nextBtn, previousBtn, musicIcon, equaliser, listAddIcon, playlist, repeat, shuffle, favorite;
    ArrayList<AudioModel> songsList;
    AudioModel currentSong;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    DBHelper dbHelper;
    int x = 0;

    boolean isShuffleEnabled;
    boolean isRepeatEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        // Initialize views
        titleTv = findViewById(R.id.song_title);
        currentTimeTv = findViewById(R.id.current_time);
        totalTimeTv = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
        pausePlay = findViewById(R.id.pause_play);
        nextBtn = findViewById(R.id.next);
        previousBtn = findViewById(R.id.previous);
        musicIcon = findViewById(R.id.music_icon_big);
        equaliser = findViewById(R.id.equaliser);
        listAddIcon = findViewById(R.id.list_add);
        dbHelper = new DBHelper(this);
        playlist = findViewById(R.id.playlist);
        repeat = findViewById(R.id.repeat);
        shuffle = findViewById(R.id.shuffle);
        favorite = findViewById(R.id.like);


        // Retrieve the list of favorite songs
        List<AudioModel> favoriteSongs = (List<AudioModel>) getIntent().getSerializableExtra("LIST");


        //set linear background
        RelativeLayout layout = findViewById(R.id.music_player_activity); // Replace with your root layout ID

        // Create a gradient drawable programmatically
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.BL_TR,  // Diagonal gradient
                new int[]{0xFFFF5733, 0xFF4A90E2}    // Start and end colors
        );

        layout.setBackground(gradientDrawable);
        // Load saved states
        loadPreferences();

        // Set initial icons based on saved states
        setRepeatIcon();
        setShuffleIcon();

        // Repeat and shuffle button listeners
        repeat.setOnClickListener(v -> {
            isRepeatEnabled = !isRepeatEnabled;
            setRepeatIcon();
            savePreferences();
        });

        shuffle.setOnClickListener(v -> {
            isShuffleEnabled = !isShuffleEnabled;
            setShuffleIcon();
            savePreferences();
        });



        // Playlist navigation
        playlist.setOnClickListener(v -> {
            Intent intent = new Intent(MusicPlayerActivity.this, ViewPlaylistsActivity.class);
            startActivity(intent);
        });

        // Equaliser navigation
        equaliser.setOnClickListener(v -> {
            Intent intent = new Intent(MusicPlayerActivity.this, Equaliser.class);
            startActivity(intent);
        });

        // Back navigation
        ImageView goBackIcon = findViewById(R.id.go_back);
        goBackIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MusicPlayerActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        titleTv.setSelected(true);
        songsList = (ArrayList<AudioModel>) getIntent().getSerializableExtra("LIST");

        setResourcesWithMusic();

        // Periodically update UI
        MusicPlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    currentTimeTv.setText(convertToMMSS(mediaPlayer.getCurrentPosition() + ""));

                    if (mediaPlayer.isPlaying()) {
                        pausePlay.setImageResource(R.drawable.pause);
                        musicIcon.setRotation(x++);
                    } else {
                        pausePlay.setImageResource(R.drawable.play);
                        musicIcon.setRotation(0);
                    }
                }
                new Handler().postDelayed(this, 100);
            }
        });

        // SeekBar listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Show add to playlist dialog
        listAddIcon.setOnClickListener(v -> showAddToPlaylistDialog());

        // Set favorite icon initial state based on current song
        updateFavoriteIcon();

        // Favorite icon click listener
        favorite.setOnClickListener(v -> toggleFavorite());
    }

    private void updateFavoriteIcon() {
        if (dbHelper.isSongFavorite(currentSong.getPath())) {
            favorite.setImageResource(R.drawable.love);  // Set the icon to 'favorite on'
        } else {
            favorite.setImageResource(R.drawable.favorite_add);  // Set the icon to 'favorite off'
        }
    }

    private void toggleFavorite() {
        if (dbHelper.isSongFavorite(currentSong.getPath())) {
            dbHelper.removeSongFromFavorites(currentSong.getPath());
            favorite.setImageResource(R.drawable.favorite_add);
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.addSongToFavorites(currentSong.getPath());
            favorite.setImageResource(R.drawable.love);
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
        }
    }


    // Load preferences from SharedPreferences
    private void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isRepeatEnabled = sharedPreferences.getBoolean(REPEAT_KEY, false);
        isShuffleEnabled = sharedPreferences.getBoolean(SHUFFLE_KEY, false);
    }

    // Save preferences to SharedPreferences
    private void savePreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(REPEAT_KEY, isRepeatEnabled);
        editor.putBoolean(SHUFFLE_KEY, isShuffleEnabled);
        editor.apply();
    }

    // Set the repeat icon based on the state
    private void setRepeatIcon() {
        if (isRepeatEnabled) {
            repeat.setImageResource(R.drawable.repeat_on);  // Replace with your repeat-on icon
        } else {
            repeat.setImageResource(R.drawable.repeat);  // Replace with your repeat-off icon
        }
    }

    // Set the shuffle icon based on the state
    private void setShuffleIcon() {
        if (isShuffleEnabled) {
            shuffle.setImageResource(R.drawable.shuffle_on);  // Replace with your shuffle-on icon
        } else {
            shuffle.setImageResource(R.drawable.shuffle);  // Replace with your shuffle-off icon
        }
    }

    // Set resources with the current music
    void setResourcesWithMusic() {
        currentSong = songsList.get(MyMediaPlayer.currentIndex);
        titleTv.setText(currentSong.getTitle());
        totalTimeTv.setText(convertToMMSS(currentSong.getDuration()));

        pausePlay.setOnClickListener(v -> pausePlay());
        nextBtn.setOnClickListener(v -> playNextSong());
        previousBtn.setOnClickListener(v -> playPreviousSong());
        updateFavoriteIcon(); // Update favorite icon for the new song
        playMusic();
    }

    // Play music from the current song
    private void playMusic() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();

            seekBar.setProgress(0);
            seekBar.setMax(mediaPlayer.getDuration());

            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(mp -> {
                if (isRepeatEnabled) {
                    playMusic();  // Repeat the current song
                } else {
                    playNextSong();  // Play next song
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Play next song
    private void playNextSong() {
        if (isShuffleEnabled) {
            MyMediaPlayer.currentIndex = (int) (Math.random() * songsList.size());
        } else {
            if (MyMediaPlayer.currentIndex == songsList.size() - 1) return;
            MyMediaPlayer.currentIndex += 1;
        }
        mediaPlayer.reset();
        setResourcesWithMusic();
    }

    // Play previous song
    private void playPreviousSong() {
        if (MyMediaPlayer.currentIndex == 0) return;
        MyMediaPlayer.currentIndex -= 1;
        mediaPlayer.reset();
        setResourcesWithMusic();
    }

    // Pause or play the current song
    private void pausePlay() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    // Convert milliseconds to MM:SS format
    public static String convertToMMSS(String duration) {
        Long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    // Show the dialog to add song to playlist
    private void showAddToPlaylistDialog() {
        List<String> playlists = dbHelper.getPlaylists();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Playlist");

        String[] items = playlists.toArray(new String[0]);
        builder.setItems(items, (dialog, which) -> {
            String selectedPlaylist = items[which];
            dbHelper.addSongToPlaylist(which + 1, currentSong.getPath());
            Toast.makeText(this, "Song added to " + selectedPlaylist, Toast.LENGTH_SHORT).show();
        });

        builder.setPositiveButton("Create New Playlist", (dialog, which) -> showCreatePlaylistDialog());
        builder.show();
    }

    // Show the dialog to create a new playlist
    private void showCreatePlaylistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Playlist");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String playlistName = input.getText().toString();
            if (!playlistName.isEmpty()) {
                dbHelper.createPlaylist(playlistName);
                Toast.makeText(this, "Playlist created", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Playlist name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }
}
