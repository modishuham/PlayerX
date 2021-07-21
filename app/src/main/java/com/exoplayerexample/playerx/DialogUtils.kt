package com.exoplayerexample.playerx

import android.app.Dialog
import android.content.Context
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.view.get
import java.util.*
import kotlin.collections.ArrayList

object DialogUtils {

    private var mLanguageDialog: Dialog? = null
    private var mSubtitleDialog: Dialog? = null
    private var mVideoQualityDialog: Dialog? = null
    private var mPlaybackSpeedDialog: Dialog? = null

    fun createLanguageDialog(
        context: Context,
        languages: ArrayList<String>
    ) {
        mLanguageDialog = Dialog(context)
        mLanguageDialog?.setContentView(R.layout.dialog_player_languages)
        val radioGroup = mLanguageDialog?.findViewById<RadioGroup>(R.id.rg_lang)
        languages.forEachIndexed { index, s ->
            val radioButton = RadioButton(context)
            radioButton.text = s
            radioButton.id = index
            radioGroup?.addView(radioButton)
        }
        radioGroup?.check(0)
    }

    fun createSubtitleDialog(
        context: Context,
        subtitleList: ArrayList<String>
    ) {
        mSubtitleDialog = Dialog(context)
        mSubtitleDialog?.setContentView(R.layout.dialog_player_subtitle)
        val radioGroup = mSubtitleDialog?.findViewById<RadioGroup>(R.id.rg_subtitle)
        subtitleList.forEachIndexed { index, s ->
            val radioButton = RadioButton(context)
            radioButton.text = s
            radioButton.id = index
            radioGroup?.addView(radioButton)
        }
    }

    fun createVideoQualityDialog(
        context: Context,
        videoQualityHashMap: TreeMap<String, VideoTrackInfo>
    ) {
        mVideoQualityDialog = Dialog(context)
        mVideoQualityDialog?.setContentView(R.layout.dialog_player_video_quality)
        val radioGroup = mVideoQualityDialog?.findViewById<RadioGroup>(R.id.rg_video_quality)
        videoQualityHashMap.values.forEachIndexed { index, videoTrackInfo ->
            val radioButton = RadioButton(context)
            radioButton.text = videoTrackInfo.name
            radioButton.id = index
            radioButton.contentDescription = videoTrackInfo.valueForSort
            radioGroup?.addView(radioButton)
        }
        radioGroup?.check(0)
    }

    fun showLanguageDialog(
        languages: ArrayList<String>,
        playerSettingChangeListener: IPlayerSettingChange
    ) {
        val radioGroup = mLanguageDialog?.findViewById<RadioGroup>(R.id.rg_lang)
        radioGroup?.setOnCheckedChangeListener { _, i ->
            radioGroup.check(i)
            playerSettingChangeListener.onLanguageChanged(languages[i])
            mLanguageDialog?.dismiss()
        }
        mLanguageDialog?.show()
    }

    fun showSubtitleDialog(
        subtitleList: ArrayList<String>,
        playerSettingChangeListener: IPlayerSettingChange
    ) {
        val radioGroup = mSubtitleDialog?.findViewById<RadioGroup>(R.id.rg_subtitle)
        radioGroup?.setOnCheckedChangeListener { _, i ->
            radioGroup.check(i)
            playerSettingChangeListener.onSubtitleChanged(subtitleList[i])
            mSubtitleDialog?.dismiss()
        }
        mSubtitleDialog?.show()
    }

    fun showVideoQualityDialog(
        playerSettingChangeListener: IPlayerSettingChange,
        videoQualityHashMap: TreeMap<String, VideoTrackInfo>
    ) {
        val radioGroup = mVideoQualityDialog?.findViewById<RadioGroup>(R.id.rg_video_quality)
        radioGroup?.setOnCheckedChangeListener { _, i ->
            val radioButton = radioGroup[i]
            radioGroup.check(i)
            playerSettingChangeListener.onVideoQualityChanged(videoQualityHashMap[radioButton.contentDescription])
            mVideoQualityDialog?.dismiss()
        }
        mVideoQualityDialog?.show()
    }

    fun showPlaybackSpeedDialog(
        context: Context,
        playerSettingChangeListener: IPlayerSettingChange
    ) {
        if (mPlaybackSpeedDialog == null) {
            mPlaybackSpeedDialog = Dialog(context)
            mPlaybackSpeedDialog?.setContentView(R.layout.dialog_player_playback_speed)
        }
        val radioGroup = mPlaybackSpeedDialog?.findViewById<RadioGroup>(R.id.rg_speed)
        radioGroup?.setOnCheckedChangeListener { _, i ->
            val radioButton = radioGroup.findViewById<RadioButton>(i)
            val speed = if (radioButton.text == "Normal") "1.0" else radioButton.text.substring(
                0,
                radioButton.text.length - 1
            )
            radioGroup.check(i)
            playerSettingChangeListener.onPlaybackSpeedChanged(speed + "F")
            mPlaybackSpeedDialog?.dismiss()
        }
        mPlaybackSpeedDialog?.show()
    }
}