package com.sorych.learn_how_to_code

import com.sorych.learn_how_to_code.ui.game.GameScreen
import LoginScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sorych.learn_how_to_code.ui.registration.RegisterScreen
import kotlinx.serialization.Serializable


sealed class Routes {
    @Serializable
    data object Login : Routes()

    @Serializable
    data object Registration : Routes()

    @Serializable
    data object Game : Routes()
}

@Composable
fun NavClass() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Login
    ) {
        composable<Routes.Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Game) {
                        popUpTo(Routes.Login) {inclusive = true}
                    }
                },
                onRegistrationClick = {
                    navController.navigate(Routes.Registration)
                }
            )
        }
        // You will also need to define your other destinations
        composable<Routes.Game> {
            GameScreen()
        }
        composable<Routes.Registration> {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.Game) {
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

