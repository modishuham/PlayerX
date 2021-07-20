package com.exoplayerexample.playerx

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util

object PlayerUtils {

    private var videoRendererIndex: Int = -1
    private var trackGroupArray: TrackGroupArray? = null
    private var videoTrackInfoHashMap: HashMap<String, VideoTrackInfo>? = null

    /*
    * This method change playback speed of exoPlayer
    * speed can be any of 1.0F, 1.25F, 1.50F, 1.75F, 2.0F
    * */
    fun changePlaybackSpeed(
        speed: Float = 1.0F,
        exoPlayer: SimpleExoPlayer?
    ) {
        val playbackParameters: PlaybackParameters = PlaybackParameters(speed)
        exoPlayer?.playbackParameters = playbackParameters
    }

    /*
    * This method change audio of adaptive playback url
    * */
    fun changePlayerAudio(
        trackSelector: DefaultTrackSelector?,
        languageCode: String = ""
    ) {
        trackSelector?.let {
            it.setParameters(
                it.buildUponParameters()
                    .setPreferredAudioLanguage(languageCode)
                //.setRendererDisabled(C.TRACK_TYPE_AUDIO, false)
                //.setRendererDisabled(C.TRACK_TYPE_VIDEO, false)
                //.setRendererDisabled(C.TRACK_TYPE_TEXT, false)
            )
        }
    }

    /**
     * Language code in two character code format as in our case
     * subtitle format contain language code in two character format
     *
     * @param languageCode
     */
    fun changeMediaSubtitle(
        trackSelector: DefaultTrackSelector?,
        languageCode: String?
    ) {
        trackSelector?.setParameters(
            trackSelector
                .buildUponParameters()
                .setPreferredTextLanguage(languageCode)
            //.setRendererDisabled(C.TRACK_TYPE_AUDIO, false)
            //.setRendererDisabled(C.TRACK_TYPE_VIDEO, false)
            //.setRendererDisabled(C.TRACK_TYPE_TEXT, false)
        )
    }

    /**
     *  This method used to get available subtitle in adaptive playback url
     */
    fun getAvailableSubtitle(trackGroups: TrackGroupArray?): ArrayList<String> {
        val subTitle = ArrayList<String>()
        trackGroups?.let {
            for (i in 0 until trackGroups.length) {
                val formatLength = trackGroups[i].length
                for (j in 0 until formatLength) {
                    val format = trackGroups[i].getFormat(j)
                    if (format.sampleMimeType != null) {
                        if (format.sampleMimeType!!.contains("text")) {
                            if (!TextUtils.isEmpty(format.language) && subTitle.indexOf(format.language) == -1) {
                                subTitle.add(format.language!!)
                            }
                        }
                    }
                }
            }
        }
        Log.e("ExoPlayer SubTitles - ", subTitle.toString())
        return subTitle
    }

    /**
     *  This method used to get available audio in adaptive playback url
     */
    fun getAvailableAudioFormat(trackGroups: TrackGroupArray?): ArrayList<String> {
        val languages = ArrayList<String>()
        trackGroups?.let {
            for (i in 0 until trackGroups.length) {
                val formatLength = trackGroups[i].length
                for (j in 0 until formatLength) {
                    val format = trackGroups[i].getFormat(j)
                    if (format.sampleMimeType != null) {
                        if (format.sampleMimeType!!.contains("audio")) {
                            if (!TextUtils.isEmpty(format.language) &&
                                languages.indexOf(format.language) == -1
                            ) {
                                languages.add(format.language!!)
                            }
                        }
                    }
                }
            }
        }
        Log.e("ExoPlayer Audio - ", languages.toString())
        return languages
    }

    fun getAvailableVideoQualities(): HashMap<String, VideoTrackInfo>? {
        return videoTrackInfoHashMap
    }

