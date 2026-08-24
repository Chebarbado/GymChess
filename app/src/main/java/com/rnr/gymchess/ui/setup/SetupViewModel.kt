package com.rnr.gymchess.ui.setup

import androidx.lifecycle.ViewModel
import com.rnr.gymchess.data.GameSessionHolder
import com.rnr.gymchess.domain.model.ExerciseType
import com.rnr.gymchess.domain.model.GameConfig
import com.rnr.gymchess.domain.model.LadderType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SetupUiState(
    val exerciseType: ExerciseType = ExerciseType.PULL_UPS,
    val maxReps: Int = 10,
    val ladderType: LadderType = LadderType.FAST,
    val timerMinutes: Int = 10,
    val playerCount: Int = 2
) {
    val isValid: Boolean
        get() = maxReps in 1..50 && timerMinutes in 1..60 && playerCount in 2..4
}

class SetupViewModel(
    private val sessionHolder: GameSessionHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    init {
        val saved = sessionHolder.config
        _uiState.value = SetupUiState(
            exerciseType = saved.exerciseType,
            maxReps = saved.maxReps,
            ladderType = saved.ladderType,
            timerMinutes = saved.timerMinutes,
            playerCount = saved.playerCount
        )
    }

    fun setExerciseType(type: ExerciseType) {
        _uiState.update { it.copy(exerciseType = type) }
    }

    fun setMaxReps(value: Int) {
        _uiState.update { it.copy(maxReps = value.coerceIn(1, 50)) }
    }

    fun setLadderType(type: LadderType) {
        _uiState.update { it.copy(ladderType = type) }
    }

    fun setTimerMinutes(value: Int) {
        _uiState.update { it.copy(timerMinutes = value.coerceIn(1, 60)) }
    }

    fun setPlayerCount(count: Int) {
        _uiState.update { it.copy(playerCount = count.coerceIn(2, 4)) }
    }

    fun saveAndContinue(): Boolean {
        val state = _uiState.value
        if (!state.isValid) return false

        val config = GameConfig(
            exerciseType = state.exerciseType,
            maxReps = state.maxReps,
            ladderType = state.ladderType,
            timerMinutes = state.timerMinutes,
            playerCount = state.playerCount
        )
        sessionHolder.updateConfig(config)
        return true
    }
}
