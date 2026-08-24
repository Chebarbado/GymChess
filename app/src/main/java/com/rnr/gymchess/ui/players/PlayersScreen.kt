package com.rnr.gymchess.ui.players

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rnr.gymchess.ui.GymChessViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayersScreen(
    onBack: () -> Unit,
    onStartGame: () -> Unit,
    viewModel: PlayersViewModel = viewModel(factory = GymChessViewModelFactory.factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Игроки") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            uiState.playerNames.forEachIndexed { index, name ->
                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.updateName(index, it) },
                    label = { Text("Игрок ${index + 1}") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Назад")
            }

            Button(
                onClick = {
                    if (viewModel.startGame() != null) onStartGame()
                },
                enabled = uiState.isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Начать игру")
            }
        }
    }
}
