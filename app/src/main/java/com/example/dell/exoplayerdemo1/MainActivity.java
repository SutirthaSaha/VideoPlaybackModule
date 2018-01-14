package com.example.dell.exoplayerdemo1;

import android.app.Dialog;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.Toolbar;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

public class MainActivity extends AppCompatActivity implements TextFragment.OnFragmentInteractionListener,PlaylistFragment.OnFragmentInteractionListener,MoreFragment.OnFragmentInteractionListener{

    String url="http://learnstack.in/assets/videos/web_internet.mp4";
    private final String STATE_RESUME_WINDOW = "resumeWindow";
    private final String STATE_RESUME_POSITION = "resumePosition";
    private final String STATE_PLAYER_FULLSCREEN = "playerFullscreen";

    private SimpleExoPlayerView exoPlayerView;
    private MediaSource mediaSource;
    private boolean exoPlayerFullscreen = false;
    private FrameLayout fullScreenButton;
    private ImageView fullScreenIcon;
    private Dialog fullScreenDialog;

    private int mResumeWindow;
    private long mResumePosition;

    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout=(TabLayout)findViewById(R.id.tablayout);
        viewPager=(ViewPager)findViewById(R.id.viewPager);
        viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.addFragments(new TextFragment(),"Video Text");
        viewPagerAdapter.addFragments(new PlaylistFragment(),"Playlist");
        viewPagerAdapter.addFragments(new MoreFragment(),"More");

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        if (savedInstanceState != null) {
            mResumeWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW);
            mResumePosition = savedInstanceState.getLong(STATE_RESUME_POSITION);
            exoPlayerFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putInt(STATE_RESUME_WINDOW, mResumeWindow);
        outState.putLong(STATE_RESUME_POSITION, mResumePosition);
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, exoPlayerFullscreen);

        super.onSaveInstanceState(outState);
    }


    private void initFullscreenDialog() {

        fullScreenDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            public void onBackPressed() {
                if (exoPlayerFullscreen)
                    closeFullscreenDialog();
                super.onBackPressed();
            }
        };
    }


    private void openFullscreenDialog() {

        ((ViewGroup) exoPlayerView.getParent()).removeView(exoPlayerView);
        fullScreenDialog.addContentView(exoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        fullScreenIcon.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_fullscreen_skrink));
        exoPlayerFullscreen = true;
        fullScreenDialog.show();
    }


    private void closeFullscreenDialog() {

        ((ViewGroup) exoPlayerView.getParent()).removeView(exoPlayerView);
        ((FrameLayout) findViewById(R.id.main_media_frame)).addView(exoPlayerView);
        exoPlayerFullscreen = false;
        fullScreenDialog.dismiss();
        fullScreenIcon.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_fullscreen_expand));
    }


    private void initFullscreenButton() {

        PlaybackControlView controlView = exoPlayerView.findViewById(R.id.exo_controller);
        fullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon);
        fullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button);
        fullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!exoPlayerFullscreen)
                    openFullscreenDialog();
                else
                    closeFullscreenDialog();
            }
        });
    }


    private void initExoPlayer() {

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), trackSelector, loadControl);
        exoPlayerView.setPlayer(player);

        boolean haveResumePosition = mResumeWindow != C.INDEX_UNSET;

        if (haveResumePosition) {
            exoPlayerView.getPlayer().seekTo(mResumeWindow, mResumePosition);
        }

        exoPlayerView.getPlayer().prepare(mediaSource);
        exoPlayerView.getPlayer().setPlayWhenReady(true);
    }


    @Override
    protected void onResume() {

        super.onResume();

        if (exoPlayerView == null) {

            exoPlayerView = (SimpleExoPlayerView) findViewById(R.id.exoplayer);
            initFullscreenDialog();
            initFullscreenButton();

            Uri videoUri=Uri.parse(url);

            DefaultHttpDataSourceFactory dataSourceFactory=new DefaultHttpDataSourceFactory("ExoPlayer Video");
            ExtractorsFactory extractorsFactory=new DefaultExtractorsFactory();
            mediaSource =new ExtractorMediaSource(videoUri,dataSourceFactory,extractorsFactory,null,null);
        }

        initExoPlayer();

        if (exoPlayerFullscreen) {
            ((ViewGroup) exoPlayerView.getParent()).removeView(exoPlayerView);
            fullScreenDialog.addContentView(exoPlayerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            fullScreenIcon.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_fullscreen_skrink));
            fullScreenDialog.show();
        }
    }

    @Override

    protected void onPause() {

        super.onPause();

        if (exoPlayerView != null && exoPlayerView.getPlayer() != null) {
            mResumeWindow = exoPlayerView.getPlayer().getCurrentWindowIndex();
            mResumePosition = Math.max(0, exoPlayerView.getPlayer().getContentPosition());

            exoPlayerView.getPlayer().release();
        }

        if (fullScreenDialog != null)
            fullScreenDialog.dismiss();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {


    }
}
