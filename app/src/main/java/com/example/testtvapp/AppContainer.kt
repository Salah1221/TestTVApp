package com.example.testtvapp

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.example.testtvapp.data.repository.MediaRepository
import com.example.testtvapp.data.repository.MediaRepositoryImpl
import com.example.testtvapp.ui.viewmodels.HomeViewModel

interface AppContainer {
    val mediaRepository: MediaRepository
    val vmFactory: ViewModelProvider.Factory
}

class AppContainerImpl(private val context: Context): AppContainer {
    override val mediaRepository: MediaRepository by lazy {
        MediaRepositoryImpl(context)
    }

    override val vmFactory: ViewModelProvider.Factory by lazy {
        viewModelFactory {
            HomeViewModel(mediaRepository)
        }
    }
}