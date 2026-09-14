package com.iqtans.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.iqtans.app.data.DemoDealRepository
import com.iqtans.app.ui.IqtansApp
import com.iqtans.app.ui.theme.IqtansTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IqtansTheme {
                IqtansApp(repository = DemoDealRepository)
            }
        }
    }
}
