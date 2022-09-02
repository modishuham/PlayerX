package com.exoplayerexample.playerx

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util


class MainActivity : AppCompatActivity(), IPlayerSettingChange {

    private val mp4Url =
        "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"
    private val dashUrl: String =
        "https://bitmovin-a.akamaihd.net/content/sintel/sintel.mpd"
    private val hlsUrl: String =
        "https://assets.afcdn.com/video49/20210722/v_645516.m3u8"
    private val subTitleExample = "https://st2.epicon.in/srt_files/839022827.vtt"

    private lateinit var playerView: PlayerView
    private var exoPlayer: ExoPlayer? = null
    private var playWhenReady: Boolean = true
    private var currentWindow: Int = 0
    private var playbackPosition: Long = 0
    private var trackSelector: DefaultTrackSelector? = null
    private val playbackStateListener: Player.Listener = playbackStateListener()
    private var languagesList = ArrayList<String>()
    private var subtitleList = ArrayList<String>()
    private var isDoubleClicked = false
    private val doubleClickHandler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {
        isDoubleClicked = false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        playerView = findViewById(R.id.player)
        findViewById<ImageView>(R.id.iv_quality).setOnClickListener {
            if (!PlayerUtils.getAvailableVideoQualities().isNullOrEmpty()) {
                DialogUtils.showVideoQualityDialog(this, PlayerUtils.getAvailableVideoQualities()!!)
            }
        }
        findViewById<ImageView>(R.id.iv_audio).setOnClickListener {
            DialogUtils.showLanguageDialog(languagesList, this)
        }
        findViewById<ImageView>(R.id.iv_speed).setOnClickListener {
            DialogUtils.showPlaybackSpeedDialog(this, this)
        }
        findViewById<ImageView>(R.id.iv_subtitle).setOnClickListener {
            DialogUtils.showSubtitleDialog(subtitleList, this)
        }

        findViewById<ImageView>(R.id.exo_fullscreen).setOnClickListener {
            if (checkLandscapeOrientation()) {
                changeOrientationToLandscape(false)
            } else {
                changeOrientationToLandscape(true)
            }
        }

        findViewById<ConstraintLayout>(R.id.exo_double_tap_increment).setOnClickListener {
            if (isDoubleClicked) {
                isDoubleClicked = false
                doubleClickHandler.removeCallbacks(runnable)
                exoPlayer?.let {
                    it.seekTo(it.currentPosition + 20000)
                    Log.e("Sound", "" + exoPlayer?.volume)
                }
            } else {
                isDoubleClicked = true
                doubleClickHandler.postDelayed(runnable, 500)
            }
        }

        findViewById<ConstraintLayout>(R.id.exo_double_tap_decrement).setOnClickListener {
            if (isDoubleClicked) {
                isDoubleClicked = false
                doubleClickHandler.removeCallbacks(runnable)
                exoPlayer?.let {
                    if (it.currentPosition > 20000) {
                        it.seekTo(it.currentPosition - 20000)
                    } else {
                        it.seekTo(0)
                    }
                }
            } else {
                isDoubleClicked = true
                doubleClickHandler.postDelayed(runnable, 500)
            }
        }


    }

