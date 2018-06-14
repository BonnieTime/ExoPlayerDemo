package com.example.pumpkin.exoplayerdemo;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/**
 * Created by pumpkin on 2018/6/8.
 */

public class PlayActivity extends Activity {

    private static final String TAG = "PlayActivity";

    private SimpleExoPlayerView mExoPlayerView;
    private SimpleExoPlayer mSimpleExoPlayer;
    private Context mContext;
    //    Uri playerUri = Uri.parse("https://storage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%20Hangin'%20with%20the%20Google%20Search%20Bar.mp4");
    Uri playerUri = Uri.parse("https://sdvideos.s3.cn-north-1.amazonaws.com.cn/allnew2/%E6%9D%8E%E5%A2%9E%E7%83%88%2B%E6%85%A2%E6%80%A7%E8%85%B9%E6%B3%BB%E4%B8%8E%E2%80%9C%E7%BB%93%E8%82%A0%E7%82%8E.mp4");

    private Uri subtitleUri;
    private ProgressBar mProgressBar;
    private String language;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.layout_play);

        Locale locale = getResources().getConfiguration().locale;
        // 获取当前系统语言
        language = locale.getLanguage();

        mContext = this;
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        initPlayer();

        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //设置为横屏
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    /**
     * 初始化player
     */
    private void initPlayer() {
        //1. 创建一个默认的 TrackSelector
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTackSelectionFactory);//从MediaSource 中选出 media 提供给可用的 Render S 来渲染，在创建播放器时被注入。
        LoadControl loadControl = new DefaultLoadControl();//控制 MediaSource 缓存更多的 media，有多少 media 被缓冲。在创建播放器时被注入。
        //2.创建ExoPlayer
        mSimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        //3.创建SimpleExoPlayerView
        mExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.exoView);
        //4.为SimpleExoPlayer设置播放器
        mExoPlayerView.setPlayer(mSimpleExoPlayer);
    }

    private void playVideo() {
        //测量播放过程中的带宽。 如果不需要，可以为null。
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        // 生成加载媒体数据的DataSource实例。
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(PlayActivity.this, Util.getUserAgent(PlayActivity.this, "useExoplayer"), bandwidthMeter);
        // 生成用于解析媒体数据的Extractor实例。
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();


        /*****************************************普通加载视频地址播放*********************************************************/
//        // MediaSource代表要播放的媒体。
//        MediaSource videoSource = new ExtractorMediaSource(playerUri, dataSourceFactory, extractorsFactory,
//                null, null);
//        //Prepare the player with the source.
//        mSimpleExoPlayer.prepare(videoSource);
//        //添加监听的listener
////        mSimpleExoPlayer.setVideoListener(mVideoListener);
//        mSimpleExoPlayer.addListener(eventListener);
////        mSimpleExoPlayer.setTextOutput(mOutput);
//        mSimpleExoPlayer.setPlayWhenReady(true);

        /*****************************************外挂字幕文件播放*********************************************************/
        // MediaSource代表要播放的媒体。
        MediaSource videoSource = new ExtractorMediaSource(playerUri, dataSourceFactory, extractorsFactory, null, null);
        Format subtitleFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_TTML, null, Format.NO_VALUE,
                Format.NO_VALUE, language, null);
        MediaSource subtitleSource = new SingleSampleMediaSource(subtitleUri, dataSourceFactory, subtitleFormat, C.TIME_UNSET);
        MergingMediaSource mergingMediaSource = new MergingMediaSource(videoSource, subtitleSource);
        //Prepare the player with the source.
        mSimpleExoPlayer.prepare(mergingMediaSource);
        //添加监听的listener
//        mSimpleExoPlayer.setVideoListener(mVideoListener);
        mSimpleExoPlayer.addListener(eventListener);
//        mSimpleExoPlayer.setTextOutput(mOutput);
        mSimpleExoPlayer.setPlayWhenReady(true);

    }


    TextRenderer.Output mOutput = new TextRenderer.Output() {
        @Override
        public void onCues(List<Cue> cues) {
            Log.i(TAG, "MainActivity.onCues.");
        }
    };

    private SimpleExoPlayer.VideoListener mVideoListener = new SimpleExoPlayer.VideoListener() {
        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
            Log.i(TAG, "MainActivity.onVideoSizeChanged.width:" + width + ", height:" + height);

        }

        @Override
        public void onRenderedFirstFrame() {
            Log.i(TAG, "MainActivity.onRenderedFirstFrame.");
        }
    };


    private ExoPlayer.EventListener eventListener = new ExoPlayer.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            Log.i(TAG, "onTimelineChanged");
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.i(TAG, "onTracksChanged");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Log.i(TAG, "onLoadingChanged");
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.i(TAG, "onPlayerStateChanged: playWhenReady = " + String.valueOf(playWhenReady) + " playbackState = " + playbackState);
            switch (playbackState) {
                case ExoPlayer.STATE_ENDED:
                    Log.i(TAG, "Playback ended!");
                    //Stop playback and return to start position
                    setPlayPause(false);
                    mSimpleExoPlayer.seekTo(0);
                    break;
                case ExoPlayer.STATE_READY:
                    mProgressBar.setVisibility(View.GONE);
                    Log.i(TAG, "ExoPlayer ready! pos: " + mSimpleExoPlayer.getCurrentPosition() + " max: " + stringForTime((int) mSimpleExoPlayer.getDuration()));
                    setProgress(0);
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    Log.i(TAG, "Playback buffering!");
                    mProgressBar.setVisibility(View.VISIBLE);
                    break;
                case ExoPlayer.STATE_IDLE:
                    Log.i(TAG, "ExoPlayer idle!");
                    break;
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.i(TAG, "onPlaybackError: " + error.getMessage());
        }

        @Override
        public void onPositionDiscontinuity() {
            Log.i(TAG, "onPositionDiscontinuity");
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            Log.i(TAG, "MainActivity.onPlaybackParametersChanged." + playbackParameters.toString());
        }
    };

    /**
     * Starts or stops playback. Also takes care of the Play/Pause button toggling
     *
     * @param play True if playback should be started
     */
    private void setPlayPause(boolean play) {
        mSimpleExoPlayer.setPlayWhenReady(play);
    }

    private String stringForTime(int timeMs) {
        StringBuilder mFormatBuilder;
        Formatter mFormatter;
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }


    @Override
    protected void onPause() {
        Log.i(TAG, "MainActivity.onPause.");
        super.onPause();
        mSimpleExoPlayer.stop();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "MainActivity.onStop.");
        super.onStop();
        mSimpleExoPlayer.release();
    }

    private void initData() {

        try {
            InputStream is = getAssets().open("ranshao.ass");
            String text = readTextFromSDcard(is);
            subtitleUri = Uri.parse(text);
            Log.e("FFFF","subtitleUri:"+subtitleUri);
            playVideo();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private String readTextFromSDcard(InputStream is) throws Exception {
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuffer buffer = new StringBuffer("");
        String str;
        while ((str = bufferedReader.readLine()) != null) {
            buffer.append(str);
            buffer.append("\n");
        }
        return buffer.toString();
    }
}
