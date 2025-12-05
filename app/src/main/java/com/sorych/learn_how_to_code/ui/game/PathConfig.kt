package com.sorych.learn_how_to_code.ui.game

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sorych.learn_how_to_code.R

data class PathConfig(
    val startCell: IntOffset,
    val endCell: IntOffset,
)

data class LevelConfig(
    val levelNumber: Int,
    val paths: List<PathConfig>,
    val backgroundColor: Color,
    val backgroundTile: Int
)

// Add this data class for grid configuration
data class GridConfig(
    val rows: Int = 10,
    val cols: Int = 10,
    val cellSize: Dp = 64.dp
)


object LevelConfigs {
    fun getLevel(levelNumber: Int): LevelConfig {
        return when (levelNumber) {
            1 -> LevelConfig(
                levelNumber = 1,
                paths = listOf(
                    PathConfig(
                        startCell = IntOffset(1, 8),
                        endCell = IntOffset(5, 8)
                    ),
                    PathConfig(
                        startCell = IntOffset(5, 8),
                        endCell = IntOffset(5, 4)
                    ),
                    PathConfig(
                        startCell = IntOffset(5, 4),
                        endCell = IntOffset(9, 4)
                    )
                ),
                backgroundColor = Color(0xFF82d4fa),
                backgroundTile = R.drawable.sea_waves2
            )
            2 -> LevelConfig(
                levelNumber = 2,
                paths = listOf(
                    PathConfig(
                        startCell = IntOffset(1, 2),
                        endCell = IntOffset(9, 2)
                    ),
                    PathConfig(
                        startCell = IntOffset(1, 8),
                        endCell = IntOffset(9, 8)
                    )
                ),
                backgroundColor = Color(0xFF82d4fa),
                backgroundTile = R.drawable.sea_waves2
            )
            3 -> LevelConfig(
                levelNumber = 3,
                paths = listOf(
                    PathConfig(
                        startCell = IntOffset(1, 3),
                        endCell = IntOffset(5, 5)
                    ),
                    PathConfig(
                        startCell = IntOffset(5, 5),
                        endCell = IntOffset(9, 7)
                    )
                ),
                backgroundColor = Color(0xFF82d4fa),
                backgroundTile = R.drawable.sea_waves2
            )
            else -> getLevel(1) // Default to level 1
        }
    }
}