package com.rnr.gymchess.data

import com.rnr.gymchess.domain.model.GameConfig
import com.rnr.gymchess.domain.model.GamePhase
import com.rnr.gymchess.domain.model.GameState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class GameSessionHolder(
    private val historySaver: GameHistorySaver? = null,
    private val scope: CoroutineScope? = null
) {
    var config: GameConfig = GameConfig()
        private set

    var gameState: GameState? = null
        private set

    private var historySaved = false

    fun updateConfig(config: GameConfig) {
        this.config = config
    }

    fun startGame(playerNames: List<String>, state: GameState) {
        gameState = state
        historySaved = false
    }

    fun updateGameState(state: GameState) {
        gameState = state
        saveHistoryIfNeeded(state)
    }

    private fun saveHistoryIfNeeded(state: GameState) {
        if (state.phase != GamePhase.FINISHED || historySaved) return
        historySaved = true
        val repository = historySaver ?: return
        val coroutineScope = scope ?: return
        coroutineScope.launch {
            repository.saveGame(state)
        }
    }

    fun clear() {
        config = GameConfig()
        gameState = null
        historySaved = false
    }
}
