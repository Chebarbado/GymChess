package com.rnr.gymchess

import com.rnr.gymchess.data.GameHistoryRepository
import com.rnr.gymchess.domain.model.ExerciseType
import com.rnr.gymchess.domain.model.GameHistoryPlayerRecord
import com.rnr.gymchess.domain.model.GameHistoryRecord
import com.rnr.gymchess.domain.model.LadderType
import com.rnr.gymchess.domain.model.PlayerStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameHistoryRepositoryTest {

    @Test
    fun encodeAndParseHistory_roundTrip() {
        val record = sampleRecord()

        val json = GameHistoryRepository.encodeHistory(listOf(record))
        val parsed = GameHistoryRepository.parseHistory(json)

        assertEquals(1, parsed.size)
        assertEquals(record, parsed.first())
    }

    @Test
    fun parseHistory_handlesMissingWinner() {
        val json = """
            [{
              "id":"1",
              "playedAtEpochMs":100,
              "exerciseType":"DIPS",
              "ladderType":"SLOW",
              "maxReps":5,
              "timerMinutes":3,
              "winnerName":null,
              "players":[]
            }]
        """.trimIndent()

        val parsed = GameHistoryRepository.parseHistory(json)

        assertEquals(1, parsed.size)
        assertNull(parsed.first().winnerName)
        assertEquals(ExerciseType.DIPS, parsed.first().exerciseType)
    }

    @Test
    fun parseHistory_returnsEmptyListForBlankInput() {
        assertTrue(GameHistoryRepository.parseHistory("").isEmpty())
    }

    @Test
    fun parseHistory_returnsEmptyListForInvalidJson() {
        assertTrue(GameHistoryRepository.parseHistory("{not-json").isEmpty())
    }

    @Test
    fun encodeHistory_keepsMultipleRecords() {
        val first = sampleRecord(id = "1")
        val second = sampleRecord(id = "2", winnerName = null)

        val parsed = GameHistoryRepository.parseHistory(
            GameHistoryRepository.encodeHistory(listOf(first, second))
        )

        assertEquals(2, parsed.size)
        assertEquals("1", parsed[0].id)
        assertEquals("2", parsed[1].id)
    }

    private fun sampleRecord(
        id: String = "test-id",
        winnerName: String? = "Игрок-1"
    ) = GameHistoryRecord(
        id = id,
        playedAtEpochMs = 1_700_000_000_000L,
        exerciseType = ExerciseType.PULL_UPS,
        ladderType = LadderType.FAST,
        maxReps = 10,
        timerMinutes = 5,
        winnerName = winnerName,
        players = listOf(
            GameHistoryPlayerRecord(
                name = "Игрок-1",
                status = PlayerStatus.FINISHED,
                remainingMs = 120_000,
                progress = 10
            ),
            GameHistoryPlayerRecord(
                name = "Игрок-2",
                status = PlayerStatus.ELIMINATED,
                remainingMs = 0,
                progress = 3
            )
        )
    )
}
