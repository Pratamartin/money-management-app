package com.pratatec.moneymgtapp.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pratatec.moneymgtapp.ui.auth.AuthViewModel
import com.pratatec.moneymgtapp.ui.auth.LoginScreen
import com.pratatec.moneymgtapp.ui.auth.RegisterScreen
import com.pratatec.moneymgtapp.ui.auth.SplashScreen

@Composable
fun NavGraph(viewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                viewModel = viewModel,
                onSessionValid = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNoSession = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
            )
        }
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
            )
        }
        composable("register") {
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = { navController.popBackStack() },
            )
        }
        composable("home") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Home — em construção",
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}
