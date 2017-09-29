package com.example.Mediaplayer;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.*;

import java.io.*;
import java.util.*;

/**
 * Created by ByelozyorovZ on 28.07.2016.
 */
public class PlayListActivity extends Activity implements
        MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {
    private Button toNavigatorButton;
    private String musicAreaPath = "/sdcard";
    private MusicDatabase db = new MusicDatabase();
    private String chosenPath = "/sdcard";

    private ImageButton btnPlay;
    private ImageButton btnStop;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private SeekBar seekProgressBar;
    private TextView songTitleLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    private MediaPlayer mp;
    private Handler mHandler = new Handler(); // Handler to update UI timer, progress bar etc,.
    private Utilities utils;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private ArrayList<AudioFileEntry> songsList = new ArrayList<AudioFileEntry>();
    AudioFileEntryAdapter audioFileEntryAdapter;

    public static int currentSongIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_list_layout);
        toNavigatorButton = (Button) findViewById(R.id.to_navigator_button);
        currentSongIndex = -1;

        try {
            FileInputStream music_file_stream = openFileInput("music.db");
            ObjectInputStream music_object_stream = new ObjectInputStream(
                    music_file_stream);
            db = (MusicDatabase) music_object_stream.readObject();
            music_object_stream.close();
        } catch (Exception e) {
            updateMusicDatabase();
        }

        Intent intent = getIntent();
        String chosenPathExtra = intent.getStringExtra("chosenPath");
        if(chosenPathExtra != null){
            chosenPath = chosenPathExtra;
        }

        String query = null;
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY).toLowerCase();
        }
        toUpdateList(query);

