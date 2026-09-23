package com.limani.music

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.limani.music.data.repository.MusicRepository
import com.limani.music.data.repository.MusicRepositoryImpl
import com.limani.music.player.PlayerController

class MusicApplication : Application() {

    lateinit var repository: MusicRepository
        private set

    lateinit var playerController: PlayerController
        private set

    override fun onCreate() {
        super.onCreate()
        repository = MusicRepositoryImpl(this)
        playerController = PlayerController(this)

        initializeAdMob()
    }

    private fun initializeAdMob() {
        try {
            MobileAds.initialize(this) { initializationStatus ->
                Log.d(TAG, "AdMob MobileAds initialization complete: $initializationStatus")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AdMob SDK", e)
        }
    }

    companion object {
        private const val TAG = "MusicApplication"
    }
}
