package com.example.musicplayer;

import java.io.Serializable;

public class AudioModel implements Serializable {
    String path;
    String title;
    String duration;


    // Default constructor (no-argument constructor)
    public AudioModel() {
        // Optionally, set default values for title, path, and artist if needed
        this.title = "";
        this.path = "";
        this.duration = "";
    }

    public AudioModel(String path, String title, String duration) {
        this.path = path;
        this.title = title;
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
