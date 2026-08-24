package com.rnr.gymchess.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rnr.gymchess.domain.model.GameState
import com.rnr.gymchess.domain.model.Player
import com.rnr.gymchess.domain.model.PlayerStatus
import com.rnr.gymchess.util.formatTimeMs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    gameState: GameState?,
    onNewGame: () -> Unit,
    onOpenHistory: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Результаты") })
        }
    ) { padding ->
        if (gameState == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Нет данных об игре")
                Button(onClick = onNewGame, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Новая игра")
                }
            }
            return@Scaffold
        }

        val winner = gameState.winner
        val sortedPlayers = gameState.players.sortedWith(
            compareByDescending<Player> { it.status == PlayerStatus.FINISHED }
                .thenByDescending { it.remainingMs }
                .thenByDescending { it.progress }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (winner != null) "Победитель" else "Игра окончена",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = winner?.name ?: "Нет победителя",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (winner != null) {
                        Text(
                            text = "Остаток времени: ${formatTimeMs(winner.remainingMs)}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Text(
                        text = gameState.config.exerciseType.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Text("Таблица", style = MaterialTheme.typography.titleMedium)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedPlayers, key = { it.id }) { player ->
                    ResultPlayerRow(
                        player = player,
                        gameState = gameState,
                        isWinner = player.id == winner?.id
                    )
                }
            }

            Button(
                onClick = onNewGame,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Новая игра")
            }

            OutlinedButton(
                onClick = onOpenHistory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("История игр")
            }
        }
    }
}

@Composable
private fun ResultPlayerRow(
    player: Player,
    gameState: GameState,
    isWinner: Boolean
) {
    val statusText = when (player.status) {
        PlayerStatus.FINISHED -> "Финиш"
        PlayerStatus.ELIMINATED -> "Время вышло"
        PlayerStatus.ACTIVE -> "Не завершил"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isWinner) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$statusText • ${player.progress}/${player.totalSteps(gameState)} шагов",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = formatTimeMs(player.remainingMs),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
