package com.rnr.gymchess

import com.rnr.gymchess.domain.logic.GameEngine
import com.rnr.gymchess.domain.logic.LadderGenerator
import com.rnr.gymchess.domain.logic.LadderLogic
import com.rnr.gymchess.domain.model.ExerciseType
import com.rnr.gymchess.domain.model.GameConfig
import com.rnr.gymchess.domain.model.GamePhase
import com.rnr.gymchess.domain.model.LadderType
import com.rnr.gymchess.domain.model.PlayerStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    @Test
    fun createGame_initializesPlayersAndLadder() {
        val config = GameConfig(
            exerciseType = ExerciseType.DIPS,
            maxReps = 3,
            ladderType = LadderType.FAST,
            timerMinutes = 5,
            playerCount = 3
        )

        val state = GameEngine.createGame(config, listOf("A", "B", "C"))

        assertEquals(listOf(1, 2, 3), state.ladderSteps)
        assertEquals(3, state.players.size)
        assertEquals(300_000L, state.players[0].remainingMs)
        assertEquals("B", state.players[1].name)
        assertEquals(0, state.activePlayerIndex)
        assertEquals(0, state.globalStepIndex)
        assertEquals(GamePhase.PLAYING, state.phase)
    }

    @Test
    fun fastLadder_usesGlobalTurnSequence() {
        val config = GameConfig(maxReps = 3, ladderType = LadderType.FAST, playerCount = 2)
        var state = GameEngine.createGame(config, listOf("A", "B"))

        assertEquals(1, state.players[0].currentReps(state))
        state = GameEngine.completeTurn(state)
        assertEquals(1, state.globalStepIndex)
        assertEquals(1, state.players[0].currentStepIndex)
        assertEquals(2, state.players[1].currentReps(state))

        state = GameEngine.completeTurn(state)
        assertEquals(2, state.globalStepIndex)
        assertEquals(3, state.players[0].currentReps(state))

        state = GameEngine.completeTurn(state)
        assertEquals(GamePhase.FINISHED, state.phase)
        assertEquals(3, state.globalStepIndex)
    }

    @Test
    fun fastLadder_totalTurnsEqualsMaxReps() {
        val config = GameConfig(maxReps = 4, ladderType = LadderType.FAST, playerCount = 2)
        var state = GameEngine.createGame(config, listOf("A", "B"))

        repeat(4) {
            state = GameEngine.completeTurn(state)
        }

        assertEquals(GamePhase.FINISHED, state.phase)
        assertEquals(2, state.players[0].currentStepIndex)
        assertEquals(2, state.players[1].currentStepIndex)
    }

    @Test
    fun tick_eliminatesPlayerWhenTimeRunsOut() {
        val config = GameConfig(maxReps = 1, timerMinutes = 1, playerCount = 2)
        var state = GameEngine.createGame(config, listOf("A", "B"))
        state = state.copy(
            players = state.players.mapIndexed { index, player ->
                if (index == 0) player.copy(remainingMs = 50) else player
            }
        )

        state = GameEngine.tick(state, 100)

        assertEquals(PlayerStatus.ELIMINATED, state.players[0].status)
        assertEquals(1, state.activePlayerIndex)
    }

    @Test
    fun tick_doesNotDecreaseTimeWhenPaused() {
        val config = GameConfig(maxReps = 1, timerMinutes = 1, playerCount = 2)
        val state = GameEngine.createGame(config, listOf("A", "B"))
            .copy(isPaused = true)

        val updated = GameEngine.tick(state, 1000)

        assertEquals(state, updated)
    }

    @Test
    fun tick_doesNotDecreaseTimeWhenGameFinished() {
        val config = GameConfig(maxReps = 1, timerMinutes = 1, playerCount = 2)
        val state = GameEngine.createGame(config, listOf("A", "B"))
            .copy(phase = GamePhase.FINISHED)

        val updated = GameEngine.tick(state, 1000)

        assertEquals(state, updated)
    }

    @Test
    fun tick_decreasesActivePlayerTime() {
        val config = GameConfig(maxReps = 2, timerMinutes = 10, playerCount = 2)
        val state = GameEngine.createGame(config, listOf("A", "B"))

        val updated = GameEngine.tick(state, 500)

        assertEquals(599_500L, updated.players[0].remainingMs)
        assertEquals(PlayerStatus.ACTIVE, updated.players[0].status)
    }

    @Test
    fun completeTurn_advancesToNextPlayer_onFastLadder() {
        val config = GameConfig(maxReps = 3, ladderType = LadderType.FAST, playerCount = 2)
        val state = GameEngine.createGame(config, listOf("A", "B"))

        val updated = GameEngine.completeTurn(state)

        assertEquals(1, updated.players[0].currentStepIndex)
        assertEquals(1, updated.globalStepIndex)
        assertEquals(1, updated.activePlayerIndex)
        assertEquals(PlayerStatus.ACTIVE, updated.players[0].status)
    }

    @Test
    fun completeTurn_finishesPersonalLadderAndDeterminesWinner() {
        val config = GameConfig(
            maxReps = 1,
            ladderType = LadderType.UP_ONLY,
            timerMinutes = 10,
            playerCount = 2
        )
        var state = GameEngine.createGame(config, listOf("A", "B"))

        state = GameEngine.completeTurn(state)

        assertEquals(PlayerStatus.FINISHED, state.players[0].status)
        assertEquals(GamePhase.PLAYING, state.phase)

        state = GameEngine.completeTurn(state)

        assertEquals(GamePhase.FINISHED, state.phase)
        assertEquals(0, state.winnerId)
    }

    @Test
    fun completeTurn_doesNothingWhenPaused() {
        val config = GameConfig(maxReps = 2, playerCount = 2)
        val state = GameEngine.createGame(config, listOf("A", "B")).copy(isPaused = true)

        val updated = GameEngine.completeTurn(state)

        assertEquals(state, updated)
    }

    @Test
    fun togglePause_switchesPauseFlag() {
        val config = GameConfig(maxReps = 2, playerCount = 2)
        val state = GameEngine.createGame(config, listOf("A", "B"))

        val paused = GameEngine.togglePause(state)
        val resumed = GameEngine.togglePause(paused)

        assertTrue(paused.isPaused)
        assertFalse(resumed.isPaused)
    }

    @Test
    fun togglePause_doesNothingWhenGameFinished() {
        val config = GameConfig(maxReps = 1, playerCount = 2)
        val state = GameEngine.createGame(config, listOf("A", "B"))
            .copy(phase = GamePhase.FINISHED)

        assertEquals(state, GameEngine.togglePause(state))
    }

    @Test
    fun forfeitActivePlayer_eliminatesPlayerAndPassesTurn() {
        val config = GameConfig(maxReps = 3, playerCount = 3)
        val state = GameEngine.createGame(config, listOf("A", "B", "C"))

        val updated = GameEngine.forfeitActivePlayer(state)

        assertEquals(PlayerStatus.ELIMINATED, updated.players[0].status)
        assertEquals(0L, updated.players[0].remainingMs)
        assertEquals(1, updated.activePlayerIndex)
    }

    @Test
    fun determineWinner_picksFinishedPlayerWithMostTime() {
        val config = GameConfig(maxReps = 1, playerCount = 3)
        var state = GameEngine.createGame(config, listOf("A", "B", "C"))
        state = state.copy(
            players = listOf(
                state.players[0].copy(status = PlayerStatus.FINISHED, remainingMs = 100_000),
                state.players[1].copy(status = PlayerStatus.FINISHED, remainingMs = 250_000),
                state.players[2].copy(status = PlayerStatus.ELIMINATED, remainingMs = 0)
            )
        )

        assertEquals(1, GameEngine.determineWinner(state.players))
    }

    @Test
    fun determineWinner_returnsNullWhenNobodyFinished() {
        val config = GameConfig(maxReps = 2, timerMinutes = 1, playerCount = 2)
        var state = GameEngine.createGame(config, listOf("A", "B"))
        state = state.copy(
            players = state.players.map { it.copy(status = PlayerStatus.ELIMINATED) }
        )

        assertNull(GameEngine.determineWinner(state.players))
    }

    @Test
    fun slowLadder_generatesCorrectSequence() {
        assertEquals(listOf(1, 1, 2, 2, 3, 3), LadderGenerator.generate(3, LadderType.SLOW))
    }

    @Test
    fun fastLadder_generatesCorrectSequence() {
        assertEquals(listOf(1, 2, 3), LadderGenerator.generate(3, LadderType.FAST))
    }

    @Test
    fun upOnlyLadder_generatesCorrectSequence() {
        assertEquals(listOf(1, 2, 3), LadderGenerator.generate(3, LadderType.UP_ONLY))
    }

    @Test
    fun upDownLadder_generatesCorrectSequence() {
        assertEquals(listOf(1, 2, 3, 2, 1), LadderGenerator.generate(3, LadderType.UP_DOWN))
    }

    @Test
    fun upDownLadder_forMaxOne_isSingleStep() {
        assertEquals(listOf(1), LadderGenerator.generate(1, LadderType.UP_DOWN))
    }

    @Test
    fun fastLadder_distributesTurnsBetweenPlayers() {
        assertEquals(2, LadderLogic.turnsForPlayer(totalGlobalSteps = 4, playerCount = 2, playerId = 0))
        assertEquals(2, LadderLogic.turnsForPlayer(totalGlobalSteps = 4, playerCount = 2, playerId = 1))
        assertEquals(2, LadderLogic.turnsForPlayer(totalGlobalSteps = 4, playerCount = 3, playerId = 0))
    }
}
