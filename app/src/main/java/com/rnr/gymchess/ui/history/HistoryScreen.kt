package com.rnr.gymchess.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rnr.gymchess.domain.model.GameHistoryRecord
import com.rnr.gymchess.domain.model.PlayerStatus
import com.rnr.gymchess.ui.GymChessViewModelFactory
import com.rnr.gymchess.util.formatHistoryDate
import com.rnr.gymchess.util.formatTimeMs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = GymChessViewModelFactory.factory)
) {
    val history by viewModel.history.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("История игр") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (history.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Пока нет сохранённых игр",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(history, key = { it.id }) { record ->
                        HistoryRecordCard(record)
                    }
                }

                OutlinedButton(
                    onClick = viewModel::clearHistory,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Очистить историю")
                }
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Назад")
            }
        }
    }
}

@Composable
private fun HistoryRecordCard(record: GameHistoryRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = formatHistoryDate(record.playedAtEpochMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = record.exerciseType.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${record.ladderType.label} • до ${record.maxReps} • ${record.timerMinutes} мин",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Победитель: ${record.winnerName ?: "нет"}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            record.players.forEach { player ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val statusText = when (player.status) {
                        PlayerStatus.FINISHED -> "финиш"
                        PlayerStatus.ELIMINATED -> "время вышло"
                        PlayerStatus.ACTIVE -> "не завершил"
                    }
                    Text(
                        text = "${player.name} • $statusText • ${player.progress} шагов",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = formatTimeMs(player.remainingMs),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
