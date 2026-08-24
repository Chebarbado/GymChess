package com.rnr.gymchess

import com.rnr.gymchess.data.GameSessionHolder
import com.rnr.gymchess.domain.logic.GameEngine
import com.rnr.gymchess.domain.model.GameConfig
import com.rnr.gymchess.domain.model.GamePhase
import com.rnr.gymchess.domain.model.PlayerStatus
import com.rnr.gymchess.testutil.MainDispatcherRule
import com.rnr.gymchess.testutil.RecordingGameFeedback
import com.rnr.gymchess.ui.game.GameEvent
import com.rnr.gymchess.ui.game.GameViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun completeTurn_updatesGameStateInSession() = runTest {
        val holder = createHolderWithStartedGame()
        val viewModel = GameViewModel(holder, RecordingGameFeedback())

        viewModel.completeTurn()
        runCurrent()

        assertEquals(1, holder.gameState?.players?.get(0)?.currentStepIndex)
        assertEquals(1, holder.gameState?.activePlayerIndex)
    }

    @Test
    fun togglePause_switchesPauseFlag() = runTest {
        val holder = createHolderWithStartedGame()
        val viewModel = GameViewModel(holder, RecordingGameFeedback())

        viewModel.togglePause()
        runCurrent()
        assertTrue(holder.gameState?.isPaused == true)

        viewModel.togglePause()
        runCurrent()
        assertFalse(holder.gameState?.isPaused == true)
    }

    @Test
    fun forfeitActivePlayer_eliminatesCurrentPlayer() = runTest {
        val holder = createHolderWithStartedGame()
        val viewModel = GameViewModel(holder, RecordingGameFeedback())

        viewModel.forfeitActivePlayer()
        runCurrent()

        assertEquals(PlayerStatus.ELIMINATED, holder.gameState?.players?.get(0)?.status)
        assertEquals(1, holder.gameState?.activePlayerIndex)
    }

    @Test
    fun timerTick_emitsTimeoutFeedbackWhenTimeRunsOut() = runTest {
        val holder = GameSessionHolder()
        val config = GameConfig(maxReps = 3, timerMinutes = 1, playerCount = 2)
        holder.updateConfig(config)
        val almostTimeout = GameEngine.createGame(config, listOf("A", "B")).copy(
            players = GameEngine.createGame(config, listOf("A", "B")).players.mapIndexed { index, player ->
                if (index == 0) player.copy(remainingMs = 50) else player
            }
        )
        holder.startGame(listOf("A", "B"), almostTimeout)

        val feedback = RecordingGameFeedback()
        val viewModel = GameViewModel(holder, feedback)
        val events = mutableListOf<GameEvent>()
        val collectJob = launch {
            viewModel.events.collect { events.add(it) }
        }

        advanceTimeBy(100)
        runCurrent()

        collectJob.cancel()
        assertEquals(1, feedback.timeoutCount)
        assertEquals(1, events.size)
        assertTrue(events.first() is GameEvent.PlayerTimedOut)
    }

    private fun createHolderWithStartedGame(timerMinutes: Int = 10): GameSessionHolder {
        val holder = GameSessionHolder()
        val config = GameConfig(maxReps = 3, timerMinutes = timerMinutes, playerCount = 2)
        holder.updateConfig(config)
        val gameState = GameEngine.createGame(config, listOf("A", "B"))
        holder.startGame(listOf("A", "B"), gameState)
        return holder
    }
}
