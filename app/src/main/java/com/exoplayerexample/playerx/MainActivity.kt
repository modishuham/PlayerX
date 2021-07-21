package com.exoplayerexample.playerx

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), IPlayerSettingChange {

    private val mp4Url =
        "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"
    private val dashUrl: String =
        "https://bitmovin-a.akamaihd.net/content/sintel/sintel.mpd"
    private val hlsUrl: String =
        "https://epiconvod.s.llnwi.net/mmd-video/lmpl_test/ek-din-achanak/stream.ismd/manifest.m3u8?stream=144p;240p;320p;480p;720p;1080p&s=1626181322&e=1627477322&h=172f50dd341d154bfe070cad4d1a336e"
    private val subTitleExample = "https://st2.epicon.in/srt_files/839022827.vtt"

    private lateinit var playerView: PlayerView
    private var exoPlayer: SimpleExoPlayer? = null
    private var playWhenReady: Boolean = true
    private var currentWindow: Int = 0
    private var playbackPosition: Long = 0
    private var trackSelector: DefaultTrackSelector? = null
    private val playbackStateListener: Player.Listener = playbackStateListener()
    private var languagesList = ArrayList<String>()
    private var subtitleList = ArrayList<String>()

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
            exoPlayer?.let {
                it.seekTo(it.currentPosition + 20000)
                Log.e("Sound", "" + exoPlayer?.volume)
            }
        }

        findViewById<ConstraintLayout>(R.id.exo_double_tap_decrement).setOnClickListener {
            exoPlayer?.let {
                if (it.currentPosition > 20000) {
                    it.seekTo(it.currentPosition - 20000)
                } else {
                    it.seekTo(0)
                }
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
            exoPlayer = SimpleExoPlayer.Builder(this)
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
            currentWindow = this.currentWindowIndex
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

        override fun onTracksChanged(
            trackGroups: TrackGroupArray,
            trackSelections: TrackSelectionArray
        ) {
            super.onTracksChanged(trackGroups, trackSelections)
            Log.e("ExoPlayer - ", "onTrackChangedCall")
            if (languagesList.isEmpty()) {
                languagesList = PlayerUtils.getAvailableAudioFormat(trackGroups)
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
                subtitleList = PlayerUtils.getAvailableSubtitle(trackGroups)
                DialogUtils.createSubtitleDialog(this@MainActivity, subtitleList)
            }
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