package com.sorych.learn_how_to_code.ui.game
import LevelRepository
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sorych.learn_how_to_code.BuildConfig
import com.sorych.learn_how_to_code.services.LevelResponse
import com.sorych.learn_how_to_code.services.OpenAIService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class GameViewModel(
    private val levelRepository: LevelRepository
): ViewModel() {

    init {
        Log.d("GameViewModel", "API Key loaded: ${BuildConfig.OPENAI_API_KEY.take(10)}...")
    }

    // Add API key (better to store in local.properties or secure storage)
    private val openAIService = OpenAIService(
        apiKey = BuildConfig.OPENAI_API_KEY.takeIf { it.isNotEmpty() }
            ?: throw IllegalStateException("OPENAI_API_KEY not configured in local.properties")
    )


    // Grid configuration
    val gridConfig = GridConfig(
        rows = 7,
        cols = 13
    )

    private val _levelConfig = MutableStateFlow(levelRepository.getLevel(1))
    val levelConfig: StateFlow<LevelConfig?> = _levelConfig.asStateFlow()

    private val _currentLevel = MutableStateFlow(1)
    val currentLevel: StateFlow<Int> = _currentLevel.asStateFlow()

    private val _isGeneratingLevel = MutableStateFlow(false)
    val isGeneratingLevel: StateFlow<Boolean> = _isGeneratingLevel.asStateFlow()

    fun loadLevel(levelNumber: Int) {
        _currentLevel.value = levelNumber
        _levelConfig.value = levelRepository.getLevel(levelNumber)
    }

    fun nextLevel() {
        val next = _currentLevel.value + 1

        // Check if level exists in repository
        val existingLevel = levelRepository.getLevel(next)

        if (existingLevel != null) {
            _currentLevel.value = next
            _levelConfig.value = existingLevel
        } else {
            // Generate new level using GPT
            generateLevelWithGPT(next)
        }
    }

    private fun generateLevelWithGPT(levelNumber: Int) {
        viewModelScope.launch {
            try {
                _isGeneratingLevel.value = true
                Log.d("GameViewModel", "Generating level $levelNumber...")

                // Get the LevelResponse directly from the service
                val levelResponse = openAIService.generateLevel(
                    currentLevel = levelNumber - 1,
                    gridSize = gridConfig.rows
                )

                Log.d("GameViewModel", "Received ${levelResponse.games.size} games")

                // Convert to LevelConfig
                val levelConfig = convertToLevelConfig(levelResponse, levelNumber)

                // Save to repository
                levelRepository.saveLevel(levelNumber, levelConfig)

                // Update UI
                _currentLevel.value = levelNumber
                _levelConfig.value = levelConfig

                Log.d("GameViewModel", "Level $levelNumber generated successfully!")

            } catch (e: Exception) {
                Log.e("GameViewModel", "Failed to generate level: ${e.message}", e)
                // Fallback to default level
                _levelConfig.value = createFallbackLevel(levelNumber)
            } finally {
                _isGeneratingLevel.value = false
            }
        }
    }

    private fun convertToLevelConfig(
        levelResponse: LevelResponse,
        levelNumber: Int
    ): LevelConfig {
        val games = levelResponse.games.map { gameDto ->
            // Convert paths
            val allPaths = gameDto.allPaths.map { pathDto ->
                openAIService.convertToPathConfig(pathDto)
            }

            // Compute valid solutions
            val directions = allPaths.map { path ->
                openAIService.computeDirection(path)
            }
            val pathIds = allPaths.map { it.id }

            GameSequence(
                startCell = IntOffset(gameDto.startCell.x, gameDto.startCell.y),
                allPaths = allPaths,
                validSolutions = listOf(
                    Solution(
                        directions = directions,
                        pathIds = pathIds
                    )
                )
            )
        }

        return LevelConfig(
            levelNumber = levelNumber,
            games = games,
            backgroundColor = Color(0xFF82d4fa)
        )
    }

    private fun createFallbackLevel(level: Int): LevelConfig {
        // Create a simple default level as fallback
        return LevelConfig(
            levelNumber = level,
            games = listOf(
                GameSequence(
                    startCell = IntOffset(0, 0),
                    allPaths = listOf(
                        PathConfig(IntOffset(0, 0), IntOffset(12, 0), "p1")
                    ),
                    validSolutions = listOf(
                        Solution(listOf(4), listOf("p1"))
                    )
                )
            ),
            backgroundColor = Color(0xFF82d4fa)
        )
    }

    private fun getLevelColor(level: Int): Color {
        val colors = listOf(
            Color(0xFF1976D2),
            Color(0xFF388E3C),
            Color(0xFFD32F2F),
            Color(0xFFF57C00)
        )
        return colors[level % colors.size]
    }


    fun computeDirections(path: PathConfig): Int {
        return when {
            path.endCell.x > path.startCell.x -> 4 // RIGHT
            path.endCell.x < path.startCell.x -> 3 // LEFT
            path.endCell.y > path.startCell.y -> 2 // DOWN
            path.endCell.y < path.startCell.y -> 1 // UP
            else -> 0
        }
    }

    fun generateValidSolutions(paths: List<PathConfig>): Solution {
        val directions = paths.map { computeDirections(it) }
        val pathIds = paths.map { it.id }
        return Solution(directions, pathIds)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                GameViewModel(levelRepository = LevelRepository())
            }
        }
    }
}