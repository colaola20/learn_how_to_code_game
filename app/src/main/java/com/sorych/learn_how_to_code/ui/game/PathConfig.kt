package com.sorych.learn_how_to_code.ui.game

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sorych.learn_how_to_code.R


data class GameSequence(
    val paths: List<PathConfig>
)
data class PathConfig(
    val startCell: IntOffset,
    val endCell: IntOffset,
    val correctSequence: Int// 1=up, 2=down, 3=left, 4=right
)

data class LevelConfig(
    val levelNumber: Int,
    val games: List<GameSequence>,
//    val paths: List<PathConfig>,
    val backgroundColor: Color,
    val backgroundTile: Int
)

// Add this data class for grid configuration
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
                    // Game 1
                    GameSequence(
                        paths = listOf(
                            PathConfig(IntOffset(1,5), IntOffset(6,5), 4),
                            PathConfig(IntOffset(6,5), IntOffset(6,1), 1),
                            PathConfig(IntOffset(6,1), IntOffset(9,1), 4)
                        )
                    ),
                    // Game 2
                    GameSequence(
                        paths = listOf(
                            PathConfig(IntOffset(0,0), IntOffset(3,3), 2),
                            PathConfig(IntOffset(3,3), IntOffset(6,6), 4)
                        )
                    ),
                    // Game 3
                    GameSequence(
                        paths = listOf(
                            PathConfig(IntOffset(2,2), IntOffset(5,5), 3),
                            PathConfig(IntOffset(5,5), IntOffset(8,2), 1)
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