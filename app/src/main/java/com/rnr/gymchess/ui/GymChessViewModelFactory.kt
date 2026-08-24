package com.rnr.gymchess.ui

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rnr.gymchess.GymChessApp
import com.rnr.gymchess.data.GameHistoryRepository
import com.rnr.gymchess.data.GameSessionHolder
import com.rnr.gymchess.ui.game.GameViewModel
import com.rnr.gymchess.ui.history.HistoryViewModel
import com.rnr.gymchess.ui.players.PlayersViewModel
import com.rnr.gymchess.ui.setup.SetupViewModel
import com.rnr.gymchess.util.GameFeedbackManager

object GymChessViewModelFactory {
    private lateinit var appContext: Context

    private val gameSessionHolder: GameSessionHolder by lazy {
        val app = appContext as GymChessApp
        GameSessionHolder(
            historySaver = app.historyRepository,
            scope = app.applicationScope
        )
    }

    val factory: ViewModelProvider.Factory by lazy {
        viewModelFactory {
            initializer { SetupViewModel(gameSessionHolder) }
            initializer { PlayersViewModel(gameSessionHolder) }
            initializer {
                GameViewModel(
                    sessionHolder = gameSessionHolder,
                    feedbackManager = feedbackManager
                )
            }
            initializer { HistoryViewModel(historyRepository) }
        }
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val historyRepository: GameHistoryRepository
        get() = (appContext as GymChessApp).historyRepository

    private val feedbackManager: GameFeedbackManager
        get() = (appContext as GymChessApp).feedbackManager

    fun clearSession() {
        gameSessionHolder.clear()
    }

    fun getSessionHolder(): GameSessionHolder = gameSessionHolder
}
