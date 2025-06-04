package com.example.media_player;

import android.media.MediaPlayer;

/**
 * Singleton class to hold a single shared instance of MediaPlayer.
 *
 * ✅ Why we use this:
 * - To make sure there's only ONE MediaPlayer running at a time across the whole app.
 * - Avoids multiple songs playing at once when switching activities.
 * - Helps us stop or release the currently playing song before playing a new one.
 * - Makes it easy to access the same MediaPlayer from any activity like MainActivity or playSong.
 *
 * Think of it like using one common music player (or speaker) for your whole app,
 * instead of starting a new one every time a new screen opens.
 */
public class MusicPlayerSingleton {
    // Static variable so it is shared and accessible from anywhere in the app
    public static MediaPlayer mediaPlayer;
}
