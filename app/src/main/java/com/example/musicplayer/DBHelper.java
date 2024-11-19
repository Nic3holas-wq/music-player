package com.example.musicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    // Table names
    private static final String TABLE_PLAYLISTS = "Playlists";
    private static final String TABLE_PLAYLIST_SONGS = "PlaylistSongs";
    private static final String TABLE_FAVORITES = "favorites";

    // Playlist columns
    private static final String COLUMN_PLAYLIST_ID = "id";
    private static final String COLUMN_PLAYLIST_NAME = "name";

    // PlaylistSongs columns
    private static final String COLUMN_PLAYLIST_SONG_PLAYLIST_ID = "playlistId";
    private static final String COLUMN_PLAYLIST_SONG_PATH = "songPath";

    // Favorites columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_SONG_PATH = "song_path";

    public DBHelper(Context context) {
        super(context, "MusicDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Playlists table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PLAYLISTS + " ("
                + COLUMN_PLAYLIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_PLAYLIST_NAME + " TEXT)");

        // Create PlaylistSongs table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PLAYLIST_SONGS + " ("
                + COLUMN_PLAYLIST_SONG_PLAYLIST_ID + " INTEGER, "
                + COLUMN_PLAYLIST_SONG_PATH + " TEXT, "
                + "FOREIGN KEY(" + COLUMN_PLAYLIST_SONG_PLAYLIST_ID + ") REFERENCES " + TABLE_PLAYLISTS + "(" + COLUMN_PLAYLIST_ID + "))");

        // Create Favorites table
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_FAVORITES + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SONG_PATH + " TEXT UNIQUE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST_SONGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }


    // Add song to favorites
    public void addSongToFavorites(String songPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SONG_PATH, songPath);
        db.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    // Check if song is in favorites
    public boolean isSongFavorite(String songPath) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, new String[]{COLUMN_ID},
                COLUMN_SONG_PATH + "=?", new String[]{songPath},
                null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        db.close();
        return exists;
    }

    // Remove song from favorites
    public void removeSongFromFavorites(String songPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, COLUMN_SONG_PATH + "=?", new String[]{songPath});
        db.close();
    }

    // Add this method to DBHelper class
    public List<AudioModel> getFavoriteSongs() {
        List<AudioModel> favoriteSongs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to get all songs from the favorites table
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_SONG_PATH + " FROM " + TABLE_FAVORITES, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Retrieve the song path from the cursor
                String songPath = cursor.getString(cursor.getColumnIndex(COLUMN_SONG_PATH));

                // Create an AudioModel object for each song
                AudioModel song = new AudioModel();
                song.setPath(songPath);  // Set the path (no need to set title for now)

                // Add the song to the list
                favoriteSongs.add(song);
            } while (cursor.moveToNext());
        }

        // Close the cursor to release the resources
        if (cursor != null) {
            cursor.close();
        }

        // Return the list of favorite songs
        return favoriteSongs;
    }




    // Create a new playlist
    public long createPlaylist(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PLAYLIST_NAME, name);
        return db.insert(TABLE_PLAYLISTS, null, values);
    }

    // Add a song to a playlist
    public void addSongToPlaylist(int playlistId, String songPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PLAYLIST_SONG_PLAYLIST_ID, playlistId);
        values.put(COLUMN_PLAYLIST_SONG_PATH, songPath);
        db.insert(TABLE_PLAYLIST_SONGS, null, values);
    }

    // Get all playlists and their IDs
    public List<String> getPlaylists() {
        List<String> playlists = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PLAYLISTS, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String playlistName = cursor.getString(cursor.getColumnIndex(COLUMN_PLAYLIST_NAME));
                playlists.add(playlistName);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return playlists;
    }

    // Get all playlist IDs
    public List<Integer> getPlaylistIds() {
        List<Integer> playlistIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PLAYLISTS, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                playlistIds.add(cursor.getInt(cursor.getColumnIndex(COLUMN_PLAYLIST_ID)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return playlistIds;
    }

    // Get all songs in a specific playlist
    public List<String> getSongsInPlaylist(int playlistId) {
        List<String> songs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_PLAYLIST_SONG_PATH + " FROM " + TABLE_PLAYLIST_SONGS +
                " WHERE " + COLUMN_PLAYLIST_SONG_PLAYLIST_ID + " = ?", new String[]{String.valueOf(playlistId)});

        if (cursor != null && cursor.moveToFirst()) {
            int songPathIndex = cursor.getColumnIndex(COLUMN_PLAYLIST_SONG_PATH);
            if (songPathIndex >= 0) {
                do {
                    songs.add(cursor.getString(songPathIndex));
                } while (cursor.moveToNext());
            } else {
                Log.e("DBHelper", "Column " + COLUMN_PLAYLIST_SONG_PATH + " not found in PlaylistSongs table.");
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return songs;
    }

    // Get all songs in the database
    public List<String> getAllSongs() {
        List<String> songs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_PLAYLIST_SONG_PATH + " FROM " + TABLE_PLAYLIST_SONGS, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(cursor.getString(cursor.getColumnIndex(COLUMN_PLAYLIST_SONG_PATH)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return songs;
    }
}
