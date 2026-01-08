package com.example.testtvapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testtvapp.TestTvApplication
import com.example.testtvapp.ui.viewmodels.HomeViewModel

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val homeViewModel = viewModel<HomeViewModel>(
        factory = TestTvApplication.container.vmFactory
    )
}