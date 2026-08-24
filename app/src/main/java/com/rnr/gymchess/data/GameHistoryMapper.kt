package com.rnr.gymchess.data

import com.rnr.gymchess.domain.model.GameHistoryPlayerRecord
import com.rnr.gymchess.domain.model.GameHistoryRecord
import com.rnr.gymchess.domain.model.GameState
import com.rnr.gymchess.domain.model.PlayerStatus
import java.util.UUID

object GameHistoryMapper {

    fun fromGameState(state: GameState): GameHistoryRecord {
        return GameHistoryRecord(
            id = UUID.randomUUID().toString(),
            playedAtEpochMs = System.currentTimeMillis(),
            exerciseType = state.config.exerciseType,
            ladderType = state.config.ladderType,
            maxReps = state.config.maxReps,
            timerMinutes = state.config.timerMinutes,
            winnerName = state.winner?.name,
            players = state.players.map { player ->
                GameHistoryPlayerRecord(
                    name = player.name,
                    status = player.status,
                    remainingMs = player.remainingMs,
                    progress = player.progress
                )
            }
        )
    }
}
