package com.runningoutofbreadth.spotifystreamer;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * A placeholder fragment containing a simple view.
 */
public class TrackPlayerFragment extends Fragment {
    private String trackPreviewUrl;
    private String trackArtist;
    private String trackAlbum;
    private String trackAlbumCover;
    private String trackName;
    MediaPlayer mediaPlayer = new MediaPlayer();

    public TrackPlayerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        final View rootView = inflater.inflate(R.layout.fragment_track_player, container, false);

        if (intent != null && intent.hasExtra("URL")) {
            trackPreviewUrl = intent.getStringExtra("URL");
            trackArtist = intent.getStringExtra("Artist");
            trackAlbum = intent.getStringExtra("Album");
            trackName = intent.getStringExtra("Track");
            trackAlbumCover = intent.getStringExtra("Cover");
        }

        try {
            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                    trackPreviewUrl + "\n"
                            + trackArtist + "\n"
                            + trackAlbum + "\n"
                            + trackName + "\n"
                    , Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        } catch (NullPointerException e) {
            Log.v("YO DAWG", "This don't got no url");
        }

        TextView artistTextView = (TextView) rootView.findViewById(R.id.player_artist_name);
        TextView albumTextView = (TextView) rootView.findViewById(R.id.player_album_name);
        ImageView albumImageView = (ImageView) rootView.findViewById(R.id.player_album_cover);
        TextView trackTextView = (TextView) rootView.findViewById(R.id.player_track_name);



        artistTextView.setText(trackArtist);
        albumTextView.setText(trackAlbum);
        Picasso.with(getActivity().getApplicationContext()).load(trackAlbumCover).into(albumImageView);
        trackTextView.setText(trackName);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(trackPreviewUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();


        Button playButton = (Button) rootView.findViewById(R.id.player_play_pause_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    Log.v("PLAY BUTTON PRESSED", "hopefully it is pausing" + trackPreviewUrl);
                } else {
                    mediaPlayer.start();
                    Log.v("PLAY BUTTON PRESSED", "hopefully it is playing" + trackPreviewUrl);
                }
            }
        });

        return rootView;
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
