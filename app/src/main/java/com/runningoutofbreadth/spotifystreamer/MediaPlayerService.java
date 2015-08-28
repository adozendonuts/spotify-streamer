package com.runningoutofbreadth.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import kaaes.spotify.webapi.android.models.Tracks;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener {
    private final String LOG_TAG = MediaPlayerService.class.getSimpleName();
    MediaPlayer mPlayer = new MediaPlayer();
    private Tracks mTrackList;
    private int mPosition;
    private String mTrackPreviewUrl;
    private final IBinder mBinder = new MusicBinder();
    Handler handler = new Handler(Looper.getMainLooper());

    // this runs when bindService is called in the fragment
    // gets all of the extras
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(LOG_TAG, "The onStartCommand has been executed");
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
                mPlayer.release();
            }
        }
        if (intent.getExtras() != null) {
            mPosition = intent.getExtras().getInt(TrackPlayerFragment.POSITION_KEY);
            mTrackList = intent.getExtras().getParcelable(TrackPlayerFragment.TRACK_LIST_KEY);
            if (mTrackList != null) {
                mTrackPreviewUrl = mTrackList.tracks.get(mPosition).preview_url;
            }
            mPlayer = new MediaPlayer();
            mPlayer.setOnPreparedListener(this);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mPlayer.setDataSource(mTrackPreviewUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // change pause button back to play on song completion
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    TrackPlayerFragment.mPlayButton.setImageResource(android.R.drawable.ic_media_play);
                }
            });
            mPlayer.prepareAsync();
        }
        return Service.START_NOT_STICKY;
    }

    public class MusicBinder extends Binder {
        MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.v(LOG_TAG, "THE MEDIAPLAYER IS PREPARED!");
        mediaPlayer.start();
        // change play button back to pause on song playback start
        TrackPlayerFragment.mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
        handler.postDelayed(TrackPlayerFragment.updateSeekBar, 100);
        Log.v(LOG_TAG, "THE MEDIAPLAYER HAS STARTED!");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.v(LOG_TAG, "THE SERVICE HAS BEEN DESTROYED!");
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        mPlayer.stop();
        mPlayer.release();
    }

    //methods for client
    public void play() {
        mPlayer.start();
    }

    public void pause() {
        mPlayer.pause();
    }

    public void stop() {
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
    }

    public void next() {
        if (Utility.hasInternet(getApplicationContext())) {
            if (mPosition < mTrackList.tracks.size() - 1) {
                mPosition += 1;
                Utility.loadTrack(mTrackList, mPosition, mPlayer);
            }
        } else {
            Utility.noInternetToast(getApplicationContext());
        }
    }

    public void previous() {
        if (Utility.hasInternet(getApplicationContext())) {
            if (mPosition != 0) {
                mPosition -= 1;
                Utility.loadTrack(mTrackList, mPosition, mPlayer);
            }
        } else {
            Utility.noInternetToast(getApplicationContext());
        }
    }
}
