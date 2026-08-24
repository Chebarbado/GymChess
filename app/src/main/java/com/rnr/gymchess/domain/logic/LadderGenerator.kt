package com.rnr.gymchess.domain.logic

import com.rnr.gymchess.domain.model.LadderType

object LadderGenerator {
    fun generate(max: Int, type: LadderType): List<Int> = when (type) {
        LadderType.FAST -> (1..max).toList()
        LadderType.SLOW -> (1..max).flatMap { listOf(it, it) }
        LadderType.UP_ONLY -> (1..max).toList()
        LadderType.UP_DOWN -> buildList {
            addAll(1..max)
            if (max > 1) {
                addAll(max - 1 downTo 1)
            }
        }
    }
}
