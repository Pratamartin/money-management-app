package com.pratatec.moneymgtapp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pratatec.moneymgtapp.R
import com.pratatec.moneymgtapp.ui.auth.components.AuthFooterLink
import com.pratatec.moneymgtapp.ui.auth.components.PasswordField
import com.pratatec.moneymgtapp.ui.auth.components.PrimaryButton
import com.pratatec.moneymgtapp.ui.profile.ThemeViewModel
import com.pratatec.moneymgtapp.ui.theme.AppTheme

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    themeViewModel: ThemeViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
) {
    val state = viewModel.registerState

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is AuthEvent.NavigateToLogin) onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateToLogin) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            IconButton(
                onClick = {
                    themeViewModel.setTheme(
                        if (themeViewModel.appTheme == AppTheme.DARK) AppTheme.WHITE else AppTheme.DARK
                    )
                },
            ) {
                Icon(
                    imageVector = if (themeViewModel.appTheme == AppTheme.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Alternar tema",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "Logo MoneyMgt",
            modifier = Modifier.size(52.dp),
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Criar conta",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Leva menos de um minuto.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = state.nome,
            onValueChange = viewModel::updateNome,
            label = { Text("Nome") },
            singleLine = true,
            isError = state.fieldErrors.containsKey("nome"),
            supportingText = state.fieldErrors["nome"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.username,
            onValueChange = viewModel::updateUsername,
            label = { Text("Usuário") },
            placeholder = { Text("ex: pratatec_") },
            singleLine = true,
            isError = state.fieldErrors.containsKey("username"),
            supportingText = state.fieldErrors["username"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::updateRegisterEmail,
            label = { Text("Email") },
            singleLine = true,
            isError = state.fieldErrors.containsKey("email"),
            supportingText = state.fieldErrors["email"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        PasswordField(
            value = state.password,
            onValueChange = viewModel::updateRegisterPassword,
            isError = state.fieldErrors.containsKey("password"),
            errorMessage = state.fieldErrors["password"],
            imeAction = ImeAction.Next,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        PasswordField(
            value = state.confirmPassword,
            onValueChange = viewModel::updateConfirmPassword,
            label = "Confirmar senha",
            isError = state.fieldErrors.containsKey("confirmPassword"),
            errorMessage = state.fieldErrors["confirmPassword"],
            imeAction = ImeAction.Done,
            onImeAction = viewModel::register,
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

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Ao se cadastrar, você concorda com nossos Termos e Política de Privacidade.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        PrimaryButton(
            text = "Criar conta",
            onClick = viewModel::register,
            isLoading = state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))

        AuthFooterLink(
            text = "Já tem conta?",
            linkText = "Entrar",
            onClick = onNavigateToLogin,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(Modifier.height(32.dp))
    }
}
