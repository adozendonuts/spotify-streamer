package com.runningoutofbreadth.spotifystreamer;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
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
    private String mTrackPreviewUrl;
    private String mTrackArtist;
    private String mTrackAlbum;
    private String mTrackAlbumCover;
    private String mTrackName;
    private Tracks mTrackList;
    private int mPosition;
    private int mCurrentPosition;

    public TrackPlayerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt(POSITION_KEY);
            mTrackPreviewUrl = savedInstanceState.getString(TRACK_URL_KEY);
            mTrackArtist = savedInstanceState.getString(ARTIST_NAME_KEY);
            mTrackAlbum = savedInstanceState.getString(ALBUM_NAME_KEY);
            mTrackAlbumCover = savedInstanceState.getString(ALBUM_COVER_KEY);
            mTrackName = savedInstanceState.getString(TRACK_NAME_KEY);
            mTrackList = savedInstanceState.getParcelable(TRACK_LIST_KEY);
            mCurrentPosition = savedInstanceState.getInt(CURRENT_POSITION_KEY);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(POSITION_KEY, mPosition);
        savedInstanceState.putParcelable(TRACK_LIST_KEY, mTrackList);
        savedInstanceState.putString(ARTIST_NAME_KEY, mTrackArtist);
        savedInstanceState.putString(TRACK_URL_KEY, mTrackPreviewUrl);
        savedInstanceState.putString(ALBUM_NAME_KEY, mTrackAlbum);
        savedInstanceState.putString(ALBUM_COVER_KEY, mTrackAlbumCover);
        savedInstanceState.putString(TRACK_NAME_KEY, mTrackName);
        savedInstanceState.putInt(CURRENT_POSITION_KEY, mCurrentPosition);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState == null) {
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

        return rootView;
    }

    //helper method for updating all views for dialogfragment and loading next track

//    public void updateViews(TextView artistTextView, TextView albumTextView,
//                            ImageView albumImageView, TextView trackTextView, int position) {
//        mTrackName = mTrackList.tracks.get(position).name;
//        mTrackAlbum = mTrackList.tracks.get(position).album.name;
//        mTrackAlbumCover = mTrackList.tracks.get(position).album.images.get(0).url;
//        mTrackPreviewUrl = mTrackList.tracks.get(mPosition).preview_url;
//        artistTextView.setText(mTrackArtist);
//        albumTextView.setText(mTrackAlbum);
//        picassoLoader(mTrackAlbumCover, albumImageView);
//        trackTextView.setText(mTrackName);}


        public void picassoLoader(String stringUrl, ImageView imageView) {
            Picasso.with(getActivity().getApplicationContext()).load(stringUrl)
                    .resize(300, 300)
                    .centerCrop()
                    .into(imageView);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            return dialog;
        }

    }
