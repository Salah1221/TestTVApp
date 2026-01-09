package com.example.testtvapp

import android.app.Application

class TestTvApplication: Application() {
    companion object {
        private lateinit var _container: AppContainer

        val container: AppContainer
            get() = if (::_container.isInitialized) {
                _container
            } else {
                throw IllegalStateException("AppContainer not initialized. Make sure TestTvApplication.onCreate() has been called.")
            }
    }

    override fun onCreate() {
        super.onCreate()
        _container = AppContainerImpl(this@TestTvApplication)
    }
}