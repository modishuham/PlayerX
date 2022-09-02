package com.exoplayerexample.playerx

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.common.collect.ImmutableList
import java.util.*

object PlayerUtils {

    private var videoRendererIndex: Int = -1
    private var trackGroupArray: TrackGroupArray? = null
    private var videoTrackInfoHashMap: TreeMap<String, VideoTrackInfo>? = null

    private val SUPPORTED_TRACK_TYPES: ImmutableList<Int> =
        ImmutableList.of(C.TRACK_TYPE_VIDEO, C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_TEXT)

    /**
     * Returns whether a track selection dialog will have content to display if initialized with the
     * specified [Player].
     */
    fun willHaveContent(player: Player): Boolean {
        return willHaveContent(player.currentTracks)
    }

    /**
     * Returns whether a track selection dialog will have content to display if initialized with the
     * specified [Tracks].
     */
    private fun willHaveContent(tracks: Tracks): Boolean {
        for (trackGroup in tracks.groups) {
            if (SUPPORTED_TRACK_TYPES.contains(trackGroup.type)) {
                return true
            }
        }
        return false
    }

    /*
    * This method change playback speed of exoPlayer
    * speed can be any of 1.0F, 1.25F, 1.50F, 1.75F, 2.0F
    * */
    fun changePlaybackSpeed(
        speed: Float = 1.0F,
        exoPlayer: ExoPlayer?
    ) {
        val playbackParameters = PlaybackParameters(speed)
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
    @Deprecated("")
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
    @Deprecated("")
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

    fun getAvailableAudio(currentTracks: Tracks): ArrayList<String> {
        val audioLanguages = ArrayList<String>()
        for (trackGroup in currentTracks.groups) {
            if (trackGroup.type == C.TRACK_TYPE_AUDIO) {
                audioLanguages.add(trackGroup.getTrackFormat(0).language.toString())
            }
        }
        return audioLanguages
    }

    fun getAvailableSubtitles(currentTracks: Tracks): ArrayList<String> {
        val subtitleList = ArrayList<String>()
        for (trackGroup in currentTracks.groups) {
            if (trackGroup.type == C.TRACK_TYPE_TEXT) {
                subtitleList.add(trackGroup.getTrackFormat(0).language.toString())
            }
        }
        return subtitleList
    }

    fun getAvailableVideoQualities(currentTracks: Tracks): ArrayList<Int> {
        val qualityList = ArrayList<Int>()
        for (trackGroup in currentTracks.groups) {
            if (trackGroup.type == C.TRACK_TYPE_VIDEO) {
                for (trackIndex in 0 until trackGroup.length) {
                    qualityList.add(trackGroup.getTrackFormat(trackIndex).bitrate / 1000)
                }
            }
        }
        Log.e("Shubham________", "" + qualityList.toString())
        return qualityList
    }

    fun getAvailableVideoQualities(): TreeMap<String, VideoTrackInfo>? {
        return videoTrackInfoHashMap
    }

    /**
     *  This method used to get available video qualities in adaptive playback url
     */
    fun initVideoQualities(
        exoPlayer: ExoPlayer?,
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
        videoTrackInfoHashMap = TreeMap()
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
                    val bitrateCurrent = (format.bitrate / 1000).toLong()
                    Log.e("Bitrate", " $bitrateCurrent")
                    getVideoQualityFromBitrate(videoTrackInfo)
                }
            }
        }
        if (videoTrackInfoHashMap != null && videoTrackInfoHashMap!!.size > 0) {
            val videoTrackInfo = VideoTrackInfo()
            videoTrackInfo.name = "Auto"
            videoTrackInfo.valueForSort = "A"
            videoTrackInfoHashMap!![videoTrackInfo.valueForSort.toString()] = videoTrackInfo
        }
        Log.e("VideoQualities", "" + videoTrackInfoHashMap!!.toString())
    }


    /*
    * This method is used to change quality of adaptive playback url
    * */
    fun changeVideoQuality(
        context: Context,
        videoTrackInfo: VideoTrackInfo?,
        trackSelector: DefaultTrackSelector?
    ) {
        val parametersBuilder = trackSelector?.buildUponParameters()
        if (videoTrackInfo?.name.equals("Auto", true)) {
            parametersBuilder?.clearSelectionOverrides(videoRendererIndex)
            trackSelector?.setParameters(parametersBuilder!!)
            return
        }
        var override: DefaultTrackSelector.SelectionOverride? = null
        if (videoTrackInfo != null) {
            override = DefaultTrackSelector.SelectionOverride(
                videoTrackInfo.groupIndex,
                videoTrackInfo.trackIndex
            )
        }
        if (override != null) {
            trackSelector?.parameters = DefaultTrackSelector.ParametersBuilder(context).build()
            parametersBuilder?.setSelectionOverride(videoRendererIndex, trackGroupArray!!, override)
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

    private fun getVideoQualityFromBitrate(videoTrackInfo: VideoTrackInfo) {
        when (videoTrackInfo.bitrate / 1000) {
            in 10..300 -> {
                videoTrackInfo.name = "144p"
                videoTrackInfo.valueForSort = "B"
                putVideoQualitiesInHashMap(videoTrackInfo)
            }
            in 300..700 -> {
                videoTrackInfo.name = "240p"
                videoTrackInfo.valueForSort = "C"
                putVideoQualitiesInHashMap(videoTrackInfo)
            }
            in 400..1000 -> {
                videoTrackInfo.name = "360p"
                videoTrackInfo.valueForSort = "D"
                putVideoQualitiesInHashMap(videoTrackInfo)

            }
            in 500..2000 -> {
                videoTrackInfo.name = "480p"
                videoTrackInfo.valueForSort = "E"
                putVideoQualitiesInHashMap(videoTrackInfo)

            }
            in 1500..4000 -> {
                videoTrackInfo.name = "720p"
                videoTrackInfo.valueForSort = "F"
                putVideoQualitiesInHashMap(videoTrackInfo)
            }
            in 3000..6000 -> {
                videoTrackInfo.name = "1080p"
                videoTrackInfo.valueForSort = "G"
                putVideoQualitiesInHashMap(videoTrackInfo)

            }
            in 6000..13000 -> {
                videoTrackInfo.name = "1440p"
                videoTrackInfo.valueForSort = "H"
                putVideoQualitiesInHashMap(videoTrackInfo)
            }
            in 13000..34000 -> {
                videoTrackInfo.name = "4k"
                videoTrackInfo.valueForSort = "I"
                putVideoQualitiesInHashMap(videoTrackInfo)
            }
        }

    }

    private fun putVideoQualitiesInHashMap(videoTrackInfo: VideoTrackInfo) {
        if (videoTrackInfoHashMap?.contains(videoTrackInfo.valueForSort.toString()) != true) {
            videoTrackInfoHashMap?.put(
                videoTrackInfo.valueForSort.toString(),
                videoTrackInfo
            )
        }
    }
}