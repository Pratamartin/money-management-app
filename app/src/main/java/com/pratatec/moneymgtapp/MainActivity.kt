package com.pratatec.moneymgtapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pratatec.moneymgtapp.ui.auth.AuthViewModel
import com.pratatec.moneymgtapp.ui.navigation.NavGraph
import com.pratatec.moneymgtapp.ui.theme.MoneymgtappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoneymgtappTheme {
                val viewModel: AuthViewModel = viewModel()
                NavGraph(viewModel = viewModel)
            }
        }
    }
}
