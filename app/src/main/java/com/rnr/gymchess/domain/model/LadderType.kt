package com.rnr.gymchess.domain.model

enum class LadderType(val label: String) {
    FAST("Быстрая (1-2-3-4…)"),
    SLOW("Медленная (1-1-2-2…)"),
    UP_ONLY("Только вверх (1…N)"),
    UP_DOWN("Вверх-вниз (1…N…1)")
}

fun LadderType.isGlobalLadder(): Boolean = this == LadderType.FAST
