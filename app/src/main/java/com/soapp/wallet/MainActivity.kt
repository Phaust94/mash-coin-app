package com.soapp.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.soapp.wallet.ui.App
import com.soapp.wallet.ui.SoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = AppContainer(applicationContext)
        setContent {
            SoAppTheme {
                App(container)
            }
        }
    }
}
