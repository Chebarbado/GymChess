package com.rnr.gymchess

import com.rnr.gymchess.data.GameSessionHolder
import com.rnr.gymchess.domain.model.ExerciseType
import com.rnr.gymchess.domain.model.GameConfig
import com.rnr.gymchess.domain.model.LadderType
import com.rnr.gymchess.ui.setup.SetupUiState
import com.rnr.gymchess.ui.setup.SetupViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupViewModelTest {

    @Test
    fun init_restoresSavedConfig() {
        val holder = GameSessionHolder()
        holder.updateConfig(
            GameConfig(
                exerciseType = ExerciseType.DIPS,
                maxReps = 12,
                ladderType = LadderType.SLOW,
                timerMinutes = 8,
                playerCount = 3
            )
        )

        val viewModel = SetupViewModel(holder)
        val state = viewModel.uiState.value

        assertEquals(ExerciseType.DIPS, state.exerciseType)
        assertEquals(12, state.maxReps)
        assertEquals(LadderType.SLOW, state.ladderType)
        assertEquals(8, state.timerMinutes)
        assertEquals(3, state.playerCount)
    }

    @Test
    fun setters_clampValuesToAllowedRanges() {
        val viewModel = SetupViewModel(GameSessionHolder())

        viewModel.setMaxReps(999)
        viewModel.setTimerMinutes(0)
        viewModel.setPlayerCount(10)

        val state = viewModel.uiState.value
        assertEquals(50, state.maxReps)
        assertEquals(1, state.timerMinutes)
        assertEquals(4, state.playerCount)
    }

    @Test
    fun saveAndContinue_persistsValidConfig() {
        val holder = GameSessionHolder()
        val viewModel = SetupViewModel(holder)
        viewModel.setExerciseType(ExerciseType.DIPS)
        viewModel.setMaxReps(7)
        viewModel.setLadderType(LadderType.SLOW)
        viewModel.setTimerMinutes(12)
        viewModel.setPlayerCount(4)

        assertTrue(viewModel.saveAndContinue())
        assertEquals(ExerciseType.DIPS, holder.config.exerciseType)
        assertEquals(7, holder.config.maxReps)
        assertEquals(LadderType.SLOW, holder.config.ladderType)
        assertEquals(12, holder.config.timerMinutes)
        assertEquals(4, holder.config.playerCount)
    }

    @Test
    fun setupUiState_isInvalidOutsideRanges() {
        assertFalse(SetupUiState(maxReps = 0).isValid)
        assertFalse(SetupUiState(timerMinutes = 61).isValid)
        assertFalse(SetupUiState(playerCount = 1).isValid)
        assertTrue(SetupUiState().isValid)
    }
}
