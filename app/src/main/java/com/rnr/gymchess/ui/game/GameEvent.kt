package com.rnr.gymchess.ui.game

import com.rnr.gymchess.domain.model.Player

sealed interface GameEvent {
    data class PlayerTimedOut(val player: Player) : GameEvent
}
