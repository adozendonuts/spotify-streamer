package com.runningoutofbreadth.spotifystreamer;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import kaaes.spotify.webapi.android.models.Tracks;

/**
 * A placeholder fragment containing a simple view.
 */
public class TrackPlayerFragment extends DialogFragment {
    static final String TRACK_LIST_KEY = "TRACKLIST";
    static final String POSITION_KEY = "POSITION";
    static final String ARTIST_NAME_KEY = "ARTIST";
    static final String TRACK_URL_KEY = "URL";
    static final String ALBUM_NAME_KEY = "ALBUM";
    static final String ALBUM_COVER_KEY = "COVER";
    static final String TRACK_NAME_KEY = "TRACK";
    static final String CURRENT_POSITION_KEY = "CURRENTTIME";
    static final String CURRENT_DURATION_KEY = "DURATION";
    static final String BOUND_KEY = "BOUND";
    private String mTrackPreviewUrl;
    private String mTrackArtist;
    private String mTrackAlbum;
    private String mTrackAlbumCover;
    private String mTrackName;
    private Tracks mTrackList;
    protected static ImageView mPlayButton;
    protected static SeekBar mSeekBar;
    protected static TextView mCurrentTimeTextView;
    protected static TextView mDurationTextView;
    protected static Handler mHandler = new Handler();
    private int mPosition;
    private static int mCurrentPosition;
    private static int mDuration;
    protected static MediaPlayerService mService;
    protected boolean mBound;
    Bundle mArgs;

    public TrackPlayerFragment() {
    }

    protected ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((MediaPlayerService.MusicBinder) service).getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (!mBound) {
            Intent intent = new Intent(getActivity(), MediaPlayerService.class);
            intent.putExtras(mArgs);
            try {
                getActivity().startService(intent);
                getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            } catch (Exception e) {
                //TODO figure out what is causing the ServiceConnectionLeaked error
                Log.v("SERVICECONNECTION LEAK", "WHY");
            }
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt(POSITION_KEY);
            mTrackPreviewUrl = savedInstanceState.getString(TRACK_URL_KEY);
            mTrackArtist = savedInstanceState.getString(ARTIST_NAME_KEY);
            mTrackAlbum = savedInstanceState.getString(ALBUM_NAME_KEY);
            mTrackAlbumCover = savedInstanceState.getString(ALBUM_COVER_KEY);
            mTrackName = savedInstanceState.getString(TRACK_NAME_KEY);
            mTrackList = savedInstanceState.getParcelable(TRACK_LIST_KEY);
            mCurrentPosition = savedInstanceState.getInt(CURRENT_POSITION_KEY);
            mDuration = savedInstanceState.getInt(CURRENT_DURATION_KEY, 30);
            mBound = savedInstanceState.getBoolean(BOUND_KEY);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(POSITION_KEY, mPosition);
        savedInstanceState.putParcelable(TRACK_LIST_KEY, mTrackList);
        savedInstanceState.putString(ARTIST_NAME_KEY, mTrackArtist);
        savedInstanceState.putString(TRACK_URL_KEY, mTrackPreviewUrl);
        savedInstanceState.putString(ALBUM_NAME_KEY, mTrackAlbum);
        savedInstanceState.putString(ALBUM_COVER_KEY, mTrackAlbumCover);
        savedInstanceState.putString(TRACK_NAME_KEY, mTrackName);
        savedInstanceState.putInt(CURRENT_POSITION_KEY, mCurrentPosition);
        savedInstanceState.putInt(CURRENT_DURATION_KEY, mDuration);
        savedInstanceState.putBoolean(BOUND_KEY, mBound);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            mArgs = getArguments();
            if (mArgs != null) {
                mTrackList = mArgs.getParcelable(TRACK_LIST_KEY);
                mPosition = mArgs.getInt(POSITION_KEY);
                mTrackArtist = mArgs.getString(ARTIST_NAME_KEY);
                mTrackName = mTrackList.tracks.get(mPosition).name;
                mTrackPreviewUrl = mTrackList.tracks.get(mPosition).preview_url;
                mTrackAlbum = mTrackList.tracks.get(mPosition).album.name;
                mTrackAlbumCover = mTrackList.tracks.get(mPosition).album.images.get(0).url;
            }
        }

        final View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        final TextView artistTextView = (TextView) rootView.findViewById(R.id.player_artist_name);
        artistTextView.setText(mTrackArtist);

        final TextView albumTextView = (TextView) rootView.findViewById(R.id.player_album_name);
        albumTextView.setText(mTrackAlbum);

