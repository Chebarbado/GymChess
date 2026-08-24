package com.rnr.gymchess.domain.model

data class GameHistoryRecord(
    val id: String,
    val playedAtEpochMs: Long,
    val exerciseType: ExerciseType,
    val ladderType: LadderType,
    val maxReps: Int,
    val timerMinutes: Int,
    val winnerName: String?,
    val players: List<GameHistoryPlayerRecord>
)

data class GameHistoryPlayerRecord(
    val name: String,
    val status: PlayerStatus,
    val remainingMs: Long,
    val progress: Int
)