    /**
     *  This method used to get available video qualities in adaptive playback url
     */
    fun initVideoQualities(
        exoPlayer: SimpleExoPlayer?,
        trackSelector: DefaultTrackSelector?
    ) {
        videoRendererIndex = -1
        val mappedTrackInfo = trackSelector?.currentMappedTrackInfo
        mappedTrackInfo?.let {
            for (i in 0 until it.rendererCount) {
                val trackGroups: TrackGroupArray = it.getTrackGroups(i)
                if (trackGroups.length != 0) {
                    when (exoPlayer?.getRendererType(i)) {
                        C.TRACK_TYPE_AUDIO -> {
                        }
                        C.TRACK_TYPE_VIDEO -> videoRendererIndex = i
                        C.TRACK_TYPE_TEXT -> {
                        }
                        else -> continue
                    }
                }
            }
            if (videoRendererIndex == -1) {
                return
            }
            trackGroupArray = it.getTrackGroups(videoRendererIndex)
            initQualityHashMap(it, trackGroupArray, videoRendererIndex)
        }
    }

    private fun initQualityHashMap(
        mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
        trackGroups: TrackGroupArray?,
        rendererIndex: Int
    ) {
        videoTrackInfoHashMap = HashMap()
        for (groupIndex in 0 until trackGroups!!.length) {
            val group = trackGroups[groupIndex]
            for (trackIndex in 0 until group.length) {
                val format = group.getFormat(trackIndex)
                if (mappedTrackInfo.getTrackSupport(
                        rendererIndex,
                        groupIndex,
                        trackIndex
                    ) == C.FORMAT_HANDLED
                ) {
                    val videoTrackInfo = VideoTrackInfo()
                    videoTrackInfo.groupIndex = groupIndex
                    videoTrackInfo.trackIndex = trackIndex
                    videoTrackInfo.bitrate = format.bitrate.toLong()
                    //val availableVideoQuality: VideoQuality = getAvailableVideoQuality()
                    val bitrateCurrent = (format.bitrate / 1000).toLong()
                    /*val bitrateArrayList: ArrayList<Bitrate> =
                        availableVideoQuality.getBitrateArrayList()
                    val bitrateHigh: Long = bitrateArrayList[1].getBitrate()
                    val bitrateMid: Long = bitrateArrayList[2].getBitrate()
                    val bitrateLow: Long = bitrateArrayList[3].getBitrate()
                    if (bitrateCurrent > bitrateMid) {
                        //high
                        if (videoTrackInfoHashMap.get(AppConstant.PLAYER_VIDEO_QUALITY_HIGH) == null) {
                            videoTrackInfoHashMap.put(
                                AppConstant.PLAYER_VIDEO_QUALITY_HIGH,
                                videoTrackInfo
                            )
                        } else {
                            val videoTrackInfo1: VideoTrackInfo =
                                videoTrackInfoHashMap.get(AppConstant.PLAYER_VIDEO_QUALITY_HIGH)
                            if (bitrateCurrent > videoTrackInfo1.getBitrate()) {
                                videoTrackInfoHashMap.put(
                                    AppConstant.PLAYER_VIDEO_QUALITY_HIGH,
                                    videoTrackInfo
                                )
                            }
                        }
                    } else if (bitrateCurrent > bitrateLow) {
                        //mid
                        if (videoTrackInfoHashMap.get(AppConstant.PLAYER_VIDEO_QUALITY_MEDIUM) == null) {
                            videoTrackInfoHashMap.put(
                                AppConstant.PLAYER_VIDEO_QUALITY_MEDIUM,
                                videoTrackInfo
                            )
                        } else {
                            val videoTrackInfo1: VideoTrackInfo =
                                videoTrackInfoHashMap.get(AppConstant.PLAYER_VIDEO_QUALITY_MEDIUM)
                            if (bitrateCurrent > videoTrackInfo1.getBitrate()) {
                                videoTrackInfoHashMap.put(
                                    AppConstant.PLAYER_VIDEO_QUALITY_MEDIUM,
                                    videoTrackInfo
                                )
                            }
                        }
                    } else {
                        //low
                        if (videoTrackInfoHashMap.get(AppConstant.PLAYER_VIDEO_QUALITY_LOW) == null) {
                            videoTrackInfoHashMap.put(
                                AppConstant.PLAYER_VIDEO_QUALITY_LOW,
                                videoTrackInfo
                            )
                        } else {
                            val videoTrackInfo1: VideoTrackInfo =
                                videoTrackInfoHashMap.get(AppConstant.PLAYER_VIDEO_QUALITY_LOW)
                            if (bitrateCurrent > videoTrackInfo1.getBitrate()) {
                                videoTrackInfoHashMap.put(
                                    AppConstant.PLAYER_VIDEO_QUALITY_LOW,
                                    videoTrackInfo
                                )
                            }
                        }
                    }*/
                    //if (bitrateCurrent > bitrateHigh) {
                    videoTrackInfoHashMap?.put(
                        "Quality$trackIndex",
                        videoTrackInfo
                    )
                    Log.e(
                        "Quality$trackIndex",
                        "" + videoTrackInfo.bitrate
                    )
                    //}
                }
            }
        }
    }


