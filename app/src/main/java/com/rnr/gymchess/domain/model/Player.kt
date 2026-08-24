package com.rnr.gymchess.domain.model

import com.rnr.gymchess.domain.logic.LadderLogic

data class Player(
    val id: Int,
    val name: String,
    val remainingMs: Long,
    val currentStepIndex: Int = 0,
    val status: PlayerStatus = PlayerStatus.ACTIVE
) {
    fun currentReps(state: GameState): Int? = LadderLogic.currentReps(state, this)

    fun totalSteps(state: GameState): Int = LadderLogic.totalStepsForPlayer(state, this)

    val progress: Int get() = currentStepIndex
}