        final ImageView albumImageView = (ImageView) rootView.findViewById(R.id.player_album_cover);
        picassoLoader(mTrackAlbumCover, albumImageView);

        final TextView trackTextView = (TextView) rootView.findViewById(R.id.player_track_name);
        trackTextView.setText(mTrackName);

        mSeekBar = (SeekBar) rootView.findViewById(R.id.player_seekbar);
        mSeekBar.setMax(mDuration);
        mSeekBar.setProgress(mCurrentPosition);
        mSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {


                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            if (mService.mPlayer != null) {
                                mService.mPlayer.seekTo(progress * 1000);
                            }
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        Log.v("SEEKBAR PROGRESS", Integer.toString(seekBar.getProgress()));
                        mHandler.removeCallbacks(updateSeekBar);
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mHandler.postDelayed(updateSeekBar, 100);
                    }
                }

        );

        mDurationTextView = (TextView) rootView.findViewById(R.id.player_track_time_end);

        mCurrentTimeTextView = (TextView) rootView.findViewById(R.id.player_track_time_start);

        mPlayButton = (ImageView) rootView.findViewById(R.id.player_play_pause_button);
        mPlayButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        if (mBound) {
                            Log.v("MSERVICE", "THIS IS THE VALUE OF mService" + mService);
                            if (mService.mPlayer.isPlaying()) {
                                mService.pause();
                                mPlayButton.setImageResource(android.R.drawable.ic_media_play);
                            } else {
                                mService.play();
                                mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
                                mHandler.postDelayed(updateSeekBar, 100);
                            }
                        }
                    }
                }
        );

        final ImageView prevButton = (ImageView) rootView.findViewById(R.id.player_track_previous_button);
        prevButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mService.previous();
                        if (mPosition != 0) {
                            mPosition -= 1;
                            updateViews(artistTextView, albumTextView, albumImageView,
                                    trackTextView, mCurrentTimeTextView, mDurationTextView, mPosition);
                        }
                    }
                }
        );

        final ImageView nextButton = (ImageView) rootView.findViewById(R.id.player_track_next_button);
        nextButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mService.next();
                        if (mPosition != mTrackList.tracks.size() - 1) {
                            mPosition += 1;
                            updateViews(artistTextView, albumTextView, albumImageView,
                                    trackTextView, mCurrentTimeTextView, mDurationTextView, mPosition);
                        }
                    }
                }
        );
        return rootView;
    }

    //helper method for updating all views for dialogfragment and loading next track
    public void updateViews(TextView artistTextView, TextView albumTextView,
                            ImageView albumImageView, TextView trackTextView,
                            TextView currentTimeTextView,
                            TextView durationTextView, int position) {
        String currentTimeString = String.format("%01d:%02d", 0, 0);
        String durationString = String.format("%01d:%02d", 0, 30);

        mTrackName = mTrackList.tracks.get(position).name;
        mTrackAlbum = mTrackList.tracks.get(position).album.name;
        mTrackAlbumCover = mTrackList.tracks.get(position).album.images.get(0).url;
        artistTextView.setText(mTrackArtist);
        albumTextView.setText(mTrackAlbum);
        currentTimeTextView.setText(currentTimeString);
        durationTextView.setText(durationString);
        picassoLoader(mTrackAlbumCover, albumImageView);
        trackTextView.setText(mTrackName);
    }

    // to help test resizing
    public void picassoLoader(String stringUrl, ImageView imageView) {
        Picasso.with(getActivity().getApplicationContext()).load(stringUrl)
                .resize(300, 300)
                .centerCrop()
                .into(imageView);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            TrackPlayerFragment.mService.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Runnable updateSeekBar = new Runnable() {
        int duration;
        int currentPosition;
        String currentTimeString;
        String durationString;

        @Override
        public void run() {
            if (mService.mPlayer != null) {
                currentPosition = mService.mPlayer.getCurrentPosition() / 1000;
                duration = mService.mPlayer.getDuration() / 1000;

                //update SeekBar
                mSeekBar.setMax(duration);
                mSeekBar.setProgress(currentPosition);

                //update views
                //TODO figure out why duration is sometimes incorrect
                currentTimeString = String.format("%01d:%02d", 0, currentPosition);
                mCurrentTimeTextView.setText(currentTimeString);
                durationString = String.format("%01d:%02d", 0, 30);
                mDurationTextView.setText(durationString);

                //run this at 1 second intervals, assign mCurrentPosition for saved state
                mCurrentPosition = currentPosition;
                mHandler.postDelayed(this, 100);
            } else {
                Log.v("MPLAYER THREAD", "mediaplayer isn't on. We can't get this party started...");
            }
        }
    };
}
