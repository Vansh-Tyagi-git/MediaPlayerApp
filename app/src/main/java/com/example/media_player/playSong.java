package com.example.media_player;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.util.ArrayList;

// ... (imports remain unchanged)

public class playSong extends AppCompatActivity {
    private ImageButton play, next, previous;
    private TextView songName;
    private SeekBar seekbar;
    private ArrayList<File> songs;
    private MediaPlayer mediaPlayer;
    private Thread updateSeek;
    private int position;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAndCleanup();
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
        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);
        songName = findViewById(R.id.songName);
        seekbar = findViewById(R.id.seekBar2);
        songName.setSelected(true);

        // Get data from intent
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        ArrayList<String> songPaths = bundle.getStringArrayList("songList");
        position = bundle.getInt("position", 0);

        // Convert paths to File
        songs = new ArrayList<>();
        if (songPaths != null) {
            for (String path : songPaths) {
                songs.add(new File(path));
            }
        }

        if (songs.isEmpty()) {
            Toast.makeText(this, "No songs available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Start playing
        playSongAtPosition(position);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });

        play.setOnClickListener(view -> {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                play.setImageResource(R.drawable.pause);
            } else if (mediaPlayer != null) {
                mediaPlayer.pause();
                play.setImageResource(R.drawable.play);
            }
        });

        next.setOnClickListener(view -> {
            position = (position + 1) % songs.size();
            playSongAtPosition(position);
        });

        previous.setOnClickListener(view -> {
            position = (position - 1 + songs.size()) % songs.size(); // wraparound
            playSongAtPosition(position);
        });
    }

    private void playSongAtPosition(int pos) {
        try {
            stopAndCleanup();

            File songFile = songs.get(pos);
            Uri uri = Uri.fromFile(songFile);
            mediaPlayer = MediaPlayer.create(this, uri);
            MusicPlayerSingleton.mediaPlayer = mediaPlayer;

            songName.setText(songFile.getName());

            mediaPlayer.setOnPreparedListener(mp -> {
                seekbar.setMax(mp.getDuration());
                mp.start();
                play.setImageResource(R.drawable.pause);
                startSeekBarUpdateThread();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                position = (position + 1) % songs.size();
                playSongAtPosition(position);
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error playing song", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAndCleanup() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (updateSeek != null && updateSeek.isAlive()) {
            updateSeek.interrupt();
            updateSeek = null;
        }
    }

    private void startSeekBarUpdateThread() {
        updateSeek = new Thread(() -> {
            try {
                while (mediaPlayer != null && mediaPlayer.isPlaying() && !Thread.currentThread().isInterrupted()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    runOnUiThread(() -> seekbar.setProgress(currentPosition));
                    Thread.sleep(800);
                }
            } catch (InterruptedException ignored) {}
        });
        updateSeek.start();
    }
}
