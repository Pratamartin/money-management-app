package com.pratatec.moneymgtapp.wear

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pratatec.moneymgtapp.wear.presentation.navigation.WearNavGraph
import com.pratatec.moneymgtapp.wear.presentation.theme.WearAppTheme

class WearMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as Application
        setContent {
            WearAppTheme {
                WearNavGraph(app)
            }
        }
    }
}
