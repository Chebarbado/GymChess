package com.rnr.gymchess.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rnr.gymchess.ui.GymChessViewModelFactory
import com.rnr.gymchess.ui.game.GameScreen
import com.rnr.gymchess.ui.history.HistoryScreen
import com.rnr.gymchess.ui.players.PlayersScreen
import com.rnr.gymchess.ui.result.ResultScreen
import com.rnr.gymchess.ui.setup.SetupScreen

@Composable
fun GymChessNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SETUP
    ) {
        composable(Routes.SETUP) {
            SetupScreen(
                onContinue = {
                    navController.navigate(Routes.PLAYERS)
                },
                onOpenHistory = {
                    navController.navigate(Routes.HISTORY)
                }
            )
        }

        composable(Routes.PLAYERS) {
            PlayersScreen(
                onBack = { navController.popBackStack() },
                onStartGame = {
                    navController.navigate(Routes.GAME) {
                        popUpTo(Routes.SETUP) { inclusive = false }
                    }
                }
            )
        }

        composable(Routes.GAME) {
            GameScreen(
                onGameFinished = {
                    navController.navigate(Routes.RESULT) {
                        popUpTo(Routes.GAME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.RESULT) {
            val gameState = GymChessViewModelFactory.getSessionHolder().gameState

            ResultScreen(
                gameState = gameState,
                onNewGame = {
                    GymChessViewModelFactory.clearSession()
                    navController.navigate(Routes.SETUP) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onOpenHistory = {
                    navController.navigate(Routes.HISTORY)
                }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
