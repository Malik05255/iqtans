package com.iqtans.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.iqtans.app.data.BackendSearchClient
import com.iqtans.app.data.DemoDealRepository
import com.iqtans.app.data.LocalPreferences
import com.iqtans.app.ui.IqtansApp
import com.iqtans.app.ui.theme.IqtansTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val liveClient = BuildConfig.IQTANS_API_BASE_URL.trim().takeIf { it.startsWith("https://") || it.startsWith("http://10.0.2.2") }?.let(::BackendSearchClient)
        val storage = LocalPreferences(applicationContext)
        setContent { IqtansTheme { IqtansApp(repository = DemoDealRepository, liveClient = liveClient, storage = storage) } }
    }
}
