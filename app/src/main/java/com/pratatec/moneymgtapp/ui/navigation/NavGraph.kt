package com.pratatec.moneymgtapp.ui.navigation

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pratatec.moneymgtapp.ui.auth.AuthViewModel
import com.pratatec.moneymgtapp.ui.auth.LoginScreen
import com.pratatec.moneymgtapp.ui.auth.RegisterScreen
import com.pratatec.moneymgtapp.ui.auth.SplashScreen
import com.pratatec.moneymgtapp.ui.category.CategoryScreen
import com.pratatec.moneymgtapp.ui.category.CategoryViewModel
import com.pratatec.moneymgtapp.ui.daily.DailyScreen
import com.pratatec.moneymgtapp.ui.daily.DailyViewModel
import com.pratatec.moneymgtapp.ui.daily.DailyViewModelFactory
import com.pratatec.moneymgtapp.ui.home.HomeScreen
import com.pratatec.moneymgtapp.ui.home.HomeEvent
import com.pratatec.moneymgtapp.ui.home.HomeViewModel
import com.pratatec.moneymgtapp.ui.home.HomeViewModelFactory
import com.pratatec.moneymgtapp.ui.profile.ProfileScreen
import com.pratatec.moneymgtapp.ui.profile.ThemeViewModel

@Composable
fun NavGraph(viewModel: AuthViewModel, themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                viewModel = viewModel,
                onSessionValid = {
                    navController.navigate("main") {
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
                themeViewModel = themeViewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
            )
        }
        composable("register") {
            RegisterScreen(
                viewModel = viewModel,
                themeViewModel = themeViewModel,
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = { navController.popBackStack() },
            )
        }
        composable("main") {
            val app = LocalContext.current.applicationContext as Application
            val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(app))
            LaunchedEffect(homeViewModel) {
                homeViewModel.events.collect { event ->
                    when (event) {
                        HomeEvent.SessionExpired -> navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }
            MainScaffold(
                homeViewModel = homeViewModel,
                themeViewModel = themeViewModel,
                onNavigateToCategories = { navController.navigate("categories") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
        composable("categories") {
            val categoryViewModel: CategoryViewModel = viewModel()
            CategoryScreen(
                viewModel = categoryViewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun MainScaffold(
    homeViewModel: HomeViewModel,
    themeViewModel: ThemeViewModel,
    onNavigateToCategories: () -> Unit,
    onLogout: () -> Unit,
) {
    val tabNavController = rememberNavController()
    val currentEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = currentEntry?.destination?.route

    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                NavigationBarItem(
                    selected = currentRoute == "home",
                    onClick = {
                        tabNavController.navigate("home") {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(tabNavController.graph.startDestinationId) { saveState = true }
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Início") },
                    colors = navItemColors,
                )
                NavigationBarItem(
                    selected = currentRoute == "monthly",
                    onClick = {
                        tabNavController.navigate("monthly") {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(tabNavController.graph.startDestinationId) { saveState = true }
                        }
                    },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text("Mensal") },
                    colors = navItemColors,
                )
                NavigationBarItem(
                    selected = currentRoute == "daily",
                    onClick = {
                        tabNavController.navigate("daily") {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(tabNavController.graph.startDestinationId) { saveState = true }
                        }
                    },
                    icon = { Icon(Icons.Default.Today, contentDescription = null) },
                    label = { Text("Diário") },
                    colors = navItemColors,
                )
                NavigationBarItem(
                    selected = currentRoute == "profile",
                    onClick = {
                        tabNavController.navigate("profile") {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(tabNavController.graph.startDestinationId) { saveState = true }
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Perfil") },
                    colors = navItemColors,
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToCategories = onNavigateToCategories,
                )
            }
            composable("monthly") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Controle Mensal — em breve", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            composable("daily") {
                val periodo = homeViewModel.uiState.periodo
                if (periodo != null) {
                    val app = LocalContext.current.applicationContext as Application
                    val dailyViewModel: DailyViewModel = viewModel(
                        key = "daily_${periodo.id}",
                        factory = DailyViewModelFactory(app, periodo.id),
                    )
                    DailyScreen(
                        viewModel = dailyViewModel,
                        mes = periodo.mes,
                        ano = periodo.ano,
                        onBack = {
                            tabNavController.navigate("home") { launchSingleTop = true }
                        },
                    )
                } else if (homeViewModel.uiState.isLoading) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Configure um período na aba Início.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            composable("profile") {
                ProfileScreen(
                    viewModel = themeViewModel,
                    onLogout = onLogout,
                )
            }
        }
    }
}