// All player buttons -------------------------------------------------------------------------
        btnPlay = (ImageButton) findViewById(R.id.imageButtonPlay);
        btnStop = (ImageButton) findViewById(R.id.imageButtonStop);
        btnNext = (ImageButton) findViewById(R.id.imageButtonNext);
        btnPrevious = (ImageButton) findViewById(R.id.imageButtonPrevious);
        btnRepeat = (ImageButton) findViewById(R.id.imageButtonRepeat);
        btnShuffle = (ImageButton) findViewById(R.id.imageButtonShuffle);
        seekProgressBar = (SeekBar) findViewById(R.id.seekBar);
        songTitleLabel = (TextView) findViewById(R.id.song_title);
        songCurrentDurationLabel = (TextView) findViewById(R.id.time_played);
        songTotalDurationLabel = (TextView) findViewById(R.id.total_running_time);

        // Mediaplayer
        mp = new MediaPlayer();
        utils = new Utilities();
        // Listeners
        seekProgressBar.setOnSeekBarChangeListener(this);
        mp.setOnCompletionListener(this);

        /**
         * Play button click event
         * plays a song and changes button to pause image
         * pauses a song and changes button to play image
         * */
        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check for already playing
                if (mp.isPlaying()) {
                    if (mp != null) {
                        mp.pause();
                        // Changing button image to play button
                        btnPlay.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                        // Stop updating progress bar.
                        stopUpdatingProgressBar();
                    }
                } else {
                    // Resume song
                    if (mp != null) {
                        mp.start();
                        // Changing button image to pause button
                        btnPlay.setImageResource(R.drawable.ic_pause_black_48dp);
                        // Start updating progress bar.
                        startUpdatingProgressBar();
                    }
                }
            }
        });

        /**
         * Stop button click event
         * */
        btnStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mp.stop();
                btnPlay.setImageResource(R.drawable.ic_play_arrow_black_48dp);
                // Stop updating progress bar.
                songTotalDurationLabel.setText("");
                // Displaying time completed playing
                songCurrentDurationLabel.setText("");
                seekProgressBar.setProgress(0);
                seekProgressBar.setMax(100);
                stopUpdatingProgressBar();
            }
        });


        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         * */
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                onCompletion(mp);
            }
        });

        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
        btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (currentSongIndex > 0) {
                    playSong(currentSongIndex - 1);
                } else {
                    // play last song
                    playSong(songsList.size() - 1);
                }
            }
        });

        /**
         * Button Click event for Repeat button
         * Enables repeat flag to true
         * */
        btnRepeat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isRepeat) {
                    isRepeat = false;
                    Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource(R.drawable.ic_repeat_black_48dp);
                } else {
                    // make repeat to true
                    isRepeat = true;
                    Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isShuffle = false;
                    btnRepeat.setImageResource(R.drawable.ic_repeat_one_black_48dp);
                    btnShuffle.setImageResource(R.drawable.ic_local_dining_black_48dp);
                }
            }
        });

        /**
         * Button Click event for Shuffle button
         * Enables shuffle flag to true
         * */
        btnShuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isShuffle) {
                    isShuffle = false;
                    Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                    btnShuffle.setImageResource(R.drawable.ic_local_dining_black_48dp);
                } else {
                    // make repeat to true
                    isShuffle = true;
                    Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isRepeat = false;
                    btnShuffle.setImageResource(R.drawable.ic_shuffle_black_48dp);
                    btnRepeat.setImageResource(R.drawable.ic_repeat_black_48dp);
                }
            }
        });

        try {
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                Uri fileUri = intent.getData();
                File intentFile = new File(fileUri.getPath());
                for (AudioFileEntry entry : songsList) {
                    File entryFile = new File(entry.getFilePath());
                    if (intentFile.getCanonicalPath().equals(entryFile.getCanonicalPath())) {
                        playSong(songsList.indexOf(entry));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Receiving song index from playlist view
     * and play the song
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 100) {
            playSong(data.getExtras().getInt("songIndex"));
        }
    }

    /**
     * Function to play a song
     *
     * @param nextSongIndex - index of song
     */
    public void playSong(int nextSongIndex) {
        currentSongIndex = nextSongIndex;
        audioFileEntryAdapter.notifyDataSetChanged();

        // Play song
        try {
            mp.reset();
            mp.setDataSource(songsList.get(currentSongIndex).getFilePath());
            mp.prepare();
            mp.start();
            // Displaying Song title
            String songTitle = songsList.get(currentSongIndex).getTitle();

            String songArtist = songsList.get(currentSongIndex).getArtist();
            String songAlbum = songsList.get(currentSongIndex).getAlbum();
            if (songArtist != null && !songArtist.equals("")) {
                songTitle += ", " + songArtist;
            }
            if (songAlbum != null && !songAlbum.equals("")) {
                songTitle += ", " + songAlbum;
            }
            songTitleLabel.setText(songTitle);
            // Changing Button Image to pause image
            btnPlay.setImageResource(R.drawable.ic_pause_black_48dp);
            // set Progress bar values
            seekProgressBar.setProgress(0);
            seekProgressBar.setMax(100);
            // Updating progress bar
            startUpdatingProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopUpdatingProgressBar() {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * Update timer on seekbar
     */
    public void startUpdatingProgressBar() {
        stopUpdatingProgressBar();
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();
            // Displaying Total Duration time
            songTotalDurationLabel.setText("" + utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            songCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(currentDuration));
            // Updating progress bar
            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            seekProgressBar.setProgress(progress);
            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        stopUpdatingProgressBar();
    }

    /**
     * When user stops moving the progress handler
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int totalDuration = mp.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
        // forward or backward to certain seconds
        mp.seekTo(currentPosition);
        // update timer progress again
        startUpdatingProgressBar();
    }

    /**
     * On Song Playing completed
     * if repeat is ON play same song again
     * if shuffle is ON play random song
     */
    @Override
    public void onCompletion(MediaPlayer arg0) {
        // check for repeat is ON or OFF
        if (isRepeat) {
            // repeat is on play same song again
            if (currentSongIndex == -1) {
                currentSongIndex = 0;
            }
            playSong(currentSongIndex);
        } else if (isShuffle) {
            // shuffle is on - play a random song
            Random rand = new Random();
            playSong(rand.nextInt(songsList.size()));
        } else {
            // no repeat or shuffle ON - play next song
            if (currentSongIndex < (songsList.size() - 1)) {
                playSong(currentSongIndex + 1);
            } else {
                // play first song
                playSong(0);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Wait for the possible last run of the task upgrading progress bar to finish
        try {
            stopUpdatingProgressBar();
            Thread.sleep(90);
            stopUpdatingProgressBar();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mp.release();
    } //------------------------------------------------------------

    private void findMusicFiles(String path) {
        File dir = new File(path);
        String[] list = dir.list();
        List<String> musicFiles = new ArrayList<String>();
        if (list != null) {
            for (String fileName : list) {
                String filePath = path;
                if (path.endsWith(File.separator)) {
                    filePath += fileName; // "/foo/bar/" + "baz.jpeg"
                } else {
                    filePath += File.separator + fileName; // "foo/bar" + "/" + "baz.jpeg"
                }
                File file = new File(filePath);
                if (file.isDirectory()) {
                    findMusicFiles(filePath);
                } else if (isMusicFile(file)) {
                    musicFiles.add(filePath);
                }
            }
        }

        if (musicFiles.size() > 0) {
            List<AudioFileEntry> temp = new ArrayList<AudioFileEntry>();
            for (String filePath : musicFiles) {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(filePath);
                String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                int runningTime = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
                AudioFileEntry audioFileEntry = new AudioFileEntry(title, artist, album, runningTime, filePath);
                temp.add(audioFileEntry);
            }
            db.dirs.put(path, temp);
        }
    }

    private boolean isMusicFile(File file) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        int index = file.getName().lastIndexOf('.')+1;
        String ext = file.getName().substring(index).toLowerCase();
        String type = mime.getMimeTypeFromExtension(ext);
        if (type != null && type.equals("audio/mpeg")){
            return true;
        }
        else return false;
    }

    private void updateMusicDatabase() {
        db = new MusicDatabase();
        findMusicFiles(musicAreaPath);
        try {
            FileOutputStream music_file_stream = openFileOutput(
                    "music.db", Context.MODE_PRIVATE);
            ObjectOutputStream music_object_stream = new ObjectOutputStream(
                    music_file_stream);
            music_object_stream.writeObject(db);
            music_object_stream.close();
        } catch (Exception e) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Не удалось обновить базу данных!", Toast.LENGTH_SHORT);
        }
//        adb push "F:\скачка\alexandra_stan.mp3" "/sdcard/Stan/alexandra.mp3"
//        adb push "F:\скачка\Hi-Fi\114_Hi-Fi - Besprizornik.mp3" "/sdcard/Hi-Fi/Besprizornik.mp3"
        toUpdateList(null);
    }

    private void toUpdateList(String query) {
        // Setup the list view
        final ListView audioFileEntryListView = (ListView) findViewById(R.id.playListView);
        audioFileEntryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playSong(position);
            }
        });
        audioFileEntryAdapter = new AudioFileEntryAdapter(this, R.layout.audio_file_entry_list_item);
        audioFileEntryListView.setAdapter(audioFileEntryAdapter);
        songsList = new ArrayList<AudioFileEntry>();
        for (Map.Entry entry : db.dirs.entrySet()) {
            if (chosenPath.equals("/sdcard") || entry.getKey().equals(chosenPath)) {
                for (AudioFileEntry element : (List<AudioFileEntry>) entry.getValue()) {
                    if (query == null
                            || (element.getTitle() != null && element.getTitle().toLowerCase().contains(query))
                            || (element.getArtist() != null && element.getArtist().toLowerCase().contains(query))
                            || (element.getAlbum() != null && element.getAlbum().toLowerCase().contains(query))) {
                        audioFileEntryAdapter.add(element);
                        songsList.add(element);
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get the ID of the selected menu item
        int id = item.getItemId();
        // Operations for the selected menu item
        switch (id) {
            case R.id.action_search:
                onSearchRequested();
                return true;
            case R.id.action_refresh:
                updateMusicDatabase();
                return true;
            case R.id.sort_by_title:
                audioFileEntryAdapter.sort(new Comparator<AudioFileEntry>() {
                    @Override
                    public int compare(AudioFileEntry lhs, AudioFileEntry rhs) {
                        if (lhs.getTitle() == null && rhs.getTitle() == null) {
                            return 0;
                        } else if (rhs.getTitle() == null) {
                            return -1;
                        } else if (lhs.getTitle() == null) {
                            return 1;
                        } else {
                            return lhs.getTitle().compareTo(rhs.getTitle());
                        }
                    }
                });
                return true;
            case R.id.sort_by_album:
                audioFileEntryAdapter.sort(new Comparator<AudioFileEntry>() {
                    @Override
                    public int compare(AudioFileEntry lhs, AudioFileEntry rhs) {
                        if (lhs.getAlbum() == null && rhs.getAlbum() == null) {
                            return 0;
                        } else if (rhs.getAlbum() == null) {
                            return -1;
                        } else if (lhs.getAlbum() == null) {
                            return 1;
                        } else {
                            return lhs.getAlbum().compareTo(rhs.getAlbum());
                        }
                    }
                });
                return true;
            case R.id.sort_by_artist:
                audioFileEntryAdapter.sort(new Comparator<AudioFileEntry>() {
                    @Override
                    public int compare(AudioFileEntry lhs, AudioFileEntry rhs) {
                        if (lhs.getArtist() == null && rhs.getArtist() == null) {
                            return 0;
                        } else if (rhs.getArtist() == null) {
                            return -1;
                        } else if (lhs.getArtist() == null) {
                            return 1;
                        } else {
                            return lhs.getArtist().compareTo(rhs.getArtist());
                        }
                    }
                });
                return true;
            case R.id.sort_by_running_time:
                audioFileEntryAdapter.sort(new Comparator<AudioFileEntry>() {
                    @Override
                    public int compare(AudioFileEntry lhs, AudioFileEntry rhs) {
                        return ((Integer) lhs.getRunningTime()).compareTo((Integer) rhs.getRunningTime());
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void buttonClick(View view) {
        Intent intent = new Intent(this, NavigatorActivity.class);
        startActivity(intent);
    }
}
