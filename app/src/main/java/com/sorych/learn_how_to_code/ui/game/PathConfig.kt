package com.sorych.learn_how_to_code.ui.game

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sorych.learn_how_to_code.R

data class PathConfig(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val iconGap: Float = 0.05f,
    val pathColor: Color = Color(0xFF82d4fa),
    val strokeWidth: Float = 1f
)

data class LevelConfig(
    val levelNumber: Int,
    val paths: List<PathConfig>,
    val backgroundColor: Color,
    val backgroundTile: Int
)

// Add this data class for grid configuration
data class GridConfig(
    val rows: Int = 8,
    val cols: Int = 6,
    val cellSize: Dp = 60.dp
)


object LevelConfigs {
    fun getLevel(levelNumber: Int): LevelConfig {
        return when (levelNumber) {
            1 -> LevelConfig(
                levelNumber = 1,
                paths = listOf(
                    PathConfig(
                        startX = 0.1f,
                        startY = 0.8f,
                        endX = 0.5f,
                        endY = 0.8f
                    ),
                    PathConfig(
                        startX = 0.5f,
                        startY = 0.8f,
                        endX = 0.5f,
                        endY = 0.4f
                    ),
                    PathConfig(
                        startX = 0.5f,
                        startY = 0.4f,
                        endX = 0.9f,
                        endY = 0.4f
                    )
                ),
                backgroundColor = Color(0xFF82d4fa),
                backgroundTile = R.drawable.sea_waves2
            )
            2 -> LevelConfig(
                levelNumber = 2,
                paths = listOf(
                    PathConfig(
                        startX = 0.1f,
                        startY = 0.2f,
                        endX = 0.9f,
                        endY = 0.2f
                    ),
                    PathConfig(
                        startX = 0.1f,
                        startY = 0.8f,
                        endX = 0.9f,
                        endY = 0.8f
                    )
                ),
                backgroundColor = Color(0xFF82d4fa),
                backgroundTile = R.drawable.sea_waves2
            )
            3 -> LevelConfig(
                levelNumber = 3,
                paths = listOf(
                    // Curved/zigzag path
                    PathConfig(
                        startX = 0.1f,
                        startY = 0.3f,
                        endX = 0.5f,
                        endY = 0.5f
                    ),
                    PathConfig(
                        startX = 0.5f,
                        startY = 0.5f,
                        endX = 0.9f,
                        endY = 0.7f
                    )
                ),
                backgroundColor = Color(0xFF82d4fa),
                backgroundTile = R.drawable.sea_waves2
            )
            else -> getLevel(1) // Default to level 1
        }
    }
}