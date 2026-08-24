package com.rnr.gymchess.domain.logic

import com.rnr.gymchess.domain.model.GameConfig
import com.rnr.gymchess.domain.model.GamePhase
import com.rnr.gymchess.domain.model.GameState
import com.rnr.gymchess.domain.model.Player
import com.rnr.gymchess.domain.model.PlayerStatus
import com.rnr.gymchess.domain.model.isGlobalLadder

object GameEngine {

    fun createGame(config: GameConfig, playerNames: List<String>): GameState {
        val ladderSteps = LadderGenerator.generate(config.maxReps, config.ladderType)
        val initialMs = config.timerMinutes * 60_000L
        val players = playerNames.mapIndexed { index, name ->
            Player(
                id = index,
                name = name,
                remainingMs = initialMs
            )
        }
        return GameState(
            config = config,
            ladderSteps = ladderSteps,
            players = players,
            activePlayerIndex = 0,
            globalStepIndex = 0
        )
    }

    fun tick(state: GameState, elapsedMs: Long): GameState {
        if (state.phase != GamePhase.PLAYING || state.isPaused) return state

        val activeIndex = state.activePlayerIndex
        val activePlayer = state.players.getOrNull(activeIndex) ?: return state
        if (activePlayer.status != PlayerStatus.ACTIVE) return state

        val newRemaining = (activePlayer.remainingMs - elapsedMs).coerceAtLeast(0)
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[activeIndex] = activePlayer.copy(remainingMs = newRemaining)

        if (newRemaining == 0L) {
            updatedPlayers[activeIndex] = updatedPlayers[activeIndex].copy(
                status = PlayerStatus.ELIMINATED
            )
            return finalizeOrContinue(state, updatedPlayers, activeIndex)
        }

        return state.copy(players = updatedPlayers)
    }

    fun completeTurn(state: GameState): GameState {
        if (state.phase != GamePhase.PLAYING || state.isPaused) return state

        val activeIndex = state.activePlayerIndex
        val activePlayer = state.players.getOrNull(activeIndex) ?: return state
        if (activePlayer.status != PlayerStatus.ACTIVE) return state

        return if (state.config.ladderType.isGlobalLadder()) {
            completeGlobalTurn(state, activeIndex, activePlayer)
        } else {
            completePersonalTurn(state, activeIndex, activePlayer)
        }
    }

    private fun completeGlobalTurn(
        state: GameState,
        activeIndex: Int,
        activePlayer: Player
    ): GameState {
        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[activeIndex] = activePlayer.copy(
            currentStepIndex = activePlayer.currentStepIndex + 1
        )

        val nextGlobalStep = state.globalStepIndex + 1
        val updatedState = state.copy(
            players = updatedPlayers,
            globalStepIndex = nextGlobalStep
        )

        if (nextGlobalStep >= state.ladderSteps.size) {
            val finishedPlayers = markAllNonEliminatedFinished(updatedPlayers)
            return updatedState.copy(
                players = finishedPlayers,
                phase = GamePhase.FINISHED,
                winnerId = determineWinner(finishedPlayers)
            )
        }

        return finalizeOrContinue(updatedState, updatedPlayers, activeIndex)
    }

    private fun completePersonalTurn(
        state: GameState,
        activeIndex: Int,
        activePlayer: Player
    ): GameState {
        val nextStepIndex = activePlayer.currentStepIndex + 1
        val updatedPlayers = state.players.toMutableList()

        if (nextStepIndex >= state.ladderSteps.size) {
            updatedPlayers[activeIndex] = activePlayer.copy(
                currentStepIndex = nextStepIndex,
                status = PlayerStatus.FINISHED
            )
        } else {
            updatedPlayers[activeIndex] = activePlayer.copy(currentStepIndex = nextStepIndex)
        }

        return finalizeOrContinue(state, updatedPlayers, activeIndex)
    }

    fun togglePause(state: GameState): GameState {
        if (state.phase != GamePhase.PLAYING) return state
        return state.copy(isPaused = !state.isPaused)
    }

    fun forfeitActivePlayer(state: GameState): GameState {
        if (state.phase != GamePhase.PLAYING) return state

        val activeIndex = state.activePlayerIndex
        val activePlayer = state.players.getOrNull(activeIndex) ?: return state
        if (activePlayer.status != PlayerStatus.ACTIVE) return state

        val updatedPlayers = state.players.toMutableList()
        updatedPlayers[activeIndex] = activePlayer.copy(
            remainingMs = 0,
            status = PlayerStatus.ELIMINATED
        )
        return finalizeOrContinue(state, updatedPlayers, activeIndex)
    }

    private fun markAllNonEliminatedFinished(players: List<Player>): List<Player> {
        return players.map { player ->
            if (player.status == PlayerStatus.ACTIVE) {
                player.copy(status = PlayerStatus.FINISHED)
            } else {
                player
            }
        }
    }

    private fun finalizeOrContinue(
        state: GameState,
        players: List<Player>,
        fromIndex: Int
    ): GameState {
        val activePlayers = players.filter { it.status == PlayerStatus.ACTIVE }
        if (activePlayers.isEmpty()) {
            return state.copy(
                players = players,
                phase = GamePhase.FINISHED,
                winnerId = determineWinner(players)
            )
        }

        val nextIndex = findNextActiveIndex(players, fromIndex)
        return state.copy(
            players = players,
            activePlayerIndex = nextIndex
        )
    }

    private fun findNextActiveIndex(players: List<Player>, fromIndex: Int): Int {
        val size = players.size
        for (offset in 1..size) {
            val index = (fromIndex + offset) % size
            if (players[index].status == PlayerStatus.ACTIVE) return index
        }
        return fromIndex
    }

    fun determineWinner(players: List<Player>): Int? {
        val finished = players.filter { it.status == PlayerStatus.FINISHED }
        if (finished.isEmpty()) return null
        return finished.maxByOrNull { it.remainingMs }?.id
    }
}
