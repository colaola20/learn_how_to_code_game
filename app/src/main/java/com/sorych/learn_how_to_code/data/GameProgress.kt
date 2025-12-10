package com.sorych.learn_how_to_code.data

import kotlinx.serialization.Serializable

@Serializable
data class GameProgress (
    val level: Int,
    val score: Int
)