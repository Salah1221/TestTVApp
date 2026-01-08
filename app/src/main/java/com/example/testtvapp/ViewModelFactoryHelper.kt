@file:Suppress("UNCHECKED_CAST")

package com.example.testtvapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory

fun <VM: ViewModel> viewModelFactory(initializer: () -> VM): Factory {
    return object : Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return initializer() as T
        }
    }
}