    /*
    * This method is used to change quality of adaptive playback url
    * */
    fun changeVideoQuality(
        context: Context,
        quality: String,
        trackSelector: DefaultTrackSelector?
    ) {
        var override: DefaultTrackSelector.SelectionOverride? = null
        val videoTrackInfo = videoTrackInfoHashMap?.get(quality)
        if (videoTrackInfo != null) {
            override = DefaultTrackSelector.SelectionOverride(
                videoTrackInfo.groupIndex,
                videoTrackInfo.trackIndex
            )
        }
        val parametersBuilder = trackSelector?.buildUponParameters()
        if (override != null) {
            trackSelector?.parameters = DefaultTrackSelector.ParametersBuilder(context).build()
            parametersBuilder?.setSelectionOverride(videoRendererIndex, trackGroupArray!!, override)
        } else {
            parametersBuilder?.clearSelectionOverrides(videoRendererIndex)
        }
        trackSelector?.setParameters(parametersBuilder!!)
    }

    fun getMediaItemFromUrl(
        url: String,
        subTitleUrl: String?,
        subTitleMimeTypes: String
    ): MediaItem? {

        var subTitleList = ArrayList<MediaItem.Subtitle>()
        if (subTitleUrl != null) {
            subTitleList = getSubTitleFromUrl(subTitleUrl, subTitleMimeTypes)
        }

        when (Util.inferContentType(Uri.parse(url), null)) {
            C.TYPE_HLS -> {
                return MediaItem.Builder()
                    .setUri(url)
                    .setSubtitles(subTitleList)
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .build()
            }
            C.TYPE_DASH -> {
                return MediaItem.Builder()
                    .setUri(url)
                    .setSubtitles(subTitleList)
                    .setMimeType(MimeTypes.APPLICATION_MPD)
                    .build()
            }
            C.TYPE_OTHER -> {
                return MediaItem.Builder()
                    .setUri(url)
                    .setSubtitles(subTitleList)
                    .setMimeType(MimeTypes.APPLICATION_MP4)
                    .build()
            }
            else -> {
                return null
            }
        }
    }

    /*
    * This method return MediaItem.SubTitle from the url.
    * */
    private fun getSubTitleFromUrl(
        subTitleUrl: String,
        mimeTypes: String
    ): ArrayList<MediaItem.Subtitle> {
        val subTitleList = ArrayList<MediaItem.Subtitle>()
        when (mimeTypes) {
            MimeTypes.TEXT_VTT -> {
                val subT = MediaItem.Subtitle(Uri.parse(subTitleUrl), MimeTypes.TEXT_VTT, "sub")
                subTitleList.add(subT)
            }
            MimeTypes.TEXT_SSA -> {
                val subT = MediaItem.Subtitle(Uri.parse(subTitleUrl), MimeTypes.TEXT_SSA, "sub")
                subTitleList.add(subT)
            }
        }
        return subTitleList
    }
}