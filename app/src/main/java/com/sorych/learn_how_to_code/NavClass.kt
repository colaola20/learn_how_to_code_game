package com.sorych.learn_how_to_code

import com.sorych.learn_how_to_code.ui.game.GameScreen
import LoginScreen
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sorych.learn_how_to_code.ui.game.GameViewModel
import com.sorych.learn_how_to_code.ui.registration.RegisterScreen
import com.sorych.learn_how_to_code.ui.start.StartGameScreen
import kotlinx.serialization.Serializable


sealed class Routes {
    @Serializable
    data object Login : Routes()

    @Serializable
    data object Registration : Routes()

    @Serializable
    data object Game : Routes()

    @Serializable
    data object Start : Routes()
}

@Composable
fun NavClass() {
    val navController = rememberNavController()

    val gameViewModel: GameViewModel = viewModel(
        factory = GameViewModel.Factory
    )

    NavHost(
        navController = navController,
        startDestination = Routes.Login
    ) {
        composable<Routes.Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Start) {
                        popUpTo(Routes.Login) {inclusive = true}
                    }
                },
                onRegistrationClick = {
                    navController.navigate(Routes.Registration)
                }
            )
        }

        composable<Routes.Start> {
            StartGameScreen(
                onStartClick = {
                    navController.navigate(Routes.Game) {
                        popUpTo(Routes.Start) {inclusive = true}
                    }
                }
            )
        }
        // You will also need to define your other destinations
        composable<Routes.Game> {
            GameScreen(
                viewModel = gameViewModel,
                onExitClicked = {
                    navController.navigate(Routes.Start) {
                        popUpTo(Routes.Game) {inclusive = true}
                    }
                }
            )
        }
        composable<Routes.Registration> {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.Start) {
                        popUpTo(Routes.Registration) {inclusive = true}
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

