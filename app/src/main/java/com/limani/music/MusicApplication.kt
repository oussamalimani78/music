package com.limani.music

import android.app.Application
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
    }
}
