package com.sorych.learn_how_to_code.ui.game

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset

// Represents a single cell position on the grid
// IntOffset is already from Compose, so we use it directly

// Represents the grid configuration
data class GridConfig(
    val rows: Int = 7,
    val cols: Int = 13
)

// Represents a path segment on the grid
data class PathConfig(
    val startCell: IntOffset,
    val endCell: IntOffset,
    val id: String  // Unique identifier for this path
)

// Represents a valid solution (sequence of moves)
data class Solution(
    val directions: List<Int>,  // 1=up, 2=down, 3=left, 4=right
    val pathIds: List<String>,  // Which path IDs this solution uses in order
)

// Represents a single game/puzzle
data class GameSequence(
    val allPaths: List<PathConfig>,  // All visible paths (correct + wrong branches)
    val validSolutions: List<Solution>,  // All correct solutions
    val startCell: IntOffset  // Where the turtle starts
) {
    // Helper function to get paths by their IDs
    fun getPathsByIds(ids: List<String>): List<PathConfig> {
        return ids.mapNotNull { id -> allPaths.find { it.id == id } }
    }

    // Helper function to check if a player's sequence matches any valid solution
    fun isValidSolution(playerSequence: List<Int>): Solution? {
        return validSolutions.find { solution ->
            solution.directions == playerSequence
        }
    }
}

data class LevelConfig(
    val levelNumber: Int,
    val games: List<GameSequence>,
    val backgroundColor: Color,
)