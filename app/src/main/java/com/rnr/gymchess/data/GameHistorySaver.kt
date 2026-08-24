package com.rnr.gymchess.data

import com.rnr.gymchess.domain.model.GameState

fun interface GameHistorySaver {
    suspend fun saveGame(state: GameState)
}
