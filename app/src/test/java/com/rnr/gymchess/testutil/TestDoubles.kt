package com.rnr.gymchess.testutil

import com.rnr.gymchess.data.GameHistorySaver
import com.rnr.gymchess.domain.model.GameState
import com.rnr.gymchess.util.GameFeedback

class RecordingGameFeedback : GameFeedback {
    var timeoutCount: Int = 0
        private set

    override fun onPlayerTimedOut() {
        timeoutCount++
    }
}

class RecordingGameHistorySaver : GameHistorySaver {
    val savedStates = mutableListOf<GameState>()

    override suspend fun saveGame(state: GameState) {
        savedStates.add(state)
    }
}
