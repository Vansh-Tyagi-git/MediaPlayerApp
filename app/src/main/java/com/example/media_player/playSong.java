package com.example.media_player;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

public class playSong extends AppCompatActivity {
    private ImageButton play,next,previous;
    private TextView songName;
    ArrayList<File> songs;
    MediaPlayer mediaPlayer;
    String textContent;
    SeekBar seekbar;
    int position;
    Thread updateSeek;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (updateSeek != null && updateSeek.isAlive()) {
            updateSeek.interrupt();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_play_song);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        play = findViewById(R.id.play);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);
        songName = findViewById(R.id.songName);
        songName.setSelected(true);
        seekbar = findViewById(R.id.seekBar2);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        songs = (ArrayList) bundle.getParcelableArrayList("songList");
        songName.setText(bundle.getString("currentSong"));
        position = bundle.getInt("position",0);
        //Uri uri = Uri.parse(songs.get(position).toString());
        Uri uri = Uri.fromFile(songs.get(position));
        if (MusicPlayerSingleton.mediaPlayer != null) {
            MusicPlayerSingleton.mediaPlayer.stop();
            MusicPlayerSingleton.mediaPlayer.release();
        }
        MusicPlayerSingleton.mediaPlayer = MediaPlayer.create(this, uri);
        mediaPlayer = MusicPlayerSingleton.mediaPlayer; // ✅ Fix: now local mediaPlayer is initialized
        mediaPlayer.start();

//If a previous song is playing, it's stopped and cleaned up.
//
//Only one MediaPlayer is ever active at a time.
//
//You’re using the static factory method correctly.
        seekbar.setMax(mediaPlayer.getDuration());
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
        updateSeek = new Thread(){
            @Override
            public void run() {
                int currentPosition = 0;
                try {
                    while (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        currentPosition = mediaPlayer.getCurrentPosition();
                        int finalCurrentPosition = currentPosition;
                        runOnUiThread(() -> seekbar.setProgress(finalCurrentPosition));//In Android, only the main (UI) thread is allowed to update UI components like SeekBar.
                        sleep(800);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        updateSeek.start();

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer != null && !mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    play.setImageResource(R.drawable.pause);
                }else{
                    mediaPlayer.pause();
                    play.setImageResource(R.drawable.play);
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Stop and release current mediaPlayer
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }

                // Interrupt old thread
                if (updateSeek != null && updateSeek.isAlive()) {
                    updateSeek.interrupt();
                }

                // Move to next song
                position = (position + 1) % songs.size();
                Uri uri = Uri.fromFile(songs.get(position));

                // Start new mediaPlayer
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                MusicPlayerSingleton.mediaPlayer = mediaPlayer;
                songName.setText(songs.get(position).getName());
                mediaPlayer.start();

                // Update seekbar max
                seekbar.setMax(mediaPlayer.getDuration());

                // Start new seek thread
                updateSeek = new Thread() {
                    @Override
                    public void run() {
                        try {
                            while (mediaPlayer != null && mediaPlayer.isPlaying() && !isInterrupted()) {
                                int currentPosition = mediaPlayer.getCurrentPosition();
                                runOnUiThread(() -> seekbar.setProgress(currentPosition));
                                sleep(800);
                            }
                        } catch (InterruptedException e) {
                            // Graceful exit if interrupted
                        }
                    }
                };
                updateSeek.start();

                // Change play button to pause icon
                play.setImageResource(R.drawable.pause);
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Stop and release current mediaPlayer
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }

                // Interrupt old thread
                if (updateSeek != null && updateSeek.isAlive()) {
                    updateSeek.interrupt();
                }

                // Move to previous song
                if(position != 0){
                    position = position - 1;
                }else{
                    position = songs.size()-1;
                }
                Uri uri = Uri.fromFile(songs.get(position));

                // Start new mediaPlayer
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                MusicPlayerSingleton.mediaPlayer = mediaPlayer;
                songName.setText(songs.get(position).getName());
                mediaPlayer.start();

                // Update seekbar max
                seekbar.setMax(mediaPlayer.getDuration());

                // Start new seek thread
                updateSeek = new Thread() {
                    @Override
                    public void run() {
                        try {
                            while (mediaPlayer != null && mediaPlayer.isPlaying() && !isInterrupted()) {
                                int currentPosition = mediaPlayer.getCurrentPosition();
                                runOnUiThread(() -> seekbar.setProgress(currentPosition));
                                sleep(800);
                            }
                        } catch (InterruptedException e) {
                            // Graceful exit if interrupted
                        }
                    }
                };
                updateSeek.start();

                // Change play button to pause icon
                play.setImageResource(R.drawable.pause);
            }
        });

    }
}