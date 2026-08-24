package com.rnr.gymchess.domain.model

data class GameConfig(
    val exerciseType: ExerciseType = ExerciseType.PULL_UPS,
    val maxReps: Int = 10,
    val ladderType: LadderType = LadderType.FAST,
    val timerMinutes: Int = 10,
    val playerCount: Int = 2
)
