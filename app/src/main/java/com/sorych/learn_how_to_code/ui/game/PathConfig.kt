package com.sorych.learn_how_to_code.ui.game

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import com.sorych.learn_how_to_code.R

// Main game sequence with all paths and valid solutions
data class GameSequence(
    val allPaths: List<PathConfig>,  // All visible paths (correct + wrong branches)
    val validSolutions: List<Solution>,  // All correct solutions
    val startCell: IntOffset  // Where the turtle starts
)

// A single path segment
data class PathConfig(
    val startCell: IntOffset,
    val endCell: IntOffset,
    val id: String  // Unique identifier for this path
)

// A valid solution with directions and which paths to follow
data class Solution(
    val directions: List<Int>,  // 1=up, 2=down, 3=left, 4=right
    val pathIds: List<String>,  // Which path IDs this solution uses in order
    val description: String = ""  // Optional: "Top route", "Bottom route", etc.
)

// For loops - defines a repeating section
data class LoopConfig(
    val pathIds: List<String>,  // Which paths are in the loop
    val repeatCount: Int  // How many times to repeat
)

data class LevelConfig(
    val levelNumber: Int,
    val games: List<GameSequence>,
    val backgroundColor: Color,
    val backgroundTile: Int
)

data class GridConfig(
    val rows: Int = 7,
    val cols: Int = 13
)

