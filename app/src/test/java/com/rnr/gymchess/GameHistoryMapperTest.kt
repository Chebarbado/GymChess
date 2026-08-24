package com.rnr.gymchess

import com.rnr.gymchess.data.GameHistoryMapper
import com.rnr.gymchess.domain.logic.GameEngine
import com.rnr.gymchess.domain.model.ExerciseType
import com.rnr.gymchess.domain.model.GameConfig
import com.rnr.gymchess.domain.model.LadderType
import com.rnr.gymchess.domain.model.PlayerStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class GameHistoryMapperTest {

    @Test
    fun fromGameState_mapsConfigPlayersAndWinner() {
        val config = GameConfig(
            exerciseType = ExerciseType.DIPS,
            maxReps = 1,
            ladderType = LadderType.UP_ONLY,
            timerMinutes = 7,
            playerCount = 2
        )
        var state = GameEngine.createGame(config, listOf("Alpha", "Beta"))
        state = GameEngine.completeTurn(state)
        state = GameEngine.completeTurn(state)

        val record = GameHistoryMapper.fromGameState(state)

        assertEquals(ExerciseType.DIPS, record.exerciseType)
        assertEquals(LadderType.UP_ONLY, record.ladderType)
        assertEquals(1, record.maxReps)
        assertEquals(7, record.timerMinutes)
        assertNotNull(record.id)
        assertNotNull(record.winnerName)
        assertEquals("Alpha", record.winnerName)
        assertEquals(2, record.players.size)
        assertEquals(2, record.players.count { it.status == PlayerStatus.FINISHED })
    }
}
