package com.rnr.gymchess.ui.players

import androidx.lifecycle.ViewModel
import com.rnr.gymchess.data.GameSessionHolder
import com.rnr.gymchess.domain.logic.GameEngine
import com.rnr.gymchess.domain.model.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PlayersUiState(
    val playerNames: List<String> = emptyList()
) {
    val isValid: Boolean get() = playerNames.all { it.isNotBlank() }
}

class PlayersViewModel(
    private val sessionHolder: GameSessionHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayersUiState())
    val uiState: StateFlow<PlayersUiState> = _uiState.asStateFlow()

    init {
        val count = sessionHolder.config.playerCount
        _uiState.value = PlayersUiState(
            playerNames = List(count) { index -> "Игрок-${index + 1}" }
        )
    }

    fun updateName(index: Int, name: String) {
        _uiState.update { state ->
            val names = state.playerNames.toMutableList()
            if (index in names.indices) {
                names[index] = name
            }
            state.copy(playerNames = names)
        }
    }

    fun startGame(): GameState? {
        val state = _uiState.value
        if (!state.isValid) return null

        val gameState = GameEngine.createGame(sessionHolder.config, state.playerNames)
        sessionHolder.startGame(state.playerNames, gameState)
        return gameState
    }
}
