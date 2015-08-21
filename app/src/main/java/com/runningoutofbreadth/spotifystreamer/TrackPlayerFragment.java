package com.runningoutofbreadth.spotifystreamer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import kaaes.spotify.webapi.android.models.Tracks;

/**
 * A placeholder fragment containing a simple view.
 */
public class TrackPlayerFragment extends DialogFragment {
    static final String TRACK_LIST_KEY = "TRACKLIST";
    static final String POSITION_KEY = "POSITION";
    static final String ARTIST_NAME_KEY = "ARTIST";
    //    static final String SIZE_KEY = "PANES";
    private String mTrackPreviewUrl;
    private String mTrackArtist;
    private String mTrackAlbum;
    private String mTrackAlbumCover;
    private String mTrackName;
    private Tracks mTrackList;
    //    private boolean mTwoPane;
    private int mPosition;
    MediaPlayer mediaPlayer = new MediaPlayer();
    Handler mHandler = new Handler();
    private SeekBar mSeekBar;
    private TextView mCurrentTime;
    private TextView mFullTime;

    public TrackPlayerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null) {
            mTrackList = args.getParcelable(TRACK_LIST_KEY);
            mPosition = args.getInt(POSITION_KEY);
            mTrackArtist = args.getString(ARTIST_NAME_KEY);
            mTrackName = mTrackList.tracks.get(mPosition).name;
            mTrackPreviewUrl = mTrackList.tracks.get(mPosition).preview_url;
            mTrackAlbum = mTrackList.tracks.get(mPosition).album.name;
            mTrackAlbumCover = mTrackList.tracks.get(mPosition).album.images.get(0).url;
        }

        final View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        final TextView artistTextView = (TextView) rootView.findViewById(R.id.player_artist_name);
        artistTextView.setText(mTrackArtist);

        final TextView albumTextView = (TextView) rootView.findViewById(R.id.player_album_name);
        albumTextView.setText(mTrackAlbum);

        final ImageView albumImageView = (ImageView) rootView.findViewById(R.id.player_album_cover);
        Picasso.with(getActivity().getApplicationContext()).load(mTrackAlbumCover).into(albumImageView);

        final TextView trackTextView = (TextView) rootView.findViewById(R.id.player_track_name);
        trackTextView.setText(mTrackName);

        mSeekBar = (SeekBar) rootView.findViewById(R.id.player_seekbar);

        mCurrentTime = (TextView) rootView.findViewById(R.id.player_track_time_start);

        mFullTime = (TextView) rootView.findViewById(R.id.player_track_time_end);


        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(mTrackPreviewUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.prepareAsync();

        String duration = String.format("%02d:%02d", 0, 30);
        mFullTime.setText(duration);

        final ImageView playButton = (ImageView) rootView.findViewById(R.id.player_play_pause_button);
        ImageView prevButton = (ImageView) rootView.findViewById(R.id.player_track_previous_button);
        ImageView nextButton = (ImageView) rootView.findViewById(R.id.player_track_next_button);

        //TODO: refactor so this code doesn't repeat 3 times
        playButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            playButton.setImageResource(android.R.drawable.ic_media_pause);
                        } else {
                            playButton.setImageResource(android.R.drawable.ic_media_play);
                            mediaPlayer.start();
                            mHandler.postDelayed(updateSeekBar, 100);
                        }
                    }
                }

        );
        prevButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mediaPlayer.reset();
                        if (mPosition != 0) {
                            mPosition -= 1;
                            updateViews(artistTextView, albumTextView, albumImageView, trackTextView, mPosition);
                            try {
                                mediaPlayer.setDataSource(mTrackPreviewUrl);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mediaPlayer.prepareAsync();
                        }
                        ;
                    }
                }

        );
        nextButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mediaPlayer.reset();
                        if (mPosition != mTrackList.tracks.size()-1) {
                            mPosition += 1;
                            updateViews(artistTextView, albumTextView, albumImageView, trackTextView, mPosition);
                            try {
                                mediaPlayer.setDataSource(mTrackPreviewUrl);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mediaPlayer.prepareAsync();
                        }
                    }
                }

        );

        mSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mediaPlayer.seekTo(progress * 1000);
                            Log.v("SEEK ON PROGRESS", "progress value: " + progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mHandler.removeCallbacks(updateSeekBar);
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mHandler.postDelayed(updateSeekBar, 100);
                    }
                }

        );

        return rootView;
    }

    //helper method for updating all views for dialogfragment and loading next track
    public void updateViews(TextView artistTextView, TextView albumTextView,
                            ImageView albumImageView, TextView trackTextView, int position) {
        mTrackName = mTrackList.tracks.get(position).name;
        mTrackAlbum = mTrackList.tracks.get(position).album.name;
        mTrackAlbumCover = mTrackList.tracks.get(position).album.images.get(0).url;
        mTrackPreviewUrl = mTrackList.tracks.get(mPosition).preview_url;
        artistTextView.setText(mTrackArtist);
        albumTextView.setText(mTrackAlbum);
        Picasso.with(getActivity().getApplicationContext()).load(mTrackAlbumCover).into(albumImageView);
        trackTextView.setText(mTrackName);
    }

    private final Runnable updateSeekBar = new Runnable() {
        int duration;
        int currentPosition;
        String currentTimeString;

        @Override
        public void run() {
            duration = mediaPlayer.getDuration() / 1000;
            currentPosition = mediaPlayer.getCurrentPosition() / 1000;
            currentTimeString = String.format("%02d:%02d", 0, currentPosition);
            mSeekBar.setMax(duration);
            mSeekBar.setProgress(currentPosition);
            mCurrentTime.setText(currentTimeString);
            mHandler.postDelayed(this, 100);

            if (!mediaPlayer.isPlaying()) {
                mHandler.removeCallbacks(updateSeekBar);
            }
        }
    };

//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        // The only reason you might override this method when using onCreateView() is
//        // to modify any dialog characteristics. For example, the dialog includes a
//        // title by default, but your custom layout might not need it. So here you can
//        // remove the dialog title, but you must call the superclass to get the Dialog.
//        Dialog dialog = super.onCreateDialog(savedInstanceState);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.fragment_player);
//        return dialog;
//    }

    @Override
    public void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mHandler.removeCallbacks(updateSeekBar);
    }
}
