package com.rnr.gymchess.ui.game

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rnr.gymchess.domain.model.GamePhase
import com.rnr.gymchess.domain.model.GameState
import com.rnr.gymchess.domain.model.PlayerStatus
import com.rnr.gymchess.domain.model.isGlobalLadder
import com.rnr.gymchess.ui.GymChessViewModelFactory
import com.rnr.gymchess.util.formatTimeMs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    onGameFinished: () -> Unit,
    viewModel: GameViewModel = viewModel(factory = GymChessViewModelFactory.factory)
) {
    val gameState by viewModel.gameState.collectAsState()
    var showForfeitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(gameState?.phase) {
        if (gameState?.phase == GamePhase.FINISHED) {
            onGameFinished()
        }
    }

    if (gameState == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Игра не найдена")
        }
        return
    }

    val state = gameState!!
    val activePlayer = state.activePlayer

    if (showForfeitDialog) {
        AlertDialog(
            onDismissRequest = { showForfeitDialog = false },
            title = { Text("Сдаться?") },
            text = { Text("Таймер ${activePlayer?.name ?: "игрока"} будет обнулён.") },
            confirmButton = {
                TextButton(onClick = {
                    showForfeitDialog = false
                    viewModel.forfeitActivePlayer()
                }) {
                    Text("Сдаться")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForfeitDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Игра")
                        Text(
                            text = state.config.exerciseType.label,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isPaused) {
                Text(
                    text = "ПАУЗА",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.players, key = { it.id }) { player ->
                    val isActive = player.id == activePlayer?.id &&
                        player.status == PlayerStatus.ACTIVE &&
                        !state.isPaused
                    PlayerCard(
                        player = player,
                        gameState = state,
                        isActive = isActive
                    )
                }
            }

            activePlayer?.let { player ->
                if (player.status == PlayerStatus.ACTIVE) {
                    val reps = player.currentReps(state) ?: 0
                    val (currentStep, totalSteps) = if (state.config.ladderType.isGlobalLadder()) {
                        state.globalStepIndex + 1 to state.ladderSteps.size
                    } else {
                        player.currentStepIndex + 1 to player.totalSteps(state)
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Ход: ${player.name}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Сделать: $reps",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ход $currentStep из $totalSteps",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = viewModel::togglePause,
                    modifier = Modifier.weight(1f),
                    enabled = state.phase == GamePhase.PLAYING
                ) {
                    Text(if (state.isPaused) "Продолжить" else "Пауза")
                }
                OutlinedButton(
                    onClick = { showForfeitDialog = true },
                    modifier = Modifier.weight(1f),
                    enabled = activePlayer?.status == PlayerStatus.ACTIVE
                ) {
                    Text("Сдаться")
                }
            }

            Button(
                onClick = viewModel::completeTurn,
                modifier = Modifier.fillMaxWidth(),
                enabled = activePlayer?.status == PlayerStatus.ACTIVE && !state.isPaused
            ) {
                Text("Готово — передать ход")
            }
        }
    }
}

@Composable
private fun PlayerCard(
    player: com.rnr.gymchess.domain.model.Player,
    gameState: GameState,
    isActive: Boolean
) {
    val statusText = when (player.status) {
        PlayerStatus.ACTIVE -> {
            val reps = if (isActive) player.currentReps(gameState) else null
            if (reps != null) "Подход: $reps" else "Ожидает хода"
        }
        PlayerStatus.FINISHED -> "Финиш"
        PlayerStatus.ELIMINATED -> "Время вышло"
    }
    val totalSteps = player.totalSteps(gameState)

    val borderColor = when {
        isActive -> MaterialTheme.colorScheme.primary
        player.status == PlayerStatus.FINISHED -> MaterialTheme.colorScheme.tertiary
        player.status == PlayerStatus.ELIMINATED -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isActive) 3.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = player.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatTimeMs(player.remainingMs),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = when (player.status) {
                    PlayerStatus.ELIMINATED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${player.progress}/$totalSteps",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
