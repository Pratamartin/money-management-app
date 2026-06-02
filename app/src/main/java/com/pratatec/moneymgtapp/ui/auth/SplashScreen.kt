package com.pratatec.moneymgtapp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pratatec.moneymgtapp.ui.auth.components.MoneyMgtLogo

@Composable
fun SplashScreen(
    viewModel: AuthViewModel,
    onSessionValid: () -> Unit,
    onNoSession: () -> Unit,
) {
    LaunchedEffect(Unit) {
        if (viewModel.hasValidSession()) onSessionValid() else onNoSession()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        MoneyMgtLogo()
    }
}
