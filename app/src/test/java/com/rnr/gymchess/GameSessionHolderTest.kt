package com.rnr.gymchess

import com.rnr.gymchess.data.GameSessionHolder
import com.rnr.gymchess.domain.logic.GameEngine
import com.rnr.gymchess.domain.model.GameConfig
import com.rnr.gymchess.domain.model.GamePhase
import com.rnr.gymchess.testutil.RecordingGameHistorySaver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameSessionHolderTest {

    @Test
    fun updateConfig_storesConfiguration() {
        val holder = GameSessionHolder()
        val config = GameConfig(maxReps = 15, playerCount = 4)

        holder.updateConfig(config)

        assertEquals(config, holder.config)
    }

    @Test
    fun startGame_resetsHistorySavedFlag() = runTest {
        val saver = RecordingGameHistorySaver()
        val holder = GameSessionHolder(historySaver = saver, scope = this)
        val config = GameConfig(maxReps = 1, playerCount = 2)
        holder.updateConfig(config)

        val firstGame = GameEngine.createGame(config, listOf("A", "B"))
        holder.startGame(listOf("A", "B"), firstGame)
        holder.updateGameState(firstGame.copy(phase = GamePhase.FINISHED, winnerId = 0))
        runCurrent()
        assertEquals(1, saver.savedStates.size)

        val secondGame = GameEngine.createGame(config, listOf("A", "B"))
        holder.startGame(listOf("A", "B"), secondGame)
        holder.updateGameState(secondGame.copy(phase = GamePhase.FINISHED, winnerId = 1))
        runCurrent()

        assertEquals(2, saver.savedStates.size)
    }

    @Test
    fun updateGameState_savesHistoryOnlyOnce() = runTest {
        val saver = RecordingGameHistorySaver()
        val holder = GameSessionHolder(historySaver = saver, scope = this)
        val config = GameConfig(maxReps = 1, playerCount = 2)
        holder.updateConfig(config)
        val game = GameEngine.createGame(config, listOf("A", "B"))
        holder.startGame(listOf("A", "B"), game)

        val finished = game.copy(phase = GamePhase.FINISHED, winnerId = 0)
        holder.updateGameState(finished)
        holder.updateGameState(finished)
        runCurrent()

        assertEquals(1, saver.savedStates.size)
    }

    @Test
    fun clear_resetsSession() {
        val holder = GameSessionHolder()
        holder.updateConfig(GameConfig(maxReps = 20))
        holder.startGame(listOf("A"), GameEngine.createGame(GameConfig(), listOf("A")))

        holder.clear()

        assertEquals(GameConfig(), holder.config)
        assertNull(holder.gameState)
    }
}
