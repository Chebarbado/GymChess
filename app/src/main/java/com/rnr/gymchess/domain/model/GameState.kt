package com.rnr.gymchess.domain.model

data class GameState(
    val config: GameConfig,
    val ladderSteps: List<Int>,
    val players: List<Player>,
    val activePlayerIndex: Int = 0,
    val globalStepIndex: Int = 0,
    val isPaused: Boolean = false,
    val phase: GamePhase = GamePhase.PLAYING,
    val winnerId: Int? = null
) {
    val activePlayer: Player? get() = players.getOrNull(activePlayerIndex)

    val isGameOver: Boolean get() = phase == GamePhase.FINISHED

    val winner: Player? get() = winnerId?.let { id -> players.find { it.id == id } }
}
