package com.example.Mediaplayer;

import java.io.Serializable;

public final class AudioFileEntry implements Serializable {

    private final String title;
    private final String artist;
    private final String album;
    private final int runningTime;
    private final String filePath;

    public AudioFileEntry(final String title, final String artist,
                          final String album, final int runningTime, String filePath) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.runningTime = runningTime;
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    /**
     * @return Title of file entry
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return Author of file entry
     */
    public String getArtist() {
        return artist;
    }

    /**
     * @return Post date of file entry
     */
    public String getAlbum() {
        return album;
    }

    /**
     * @return Icon of this file entry
     */
    public int getRunningTime() {
        return runningTime;
    }
}

