package com.jhlee.ima_sdk_tester

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.util.Log
import android.widget.VideoView
import com.google.ads.interactivemedia.v3.api.AdPodInfo
import com.google.ads.interactivemedia.v3.api.player.AdMediaInfo
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer
import com.google.ads.interactivemedia.v3.api.player.VideoAdPlayer.VideoAdPlayerCallback
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate
import java.io.File
import java.util.*

class VideoAdPlayerAdapter(private val videoPlayer: VideoView, private val audioManager: AudioManager) : VideoAdPlayer {
    private val videoAdPlayerCallbacks: MutableList<VideoAdPlayerCallback> = ArrayList<VideoAdPlayerCallback>()
    private var timer: Timer? = null
    private var adDuration = 0
    private var savedAdPosition = 0
    private var loadedAdMediaInfo: AdMediaInfo? = null

    init {
        this.videoPlayer.setOnCompletionListener(OnCompletionListener { mediaPlayer: MediaPlayer? ->
            notifyImaOnContentCompleted()
        })
    }

    override fun addCallback(videoAdPlayerCallback: VideoAdPlayerCallback) {
        videoAdPlayerCallbacks.add(videoAdPlayerCallback!!)
    }

    override fun removeCallback(videoAdPlayerCallback: VideoAdPlayerCallback) {
        videoAdPlayerCallbacks.remove(videoAdPlayerCallback)
    }

    override fun loadAd(adMediaInfo: AdMediaInfo, adPodInfo: AdPodInfo) {
        loadedAdMediaInfo = adMediaInfo
        // Additional ad loading logic here
    }

    override fun playAd(adMediaInfo: AdMediaInfo) {
        val adUri: Uri?
        val file: File? = null;
        if (file != null) {
            adUri = Uri.fromFile(file)
        } else {
            adUri = Uri.parse(adMediaInfo.getUrl())
        }

        videoPlayer.setVideoURI(adUri)
        videoPlayer.setOnPreparedListener(OnPreparedListener { mediaPlayer: MediaPlayer? ->
            adDuration = mediaPlayer!!.getDuration()
            if (savedAdPosition > 0) {
                mediaPlayer.seekTo(savedAdPosition)
            }
            mediaPlayer.start()
            startAdTracking()
        })
        videoPlayer.setOnErrorListener(MediaPlayer.OnErrorListener { mediaPlayer: MediaPlayer?, errorType: Int, extra: Int ->
            notifyImaSdkAboutAdError(errorType)
            true
        })
        videoPlayer.setOnCompletionListener(OnCompletionListener { mediaPlayer: MediaPlayer? ->
            savedAdPosition = 0
            notifyImaSdkAboutAdEnded()
        })
    }

    override fun pauseAd(adMediaInfo: AdMediaInfo) {
        Log.i(LOGTAG, "pauseAd")
        savedAdPosition = videoPlayer.getCurrentPosition()
        stopAdTracking()
    }

    override fun stopAd(adMediaInfo: AdMediaInfo) {
        Log.i(LOGTAG, "stopAd")
        stopAdTracking()
    }

    override fun release() {
        Log.d("jhlee", "adapter release()")
        // Clean up resources here
        stopAdTracking() // timer.cancel(); timer = null; 내부에서 처리
        if (timer != null) {
            try {
                timer!!.cancel()
                timer!!.purge()
            } catch (ignored: Exception) {
            }
            timer = null
        }


        // 2) VideoView 재생 중지 및 리소스 해제 (UI 스레드에서 실행 권장)
        if (videoPlayer != null) {
            try {
                // 리스너 해제: Activity/Context 참조 고리 차단
                videoPlayer.setOnPreparedListener(null)
                videoPlayer.setOnCompletionListener(null)
                videoPlayer.setOnErrorListener(null)

                // 재생 중지(내부 MediaPlayer 해제)
                videoPlayer.stopPlayback()

                // (선택) 이 어댑터가 뷰 생명주기까지 책임진다면 부모에서 분리
                // ViewParent parent = videoPlayer.getParent();
                // if (parent instanceof ViewGroup) {
                //     ((ViewGroup) parent).removeView(videoPlayer);
                // }
            } catch (ignored: Exception) {
            }
        }


        // 3) IMA 콜백 및 상태 정리
        videoAdPlayerCallbacks.clear()
        loadedAdMediaInfo = null
        savedAdPosition = 0
        adDuration = 0
    }

    override fun getVolume(): Int {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / audioManager.getStreamMaxVolume(
            AudioManager.STREAM_MUSIC
        )
    }

    override fun getAdProgress(): VideoProgressUpdate {
        val adPosition = videoPlayer.getCurrentPosition().toLong()
        return VideoProgressUpdate(adPosition, adDuration.toLong())
    }

    private fun startAdTracking() {
        Log.i(LOGTAG, "startAdTracking")
        if (timer != null) {
            return
        }
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                val progressUpdate = getAdProgress()
                notifyImaSdkAboutAdProgress(progressUpdate)
            }
        }, INITIAL_DELAY_MS, POLLING_TIME_MS)
    }

    private fun stopAdTracking() {
        Log.i(LOGTAG, "stopAdTracking")
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    private fun notifyImaSdkAboutAdProgress(adProgress: VideoProgressUpdate) {
        for (callback in videoAdPlayerCallbacks) {
            callback.onAdProgress(loadedAdMediaInfo!!, adProgress)
        }
    }

    private fun notifyImaSdkAboutAdEnded() {
        Log.i(LOGTAG, "notifyImaSdkAboutAdEnded")
        for (callback in videoAdPlayerCallbacks) {
            callback.onEnded(loadedAdMediaInfo!!)
        }
    }

    private fun notifyImaSdkAboutAdError(errorType: Int) {
        Log.i(LOGTAG, "notifyImaSdkAboutAdError")
        for (callback in videoAdPlayerCallbacks) {
            callback.onError(loadedAdMediaInfo!!)
        }
    }

    private fun notifyImaOnContentCompleted() {
        Log.i(LOGTAG, "notifyImaOnContentCompleted")
        for (callback in videoAdPlayerCallbacks) {
            callback.onContentComplete()
        }
    }

    companion object {
        private const val LOGTAG = "IMABasicSample"
        private const val POLLING_TIME_MS: Long = 250
        private const val INITIAL_DELAY_MS: Long = 250
    }
}

