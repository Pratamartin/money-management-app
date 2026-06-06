package com.pratatec.moneymgtapp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pratatec.moneymgtapp.ui.auth.components.AuthFooterLink
import com.pratatec.moneymgtapp.ui.auth.components.MoneyMgtLogo
import com.pratatec.moneymgtapp.ui.auth.components.PasswordField
import com.pratatec.moneymgtapp.ui.auth.components.PrimaryButton
import com.pratatec.moneymgtapp.ui.profile.ThemeViewModel
import com.pratatec.moneymgtapp.ui.theme.AppTheme

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
) {
    val state = viewModel.loginState

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is AuthEvent.NavigateToHome) onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(80.dp))

            MoneyMgtLogo()

        Spacer(Modifier.height(48.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::updateEmail,
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        PasswordField(
            value = state.password,
            onValueChange = viewModel::updatePassword,
            imeAction = ImeAction.Done,
            onImeAction = viewModel::login,
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Esqueceu a senha?",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.End),
        )

        Spacer(Modifier.height(24.dp))

        PrimaryButton(
            text = "Entrar",
            onClick = viewModel::login,
            isLoading = state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.weight(1f))

        AuthFooterLink(
            text = "Não tem conta?",
            linkText = "Cadastre-se",
            onClick = onNavigateToRegister,
            modifier = Modifier.padding(bottom = 32.dp),
        )
        }

        IconButton(
            onClick = {
                themeViewModel.setTheme(
                    if (themeViewModel.appTheme == AppTheme.DARK) AppTheme.WHITE else AppTheme.DARK
                )
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
                .size(40.dp),
        ) {
            Icon(
                imageVector = if (themeViewModel.appTheme == AppTheme.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Alternar tema",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
