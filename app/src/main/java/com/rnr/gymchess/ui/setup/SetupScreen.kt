package com.rnr.gymchess.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rnr.gymchess.domain.model.ExerciseType
import com.rnr.gymchess.domain.model.LadderType
import com.rnr.gymchess.ui.GymChessViewModelFactory
import com.rnr.gymchess.ui.components.SetupSection
import com.rnr.gymchess.ui.components.StepperRow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SetupScreen(
    onContinue: () -> Unit,
    onOpenHistory: () -> Unit,
    viewModel: SetupViewModel = viewModel(factory = GymChessViewModelFactory.factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Настройка игры") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SetupSection(title = "Тип упражнения") {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExerciseType.entries.forEach { type ->
                        FilterChip(
                            selected = uiState.exerciseType == type,
                            onClick = { viewModel.setExerciseType(type) },
                            label = { Text(type.label) }
                        )
                    }
                }
            }

            SetupSection(title = "Параметры") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    StepperRow(
                        label = "Лесенка до",
                        value = uiState.maxReps,
                        range = 1..50,
                        onValueChange = viewModel::setMaxReps
                    )
                    StepperRow(
                        label = "Минут на игрока",
                        value = uiState.timerMinutes,
                        range = 1..60,
                        onValueChange = viewModel::setTimerMinutes
                    )
                    StepperRow(
                        label = "Количество игроков",
                        value = uiState.playerCount,
                        range = 2..4,
                        onValueChange = viewModel::setPlayerCount
                    )
                }
            }

            SetupSection(title = "Тип лесенки") {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LadderType.entries.forEach { type ->
                        FilterChip(
                            selected = uiState.ladderType == type,
                            onClick = { viewModel.setLadderType(type) },
                            label = { Text(type.label) }
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        if (viewModel.saveAndContinue()) onContinue()
                    },
                    enabled = uiState.isValid,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Далее")
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
}
