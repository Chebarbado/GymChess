package com.rnr.gymchess.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnr.gymchess.data.GameSessionHolder
import com.rnr.gymchess.domain.logic.GameEngine
import com.rnr.gymchess.domain.model.GamePhase
import com.rnr.gymchess.domain.model.GameState
import com.rnr.gymchess.domain.model.PlayerStatus
import com.rnr.gymchess.util.GameFeedback
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(
    private val sessionHolder: GameSessionHolder,
    private val feedbackManager: GameFeedback
) : ViewModel() {

    private val _gameState = MutableStateFlow<GameState?>(sessionHolder.gameState)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>()
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private var timerJob: Job? = null

    init {
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(TICK_MS)
                val current = _gameState.value ?: return@launch
                if (current.phase == GamePhase.FINISHED) return@launch
                val updated = GameEngine.tick(current, TICK_MS)
                applyStateUpdate(current, updated, emitTimeoutEvent = true)
                if (updated.phase == GamePhase.FINISHED) return@launch
            }
        }
    }

    fun completeTurn() {
        val current = _gameState.value ?: return
        val updated = GameEngine.completeTurn(current)
        applyStateUpdate(current, updated, emitTimeoutEvent = false)
    }

    fun togglePause() {
        val current = _gameState.value ?: return
        val updated = GameEngine.togglePause(current)
        applyStateUpdate(current, updated, emitTimeoutEvent = false)
    }

    fun forfeitActivePlayer() {
        val current = _gameState.value ?: return
        val updated = GameEngine.forfeitActivePlayer(current)
        applyStateUpdate(current, updated, emitTimeoutEvent = false)
    }

    private fun applyStateUpdate(
        previous: GameState,
        updated: GameState,
        emitTimeoutEvent: Boolean
    ) {
        _gameState.value = updated
        sessionHolder.updateGameState(updated)

        if (emitTimeoutEvent) {
            previous.players.zip(updated.players).forEach { (prevPlayer, nextPlayer) ->
                if (
                    prevPlayer.status == PlayerStatus.ACTIVE &&
                    nextPlayer.status == PlayerStatus.ELIMINATED
                ) {
                    viewModelScope.launch {
                        _events.emit(GameEvent.PlayerTimedOut(nextPlayer))
                        feedbackManager.onPlayerTimedOut()
                    }
                }
            }
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    companion object {
        private const val TICK_MS = 100L
    }
}
