package com.runningoutofbreadth.spotifystreamer;

import android.app.Dialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
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
    private String mTrackPreviewUrl;
    private String mTrackArtist;
    private String mTrackAlbum;
    private String mTrackAlbumCover;
    private String mTrackName;
    private Tracks mTrackList;
    private int mPosition;
    MediaPlayer mediaPlayer = new MediaPlayer();
    private SeekBar mSeekBar;
    Handler mHandler = new Handler();

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
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(mTrackPreviewUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();


        Button playButton = (Button) rootView.findViewById(R.id.player_play_pause_button);
        Button prevButton = (Button) rootView.findViewById(R.id.player_track_previous_button);
        Button nextButton = (Button) rootView.findViewById(R.id.player_track_next_button);

        //TODO: refactor so this code doesn't repeat 3 times
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                    mHandler.postDelayed(updateSeekBar, 100);
                }
            }
        });
        prevButton.setOnClickListener(new View.OnClickListener() {
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
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.reset();
                if (mPosition != mTrackList.tracks.size()) {
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
        });

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
        @Override
        public void run() {
            int duration = mediaPlayer.getDuration();
            int currentPosition = mediaPlayer.getCurrentPosition();
            mSeekBar.setMax(duration);
            mSeekBar.setProgress(currentPosition);
            mHandler.postDelayed(this, 100);

            if (!mediaPlayer.isPlaying()) {
                mHandler.removeCallbacks(updateSeekBar);
            }
        }
    };

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

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
    }
}
