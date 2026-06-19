package com.pratatec.moneymgtapp.wear.presentation.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.pratatec.moneymgtapp.wear.data.local.WearPinSession
import com.pratatec.moneymgtapp.wear.data.local.WearPinStorage
import com.pratatec.moneymgtapp.wear.data.local.WearTokenStorage
import com.pratatec.moneymgtapp.wear.presentation.addgasto.AddGastoScreen
import com.pratatec.moneymgtapp.wear.presentation.addgasto.AddGastoViewModel
import com.pratatec.moneymgtapp.wear.presentation.addgasto.AddGastoViewModelFactory
import com.pratatec.moneymgtapp.wear.presentation.home.HomeScreen
import com.pratatec.moneymgtapp.wear.presentation.home.HomeViewModel
import com.pratatec.moneymgtapp.wear.presentation.home.HomeViewModelFactory
import com.pratatec.moneymgtapp.wear.presentation.login.LoginScreen
import com.pratatec.moneymgtapp.wear.presentation.login.LoginViewModel
import com.pratatec.moneymgtapp.wear.presentation.login.LoginViewModelFactory
import com.pratatec.moneymgtapp.wear.presentation.pin.PinScreen
import com.pratatec.moneymgtapp.wear.presentation.pin.PinViewModel
import com.pratatec.moneymgtapp.wear.presentation.pin.PinViewModelFactory

private const val ROUTE_LOGIN = "login"
private const val ROUTE_PIN = "pin"
private const val ROUTE_HOME = "home"
private const val ROUTE_ADD_GASTO = "add_gasto"

@Composable
fun WearNavGraph(app: Application) {
    val tokenStorage = WearTokenStorage(app)
    val pinStorage = WearPinStorage(app)

    val start = when {
        !tokenStorage.hasTokens() -> ROUTE_LOGIN
        pinStorage.hasPin() && !WearPinSession.unlocked -> ROUTE_PIN
        else -> ROUTE_HOME
    }

    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = start,
    ) {
        composable(ROUTE_LOGIN) {
            val viewModel: LoginViewModel = viewModel(factory = LoginViewModelFactory(app))
            LoginScreen(
                uiState = viewModel.uiState,
                events = viewModel.events,
                onEmailChange = viewModel::updateEmail,
                onPasswordChange = viewModel::updatePassword,
                onLogin = viewModel::login,
                onSuccess = {
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(ROUTE_PIN) {
            val viewModel: PinViewModel = viewModel(factory = PinViewModelFactory(app))
            PinScreen(
                uiState = viewModel.uiState,
                onDigit = viewModel::onDigit,
                onDelete = viewModel::onDelete,
                onUnlocked = {
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(ROUTE_PIN) { inclusive = true }
                    }
                },
            )
        }
        composable(ROUTE_HOME) {
            val viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(app))
            HomeScreen(
                uiState = viewModel.uiState,
                onAddGasto = { navController.navigate(ROUTE_ADD_GASTO) },
                onRefresh = { viewModel.load() },
            )
        }
        composable(ROUTE_ADD_GASTO) {
            val viewModel: AddGastoViewModel = viewModel(factory = AddGastoViewModelFactory(app))
            AddGastoScreen(
                uiState = viewModel.uiState,
                events = viewModel.events,
                onSelectCategoria = viewModel::selectCategoria,
                onSelectValor = viewModel::selectValorIndex,
                onSave = viewModel::save,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