    private fun initializePlayer() {
        if (exoPlayer == null) {
            trackSelector = DefaultTrackSelector(this).apply {
                setParameters(buildUponParameters().setMaxVideoSizeSd())
            }
            val mediaItem =
                PlayerUtils.getMediaItemFromUrl(dashUrl, subTitleExample, MimeTypes.TEXT_VTT)
                    ?: return
            exoPlayer = ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector!!)
                .setBandwidthMeter(DefaultBandwidthMeter.Builder(this).build())
                .build()
            playerView.player = exoPlayer
            exoPlayer?.playWhenReady = playWhenReady
            exoPlayer?.setMediaItem(mediaItem)
            exoPlayer?.addListener(playbackStateListener)
        }
        exoPlayer?.seekTo(currentWindow, playbackPosition)
        exoPlayer?.prepare()
    }


    private fun releasePlayer() {
        exoPlayer?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentMediaItemIndex
            playWhenReady = this.playWhenReady
            removeListener(playbackStateListener)
            release()
        }
        exoPlayer = null
    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    public override fun onResume() {
        super.onResume()
        if ((Util.SDK_INT < 24 || exoPlayer == null)) {
            initializePlayer()
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private fun playbackStateListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                ExoPlayer.STATE_IDLE -> {
                }
                ExoPlayer.STATE_BUFFERING -> {
                }
                ExoPlayer.STATE_READY -> {
                }
                ExoPlayer.STATE_ENDED -> {
                }
                else -> {
                }
            }
        }

        override fun onTracksChanged(tracks: Tracks) {
            Log.e("ExoPlayer - ", "onTrackChangedCall")
            if (languagesList.isEmpty()) {
                //languagesList = PlayerUtils.getAvailableAudioFormat(trackGroups)
                languagesList = PlayerUtils.getAvailableAudio(exoPlayer!!.currentTracks)
                if (languagesList.isNotEmpty()) {
                    DialogUtils.createLanguageDialog(this@MainActivity, languagesList)
                    if (trackSelector != null) {
                        PlayerUtils.changePlayerAudio(
                            trackSelector,
                            languagesList[0]
                        )
                    }
                }
            }
            if (subtitleList.isEmpty()) {
                //subtitleList = PlayerUtils.getAvailableSubtitle(trackGroups)
                subtitleList = PlayerUtils.getAvailableSubtitles(exoPlayer!!.currentTracks)
                DialogUtils.createSubtitleDialog(this@MainActivity, subtitleList)
            }
            PlayerUtils.getAvailableVideoQualities(exoPlayer!!.currentTracks)
            if (PlayerUtils.getAvailableVideoQualities() == null) {
                PlayerUtils.initVideoQualities(exoPlayer, trackSelector)
                if (PlayerUtils.getAvailableVideoQualities() != null && PlayerUtils.getAvailableVideoQualities()!!
                        .isNotEmpty()
                ) {
                    DialogUtils.createVideoQualityDialog(
                        this@MainActivity,
                        PlayerUtils.getAvailableVideoQualities()!!
                    )
                }
            }
        }
    }

    override fun onLanguageChanged(languageCode: String?) {
        Toast.makeText(this, "" + languageCode, Toast.LENGTH_SHORT).show()
        if (trackSelector != null && languageCode != null) {
            PlayerUtils.changePlayerAudio(
                trackSelector,
                languageCode
            )
        }
    }

    override fun onSubtitleChanged(languageCode: String?) {
        Toast.makeText(this, "" + languageCode, Toast.LENGTH_SHORT).show()
        if (trackSelector != null && languageCode != null) {
            PlayerUtils.changeMediaSubtitle(
                trackSelector,
                languageCode
            )
        }
    }

    override fun onVideoQualityChanged(videoTrackInfo: VideoTrackInfo?) {
        videoTrackInfo?.let {
            PlayerUtils.changeVideoQuality(
                this,
                videoTrackInfo,
                trackSelector
            )
        }
    }

    override fun onPlaybackSpeedChanged(speed: String) {
        exoPlayer?.let {
            PlayerUtils.changePlaybackSpeed(speed.toFloat(), exoPlayer)
        }
    }


    /**
     * Changes the Orientation
     * @param shouldLandscape
     */
    private fun changeOrientationToLandscape(shouldLandscape: Boolean) {
        requestedOrientation = if (shouldLandscape) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    /**
     * Checks the Orientation
     * And returns true if Landscape else false
     */
    private fun checkLandscapeOrientation(): Boolean {
        val orientation = resources.configuration.orientation
        return orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            findViewById<ImageView>(R.id.exo_fullscreen).setImageResource(
                R.drawable.ic_fullscreen_exit
            )
        } else {
            findViewById<ImageView>(R.id.exo_fullscreen).setImageResource(
                R.drawable.ic_fullscreen
            )
        }
    }
}