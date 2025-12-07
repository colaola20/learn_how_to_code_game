package com.sorych.learn_how_to_code.ui.game

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sorych.learn_how_to_code.R

data class PathConfig(
    val startCell: IntOffset,
    val endCell: IntOffset,
    val correctSequence: Int// 1=up, 2=down, 3=left, 4=right
)

data class LevelConfig(
    val levelNumber: Int,
    val paths: List<PathConfig>,
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
                paths = listOf(
                    PathConfig(
                        startCell = IntOffset(1, 5), // top row = 0, bottom row = 6
                        endCell = IntOffset(6, 5),
                        correctSequence = 4
                    ),
                    PathConfig(
                        startCell = IntOffset(6, 5),
                        endCell = IntOffset(6, 1),
                        correctSequence = 1
                    ),
                    PathConfig(
                        startCell = IntOffset(6, 1),
                        endCell = IntOffset(9, 1),
                        correctSequence = 4
                    )
                ),
                backgroundColor = Color(0xFF82d4fa),
                backgroundTile = R.drawable.sea_waves2
            )

//            2 -> LevelConfig(
//                levelNumber = 2,
//                paths = listOf(
//                    PathConfig(
//                        startCell = IntOffset(1, 1),
//                        endCell = IntOffset(9, 1)
//                    ),
//                    PathConfig(
//                        startCell = IntOffset(1, 5),
//                        endCell = IntOffset(9, 5)
//                    )
//                ),
//                backgroundColor = Color(0xFF82d4fa),
//                backgroundTile = R.drawable.sea_waves2
//            )
//
//            3 -> LevelConfig(
//                levelNumber = 3,
//                paths = listOf(
//                    PathConfig(
//                        startCell = IntOffset(1, 2),
//                        endCell = IntOffset(5, 4)
//                    ),
//                    PathConfig(
//                        startCell = IntOffset(5, 4),
//                        endCell = IntOffset(9, 6)
//                    )
//                ),
//                backgroundColor = Color(0xFF82d4fa),
//                backgroundTile = R.drawable.sea_waves2
//            )
            else -> getLevel(1) // Default to level 1
        }
    }
}