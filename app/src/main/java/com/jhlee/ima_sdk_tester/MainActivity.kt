package com.jhlee.ima_sdk_tester

import android.app.Activity
import android.media.AudioManager
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer
import com.google.ads.interactivemedia.v3.api.AdsLoader
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory
import com.google.ads.interactivemedia.v3.api.player.AdMediaInfo


class MainActivity : Activity() {

    private var loader: AdsLoader? = null
    private lateinit var videoPlayer: VideoView
    private lateinit var videoPlayerContainer: ViewGroup
    private lateinit var adDisplayContainer: AdDisplayContainer
    private lateinit var videoAdPlayerAdapter: VideoAdPlayerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // FrameLayout 생성 및 설정
        // VideoView 생성 및 설정
        videoPlayer = VideoView(this)
        videoPlayerContainer = FrameLayout(this)


        val imaSdkFactory = ImaSdkFactory.getInstance()
        val settings = imaSdkFactory.createImaSdkSettings()
        settings.autoPlayAdBreaks = false
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
        videoAdPlayerAdapter = VideoAdPlayerAdapter(videoPlayer, audioManager)
        adDisplayContainer = ImaSdkFactory.createAdDisplayContainer(videoPlayerContainer, videoAdPlayerAdapter)
        ImaSdkFactory.getInstance().createAdsLoader(this, settings, adDisplayContainer)
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        adDisplayContainer.destroy()
//        if (loader != null) {
//            loader?.release()
//            loader = null
//        }
//        videoAdPlayerAdapter.release()
//    }
}