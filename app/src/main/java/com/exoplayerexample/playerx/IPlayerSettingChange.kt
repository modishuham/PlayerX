package com.exoplayerexample.playerx

interface IPlayerSettingChange {
    fun onLanguageChanged(languageCode: String?)
    fun onSubtitleChanged(languageCode: String?)
    fun onPlaybackSpeedChanged(speed: String)
}