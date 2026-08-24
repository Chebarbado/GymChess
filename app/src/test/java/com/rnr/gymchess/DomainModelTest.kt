package com.rnr.gymchess

import com.rnr.gymchess.domain.logic.GameEngine
import com.rnr.gymchess.domain.model.GameConfig
import com.rnr.gymchess.domain.model.GamePhase
import com.rnr.gymchess.domain.model.LadderType
import com.rnr.gymchess.domain.model.PlayerStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainModelTest {

    @Test
    fun player_currentReps_returnsStepValue_forPersonalLadder() {
        val config = GameConfig(maxReps = 3, ladderType = LadderType.UP_ONLY, playerCount = 1)
        val state = GameEngine.createGame(config, listOf("A"))
        val player = state.players.first()

        assertEquals(1, player.currentReps(state))
        assertEquals(0, player.progress)
    }

    @Test
    fun player_currentReps_returnsNullWhenFinished() {
        val config = GameConfig(maxReps = 1, ladderType = LadderType.UP_ONLY, playerCount = 1)
        var state = GameEngine.createGame(config, listOf("A"))
        state = GameEngine.completeTurn(state)
        val player = state.players.first()

        assertNull(player.currentReps(state))
        assertEquals(PlayerStatus.FINISHED, player.status)
    }

    @Test
    fun gameState_exposesActivePlayerAndWinner_onFastLadder() {
        val config = GameConfig(maxReps = 1, ladderType = LadderType.FAST, playerCount = 2)
        var state = GameEngine.createGame(config, listOf("A", "B"))

        assertEquals("A", state.activePlayer?.name)
        assertFalse(state.isGameOver)

        state = GameEngine.completeTurn(state)

        assertEquals(GamePhase.FINISHED, state.phase)
        assertTrue(state.isGameOver)
        assertEquals("A", state.winner?.name)
    }
}
