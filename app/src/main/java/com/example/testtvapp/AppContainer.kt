package com.example.testtvapp

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.example.testtvapp.data.repository.MediaRepository
import com.example.testtvapp.data.repository.MediaRepositoryImpl
import com.example.testtvapp.ui.viewmodels.HomeViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

interface AppContainer {
    val mediaRepository: MediaRepository
    val vmFactory: ViewModelProvider.Factory
}

class AppContainerImpl(private val context: Context): AppContainer {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(
                json = Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }
    override val mediaRepository: MediaRepository by lazy {
        MediaRepositoryImpl(context, client)
    }

    override val vmFactory: ViewModelProvider.Factory by lazy {
        viewModelFactory {
            HomeViewModel(mediaRepository)
        }
    }
}