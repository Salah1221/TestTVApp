package com.example.testtvapp

import android.app.Application

class TestTvApplication: Application() {
    companion object {
        lateinit var container: AppContainer
    }

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this@TestTvApplication)
    }
}