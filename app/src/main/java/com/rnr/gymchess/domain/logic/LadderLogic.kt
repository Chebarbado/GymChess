package com.rnr.gymchess.domain.logic

import com.rnr.gymchess.domain.model.GameState
import com.rnr.gymchess.domain.model.Player
import com.rnr.gymchess.domain.model.PlayerStatus
import com.rnr.gymchess.domain.model.isGlobalLadder

object LadderLogic {

    fun currentReps(state: GameState, player: Player): Int? {
        if (player.status != PlayerStatus.ACTIVE) return null
        return if (state.config.ladderType.isGlobalLadder()) {
            state.ladderSteps.getOrNull(state.globalStepIndex)
        } else {
            state.ladderSteps.getOrNull(player.currentStepIndex)
        }
    }

    fun totalStepsForPlayer(state: GameState, player: Player): Int {
        return if (state.config.ladderType.isGlobalLadder()) {
            turnsForPlayer(
                totalGlobalSteps = state.ladderSteps.size,
                playerCount = state.players.size,
                playerId = player.id
            )
        } else {
            state.ladderSteps.size
        }
    }

    fun turnsForPlayer(totalGlobalSteps: Int, playerCount: Int, playerId: Int): Int {
        if (playerCount <= 0) return 0
        return (0 until totalGlobalSteps).count { turnIndex -> turnIndex % playerCount == playerId }
    }
}
