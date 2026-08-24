package com.rnr.gymchess

import android.app.Application
import com.rnr.gymchess.data.GameHistoryRepository
import com.rnr.gymchess.ui.GymChessViewModelFactory
import com.rnr.gymchess.util.GameFeedbackManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class GymChessApp : Application() {
    lateinit var historyRepository: GameHistoryRepository
        private set

    lateinit var feedbackManager: GameFeedbackManager
        private set

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        historyRepository = GameHistoryRepository(this)
        feedbackManager = GameFeedbackManager(this)
        GymChessViewModelFactory.init(this)
    }
}
