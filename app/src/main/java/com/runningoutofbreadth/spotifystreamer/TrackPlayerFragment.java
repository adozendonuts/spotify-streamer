package com.runningoutofbreadth.spotifystreamer;

import android.app.Dialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
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
    //    static final String TRACK_NAME_KEY = "TRACK";
    static final String ARTIST_NAME_KEY = "ARTIST";
//    static final String PREVIEW_URL_KEY = "URL";
//    static final String ALBUM_KEY = "ALBUM";
//    static final String ALBUM_COVER_KEY = "COVER";
//    static final String PANES_KEY = "PANES";
    private String mTrackPreviewUrl;
    private String mTrackArtist;
    private String mTrackAlbum;
    private String mTrackAlbumCover;
    private String mTrackName;
    //    boolean mTwoPane;
    private Tracks mTrackList;
    private int mPosition;
    MediaPlayer mediaPlayer = new MediaPlayer();

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
//            mTwoPane = args.getBoolean(PANES_KEY, mTwoPane);

        }

        final View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        TextView artistTextView = (TextView) rootView.findViewById(R.id.player_artist_name);
        artistTextView.setText(mTrackArtist);

        TextView albumTextView = (TextView) rootView.findViewById(R.id.player_album_name);
        albumTextView.setText(mTrackAlbum);

        ImageView albumImageView = (ImageView) rootView.findViewById(R.id.player_album_cover);
        Picasso.with(getActivity().getApplicationContext()).load(mTrackAlbumCover).into(albumImageView);

        TextView trackTextView = (TextView) rootView.findViewById(R.id.player_track_name);
        trackTextView.setText(mTrackName);

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
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    Log.v("PLAY BUTTON PRESSED", "hopefully it is pausing" + mTrackPreviewUrl);
                } else {
                    mediaPlayer.start();
                    Log.v("PLAY BUTTON PRESSED", "hopefully it is playing" + mTrackPreviewUrl);
                }
            }
        });
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                if(mPosition!= 0) {
                    mPosition -= 1;
//                    mTrackName = mTrackList.tracks.get(mPosition).name;
//                    mTrackPreviewUrl = mTrackList.tracks.get(mPosition).preview_url;
//                    mTrackAlbum = mTrackList.tracks.get(mPosition).album.name;
//                    mTrackAlbumCover = mTrackList.tracks.get(mPosition).album.images.get(0).url;
//                    artistTextView.setText(mTrackArtist);
//                    albumTextView.setText(mTrackAlbum);
//                    Picasso.with(getActivity().getApplicationContext()).load(mTrackAlbumCover).into(albumImageView);
//                    trackTextView.setText(mTrackName);
                }
            }
        });

        return rootView;
    }

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
