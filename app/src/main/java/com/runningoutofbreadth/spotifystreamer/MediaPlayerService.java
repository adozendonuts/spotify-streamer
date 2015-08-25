package com.runningoutofbreadth.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import kaaes.spotify.webapi.android.models.Tracks;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener {
    private final String LOG_TAG = MediaPlayerService.class.getSimpleName();
    MediaPlayer mPlayer;
    private Tracks mTrackList;
    private int mPosition;

    @Override
    public void onCreate() {
        // TODO: Start up the thread running the service.
        Log.v(LOG_TAG, "THE SERVICE HAS BEEN CREATED!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(LOG_TAG, "THE SERVICE HAS STARTED!");
        //TODO do something useful
        if (intent.getExtras() != null) {
            mPosition = intent.getExtras().getInt(TrackPlayerFragment.POSITION_KEY);
            mTrackList = intent.getExtras().getParcelable(TrackPlayerFragment.TRACK_LIST_KEY);
            String mTrackPreviewUrl = mTrackList.tracks.get(mPosition).preview_url;
            mPlayer = new MediaPlayer();
            mPlayer.setOnPreparedListener(this);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mPlayer.setDataSource(mTrackPreviewUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mPlayer.prepareAsync();
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.v(LOG_TAG, "THE MEDIAPLAYER IS PREPARED!");
        mediaPlayer.start();
        Log.v(LOG_TAG, "THE MEDIAPLAYER HAS STARTED!");
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    @Override
    public void onDestroy() {
        Log.v(LOG_TAG, "THE SERVICE HAS BEEN DESTROYED!");
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        mPlayer.stop();
    }
}