object LevelConfigs {
    fun getLevel(levelNumber: Int): LevelConfig {
        return when (levelNumber) {
            1 -> LevelConfig(
                levelNumber = 1,
                games = listOf(
                    // Game 1: simple 3 steps
                    GameSequence(
                        startCell = IntOffset(0, 5),
                        allPaths = listOf(
                            PathConfig(IntOffset(0,5), IntOffset(6,5), "p1"),
                            PathConfig(IntOffset(6,5), IntOffset(6,1), "p2"),
                            PathConfig(IntOffset(6,1), IntOffset(12,1), "p3")
                        ),
                        validSolutions = listOf(
                            Solution(
                                directions = listOf(4, 1, 4),  // right, up, right
                                pathIds = listOf("p1", "p2", "p3"),
                                description = "simple 3 step"
                            )
                        )
                    ),

//                    // Game 2: 5 steps
//                    GameSequence(
//                        startCell = IntOffset(0, 0),
//                        allPaths = listOf(
//                            // First iteration
//                            PathConfig(IntOffset(0,0), IntOffset(2,0), "p1"),
//                            PathConfig(IntOffset(2,0), IntOffset(2,6), "p2"),
//                            PathConfig(IntOffset(2,6), IntOffset(10,6), "p3"),
//                            PathConfig(IntOffset(10,6), IntOffset(10,0), "p4"),
//                            PathConfig(IntOffset(10,0), IntOffset(12,0), "p5"),
//                        ),
//                        validSolutions = listOf(
//                            Solution(
//                                // 1=up, 2=down, 3=left, 4=right
//                                // Pattern: right, down (repeated 3 times), then right
//                                directions = listOf(4, 2, 4, 1, 4),
//                                pathIds = listOf("p1", "p2", "p3", "p4", "p5"),
//                                description = "simple 5 step"
//                            )
//                        )
//                    ),
//
//                    // Game 3: Zigzag pattern (7 steps)
//                    GameSequence(
//                        startCell = IntOffset(0, 0),
//                        allPaths = listOf(
//                            PathConfig(IntOffset(0,0), IntOffset(3,0), "p1"),
//                            PathConfig(IntOffset(3,0), IntOffset(3,2), "p2"),
//                            PathConfig(IntOffset(3,2), IntOffset(6,2), "p3"),
//                            PathConfig(IntOffset(6,2), IntOffset(6,4), "p4"),
//                            PathConfig(IntOffset(6,4), IntOffset(9,4), "p5"),
//                            PathConfig(IntOffset(9,4), IntOffset(9,6), "p6"),
//                            PathConfig(IntOffset(9,6), IntOffset(12,6), "p7")
//                        ),
//                        validSolutions = listOf(
//                            Solution(
//                                directions = listOf(4, 2, 4, 2, 4, 2, 4),
//                                pathIds = listOf("p1", "p2", "p3", "p4", "p5", "p6", "p7"),
//                                description = "Zigzag path"
//                            )
//                        )
//                    )
                ),
                backgroundColor = Color(0xFF82d4fa),
                backgroundTile = R.drawable.sea_waves2
            )
            2 -> LevelConfig(
                levelNumber = 2,
                games = listOf(

                    // Game 1: Branching paths with multiple solutions
                    GameSequence(
                        startCell = IntOffset(0, 0),
                        allPaths = listOf(
                            // Common start
                            PathConfig(IntOffset(0,0), IntOffset(5,0), "b1"),
                            PathConfig(IntOffset(5,0), IntOffset(5,3), "b2"),

                            // Branch point at (5,3)
                            PathConfig(IntOffset(5,3), IntOffset(0,3), "wrong1"),  // Wrong path
                            PathConfig(IntOffset(5,3), IntOffset(7,3), "b3"),      // Correct branch

                            // Continue main path
                            PathConfig(IntOffset(7,3), IntOffset(7,6), "b4"),
                            PathConfig(IntOffset(7,3), IntOffset(7,0), "b5"),
                            PathConfig(IntOffset(7,0), IntOffset(12,0), "b6"),

                            // Second branch point at (7,6)
                            PathConfig(IntOffset(7,6), IntOffset(12,6), "b5a"),    // Solution A
                            PathConfig(IntOffset(7,3), IntOffset(7,6), "b5b"),     // Solution B - part 1
                        ),
                        validSolutions = listOf(
                            // Solution A: Go right to bottom edge
                            Solution(
                                directions = listOf(4, 2, 4, 1, 4),
                                pathIds = listOf("b1", "b2", "b3", "b5", "b6"),
                                description = "Bottom route"
                            ),
                            // Solution B: Go up then right to top edge
                            Solution(
                                directions = listOf(4, 2, 4, 2, 4),
                                pathIds = listOf("b1", "b2", "b3", "b4", "b5a"),
                                description = "Top route"
                            )
                        )
                    ),

                    // Game 4: Loop example (repeat a pattern 3 times)
                    GameSequence(
                        startCell = IntOffset(0, 3),
                        allPaths = listOf(
                            // First iteration
                            PathConfig(IntOffset(0,3), IntOffset(2,3), "a1"),
                            PathConfig(IntOffset(2,3), IntOffset(2,5), "a2"),

                            // Second iteration
                            PathConfig(IntOffset(2,5), IntOffset(4,5), "a3"),
                            PathConfig(IntOffset(4,5), IntOffset(4,3), "a4"),

                            // Third iteration
                            PathConfig(IntOffset(4,3), IntOffset(6,3), "a5"),
                            PathConfig(IntOffset(6,3), IntOffset(6,5), "a6"),

                            // Final path
                            PathConfig(IntOffset(6,5), IntOffset(8,5), "a7"),
                            PathConfig(IntOffset(8,5), IntOffset(8,3), "a7"),
                            PathConfig(IntOffset(8,3), IntOffset(10,3), "a7"),
                            PathConfig(IntOffset(10,3), IntOffset(10,5), "a7"),
                            PathConfig(IntOffset(10,5), IntOffset(12,5), "a7")
                        ),
                        validSolutions = listOf(
                            Solution(
                                // 1=up, 2=down, 3=left, 4=right
                                // Pattern: right, down (repeated 3 times), then right
                                directions = listOf(4, 2, 4, 1, 4, 2, 4, 1, 4, 2, 4),
                                pathIds = listOf(
                                    "loop1_1", "loop1_2",
                                    "loop2_1", "loop2_2",
                                    "loop3_1", "loop3_2",
                                    "end"
                                ),
                                description = "Loop pattern"
                            )
                        )
                    )
                ),
                backgroundColor = Color(0xFF82d4fa),
                backgroundTile = R.drawable.sea_waves2
            )
            else -> getLevel(1)
        }
    }
}

// Helper extension to get paths by IDs
fun GameSequence.getPathsByIds(ids: List<String>): List<PathConfig> {
    return ids.mapNotNull { id -> allPaths.find { it.id == id } }
}

// Validation helper
fun GameSequence.isValidSolution(playerDirections: List<Int>): Solution? {
    return validSolutions.firstOrNull { solution ->
        solution.directions == playerDirections
    }
}

