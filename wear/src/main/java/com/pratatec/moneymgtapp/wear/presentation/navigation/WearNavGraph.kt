package com.pratatec.moneymgtapp.wear.presentation.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.pratatec.moneymgtapp.wear.presentation.addgasto.AddGastoScreen
import com.pratatec.moneymgtapp.wear.presentation.addgasto.AddGastoViewModel
import com.pratatec.moneymgtapp.wear.presentation.addgasto.AddGastoViewModelFactory
import com.pratatec.moneymgtapp.wear.presentation.home.HomeScreen
import com.pratatec.moneymgtapp.wear.presentation.home.HomeViewModel
import com.pratatec.moneymgtapp.wear.presentation.home.HomeViewModelFactory

private const val ROUTE_HOME = "home"
private const val ROUTE_ADD_GASTO = "add_gasto"

@Composable
fun WearNavGraph(app: Application) {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = ROUTE_HOME,
    ) {
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
