package com.example.musicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class FavoriteSongsAdapter extends RecyclerView.Adapter<FavoriteSongsAdapter.ViewHolder> {

    private List<AudioModel> favoriteSongs;
    private OnItemClickListener onItemClickListener;
    private Context context;
    private DBHelper dbHelper; // Add DBHelper instance
    private static final int REQUEST_CODE = 100;


    // Interface for handling item clicks
    public interface OnItemClickListener {
        void onItemClick(AudioModel song, String songTitle);
    }

    // Adapter constructor with the listener parameter
    public FavoriteSongsAdapter(Context context, List<AudioModel> favoriteSongs, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.favoriteSongs = favoriteSongs;
        this.onItemClickListener = onItemClickListener;
        this.dbHelper = new DBHelper(context); // Initialize DBHelper
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AudioModel song = favoriteSongs.get(position);

        // Get the file name from the path and remove the extension
        String fileName = getFileNameWithoutExtension(song.getPath());

        // Display the file name (song title) or "Unknown Title" if it's empty
        holder.songTitle.setText(fileName != null && !fileName.isEmpty() ? fileName : "Unknown Title");

        // Retrieve and format the song duration
        String songPath = song.getPath();
        String songDuration = getSongDuration(songPath);
        holder.musicDuration.setText(songDuration);
        // Set the click listener for the song item
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(song,fileName));
        holder.moreOptions.setOnClickListener(v -> showPopupMenu(v, song, position));
    }

    // Method to show the popup menu
    private void showPopupMenu(View view, AudioModel song, int position) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.song_options_menu); // Define options in a menu resource file

        // Set item click listener for menu items
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.remove_from_favorites) {
                removeFromFavorites(song, position);
                return true;
            } else if (itemId == R.id.share) {
                shareSong(song);
                return true;
            } else if (itemId == R.id.set_as_ringtone) {
                setAsRingtone(song);
                return true;
            } else if (itemId == R.id.delete_song) {
                deleteSong(song, position);
                return true;
            } else {
                return false;
            }
        });


        popupMenu.show();
    }

    private void removeFromFavorites(AudioModel song, int position) {
        // Remove song from database favorites
        dbHelper.removeSongFromFavorites(song.getPath());

        // Remove the song from the list and notify the adapter
        favoriteSongs.remove(position);
        notifyItemRemoved(position);

        // Show confirmation to the user
        Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
    }


    private void shareSong(AudioModel song) {
        // Implement sharing logic here
        Toast.makeText(context, "Sharing " + song.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void setAsRingtone(AudioModel song) {

        try {
            File songFile = new File(song.getPath());
            if (songFile.exists()) {
                // Copy the song file to the Ringtones directory
                File ringtoneDirectory = new File(Environment.getExternalStorageDirectory(), "Ringtones");
                if (!ringtoneDirectory.exists()) {
                    ringtoneDirectory.mkdir();  // Create the directory if it doesn't exist
                }

                File newRingtoneFile = new File(ringtoneDirectory, songFile.getName());
                if (copyFile(songFile, newRingtoneFile)) {
                    // Insert the new ringtone into the MediaStore
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DATA, newRingtoneFile.getAbsolutePath());
                    values.put(MediaStore.MediaColumns.TITLE, song.getTitle());
                    values.put(MediaStore.MediaColumns.SIZE, newRingtoneFile.length());
                    values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3");
                    values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                    values.put(MediaStore.Audio.Media.IS_NOTIFICATION, false);
                    values.put(MediaStore.Audio.Media.IS_ALARM, false);
                    values.put(MediaStore.Audio.Media.IS_MUSIC, false);

                    Uri uri = MediaStore.Audio.Media.getContentUriForPath(newRingtoneFile.getAbsolutePath());
                    context.getApplicationContext().getContentResolver().insert(uri, values);

                    // Set the ringtone using RingtoneManager
                    Uri ringtoneUri = Uri.fromFile(newRingtoneFile);
                    RingtoneManager.setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, ringtoneUri);

                    Toast.makeText(context, "Ringtone set successfully", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to set ringtone", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to copy the song file to the ringtones directory
    private boolean copyFile(File sourceFile, File destinationFile) {
        try (InputStream in = new FileInputStream(sourceFile);
             OutputStream out = new FileOutputStream(destinationFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    private void deleteSong(AudioModel song, int position) {
        File file = new File(song.getPath());

        if (file.exists()) {
            if (file.delete()) {
                // Remove the song from the list and notify the adapter
                favoriteSongs.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Song deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete song", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show();
        }
    }


    private String getSongDuration(String songPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(songPath);
            // Get the duration in milliseconds
            long durationMillis = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

            // Convert milliseconds to minutes:seconds format
            int minutes = (int) (durationMillis / 1000 / 60);
            int seconds = (int) (durationMillis / 1000 % 60);

            // Return the formatted duration
            return String.format("%02d:%02d", minutes, seconds);
        } catch (Exception e) {
            e.printStackTrace();
            return "00:00"; // Return "00:00" if there's an error
        }
    }


    // Helper method to extract the file name without extension
    private String getFileNameWithoutExtension(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            // Extract file name from the path
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

            // Remove file extension if exists
            int dotIndex = fileName.lastIndexOf(".");
            if (dotIndex > 0) {
                return fileName.substring(0, dotIndex); // Return file name without extension
            }
            return fileName; // Return file name as it is if there's no extension
        }
        return null;
    }



    @Override
    public int getItemCount() {
        return favoriteSongs.size();
    }

    // ViewHolder class to hold the views for each item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView songTitle, musicDuration;
        ImageView moreOptions;

        public ViewHolder(View itemView) {
            super(itemView);
            // Initialize the song title TextView
            songTitle = itemView.findViewById(R.id.music_title);
            musicDuration = itemView.findViewById(R.id.music_duration);
            moreOptions = itemView.findViewById(R.id.more); // Initialize the more options button
        }
    }
}


