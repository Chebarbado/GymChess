package com.rnr.gymchess.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rnr.gymchess.data.GameHistoryRepository
import com.rnr.gymchess.domain.model.GameHistoryRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val historyRepository: GameHistoryRepository
) : ViewModel() {

    val history: StateFlow<List<GameHistoryRecord>> = historyRepository.history
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
        }
    }
}
