package com.obsession.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.obsession.app.ui.navigation.ObsessionNavHost
import com.obsession.app.ui.theme.ObsessionTheme
import com.obsession.app.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
            ObsessionTheme(themeMode = themeMode) {
                ObsessionNavHost()
            }
        }
    }
}