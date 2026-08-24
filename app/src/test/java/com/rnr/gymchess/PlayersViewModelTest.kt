package com.rnr.gymchess

import com.rnr.gymchess.data.GameSessionHolder
import com.rnr.gymchess.domain.model.GameConfig
import com.rnr.gymchess.domain.model.GamePhase
import com.rnr.gymchess.ui.players.PlayersUiState
import com.rnr.gymchess.ui.players.PlayersViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayersViewModelTest {

    @Test
    fun init_generatesDefaultNamesForConfiguredPlayerCount() {
        val holder = GameSessionHolder()
        holder.updateConfig(GameConfig(playerCount = 3))

        val viewModel = PlayersViewModel(holder)

        assertEquals(listOf("Игрок-1", "Игрок-2", "Игрок-3"), viewModel.uiState.value.playerNames)
    }

    @Test
    fun updateName_changesOnlySelectedPlayer() {
        val holder = GameSessionHolder()
        holder.updateConfig(GameConfig(playerCount = 2))
        val viewModel = PlayersViewModel(holder)

        viewModel.updateName(1, "Борис")

        assertEquals(listOf("Игрок-1", "Борис"), viewModel.uiState.value.playerNames)
    }

    @Test
    fun startGame_returnsNullWhenNamesAreBlank() {
        val holder = GameSessionHolder()
        holder.updateConfig(GameConfig(playerCount = 2))
        val viewModel = PlayersViewModel(holder)
        viewModel.updateName(0, "   ")

        assertNull(viewModel.startGame())
        assertNull(holder.gameState)
    }

    @Test
    fun startGame_createsGameInSessionHolder() {
        val holder = GameSessionHolder()
        holder.updateConfig(GameConfig(maxReps = 2, playerCount = 2))
        val viewModel = PlayersViewModel(holder)
        viewModel.updateName(0, "Anna")
        viewModel.updateName(1, "Bob")

        val gameState = viewModel.startGame()

        assertNotNull(gameState)
        assertEquals(GamePhase.PLAYING, gameState?.phase)
        assertEquals(listOf("Anna", "Bob"), holder.gameState?.players?.map { it.name })
    }

    @Test
    fun playersUiState_isInvalidWhenAnyNameBlank() {
        assertFalse(PlayersUiState(playerNames = listOf("A", "  ")).isValid)
        assertTrue(PlayersUiState(playerNames = listOf("A", "B")).isValid)
    }
}
