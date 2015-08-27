package com.runningoutofbreadth.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements SearchFragment.TopTenTracksCallback {
    String TRACKSFRAGMENT_TAG = "TFTAG";
    boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragment_top_tracks) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_top_tracks, new TopTenTracksFragment(), TRACKSFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public void onItemSelected(String artistId, String artistName) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putString(TopTenTracksFragment.ARTIST_ID_KEY, artistId);
            args.putString(TopTenTracksFragment.ARTIST_NAME_KEY, artistName);
            args.putBoolean(TopTenTracksFragment.PANE_KEY, mTwoPane);

            TopTenTracksFragment topTenTracksFragment = new TopTenTracksFragment();
            topTenTracksFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_top_tracks, topTenTracksFragment, TRACKSFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, TopTenTracksActivity.class)
                    .putExtra(TopTenTracksFragment.ARTIST_ID_KEY, artistId);
            intent.putExtra(TopTenTracksFragment.ARTIST_NAME_KEY, artistName);
            startActivity(intent);
        }
    }
}
