package com.rnr.gymchess.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rnr.gymchess.domain.model.ExerciseType
import com.rnr.gymchess.domain.model.GameHistoryPlayerRecord
import com.rnr.gymchess.domain.model.GameHistoryRecord
import com.rnr.gymchess.domain.model.GameState
import com.rnr.gymchess.domain.model.LadderType
import com.rnr.gymchess.domain.model.PlayerStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.historyDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "game_history"
)

class GameHistoryRepository(context: Context) : GameHistorySaver {

    private val dataStore = context.applicationContext.historyDataStore

    val history: Flow<List<GameHistoryRecord>> = dataStore.data.map { preferences ->
        parseHistory(preferences[HISTORY_KEY] ?: "[]")
    }

    override suspend fun saveGame(state: GameState) {
        dataStore.edit { preferences ->
            val current = parseHistory(preferences[HISTORY_KEY] ?: "[]")
            val updated = listOf(GameHistoryMapper.fromGameState(state)) + current
            preferences[HISTORY_KEY] = encodeHistory(updated.take(MAX_HISTORY_ENTRIES))
        }
    }

    suspend fun clearHistory() {
        dataStore.edit { preferences ->
            preferences[HISTORY_KEY] = "[]"
        }
    }

    companion object {
        private val HISTORY_KEY = stringPreferencesKey("history_json")
        private const val MAX_HISTORY_ENTRIES = 50

        fun encodeHistory(records: List<GameHistoryRecord>): String {
            val array = JSONArray()
            records.forEach { record ->
                array.put(record.toJson())
            }
            return array.toString()
        }

        fun parseHistory(json: String): List<GameHistoryRecord> {
            if (json.isBlank()) return emptyList()
            return try {
                val array = JSONArray(json)
                buildList {
                    for (index in 0 until array.length()) {
                        add(array.getJSONObject(index).toRecord())
                    }
                }
            } catch (_: Exception) {
                emptyList()
            }
        }

        private fun GameHistoryRecord.toJson(): JSONObject {
            val playersArray = JSONArray()
            players.forEach { player ->
                playersArray.put(
                    JSONObject()
                        .put("name", player.name)
                        .put("status", player.status.name)
                        .put("remainingMs", player.remainingMs)
                        .put("progress", player.progress)
                )
            }
            return JSONObject()
                .put("id", id)
                .put("playedAtEpochMs", playedAtEpochMs)
                .put("exerciseType", exerciseType.name)
                .put("ladderType", ladderType.name)
                .put("maxReps", maxReps)
                .put("timerMinutes", timerMinutes)
                .apply {
                    if (winnerName == null) {
                        put("winnerName", JSONObject.NULL)
                    } else {
                        put("winnerName", winnerName)
                    }
                }
                .put("players", playersArray)
        }

        private fun JSONObject.toRecord(): GameHistoryRecord {
            val playersArray = getJSONArray("players")
            val players = buildList {
                for (index in 0 until playersArray.length()) {
                    val playerJson = playersArray.getJSONObject(index)
                    add(
                        GameHistoryPlayerRecord(
                            name = playerJson.getString("name"),
                            status = PlayerStatus.valueOf(playerJson.getString("status")),
                            remainingMs = playerJson.getLong("remainingMs"),
                            progress = playerJson.getInt("progress")
                        )
                    )
                }
            }
            return GameHistoryRecord(
                id = getString("id"),
                playedAtEpochMs = getLong("playedAtEpochMs"),
                exerciseType = ExerciseType.valueOf(getString("exerciseType")),
                ladderType = LadderType.valueOf(getString("ladderType")),
                maxReps = getInt("maxReps"),
                timerMinutes = getInt("timerMinutes"),
                winnerName = if (isNull("winnerName")) null else getString("winnerName"),
                players = players
            )
        }
    }
}
