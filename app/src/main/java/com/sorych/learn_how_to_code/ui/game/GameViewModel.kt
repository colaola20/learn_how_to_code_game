package com.sorych.learn_how_to_code.ui.game
import LevelRepository
import android.util.Log
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sorych.learn_how_to_code.BuildConfig
import com.sorych.learn_how_to_code.TurtleTailAppClass
import com.sorych.learn_how_to_code.data.GameProgress
import com.sorych.learn_how_to_code.data.UserProgressRepository
import com.sorych.learn_how_to_code.services.LevelResponse
import com.sorych.learn_how_to_code.services.OpenAIService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject

class GameViewModel(
    private val levelRepository: LevelRepository,
    private val userProgressRepository: UserProgressRepository
): ViewModel() {
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as TurtleTailAppClass)
                GameViewModel(levelRepository = LevelRepository(), application.userProgressRepository)
            }
        }
    }

    val gameProgress: StateFlow<GameProgress> =
        userProgressRepository.whatProgress
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GameProgress(1, 0))

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

    // Track locally for immediate access
    private var _currentLevelNumber = 1
    private var _accumulatedScore = 0

    val currentLevel: Int
        get() = _currentLevelNumber

    val currentScore: Int
        get() = _accumulatedScore

    private val _levelConfig = MutableStateFlow(levelRepository.getLevel(1))
    val levelConfig: StateFlow<LevelConfig?> = _levelConfig.asStateFlow()

    init {
        loadSavedProgress()
    }

    private fun loadSavedProgress() {
        viewModelScope.launch {
            gameProgress.collect { progress ->
                // Only load the first emission
                if (_levelConfig.value == null) {
                    Log.d("GameViewModel", "📥 Loading saved progress - Level: ${progress.level}, Score: ${progress.score}")

                    _currentLevelNumber = progress.level
                    _accumulatedScore = progress.score
                    loadLevel(progress.level)

                    Log.d("GameViewModel", "✅ Progress loaded successfully")
                }
            }
        }
    }

    private val _isGeneratingLevel = MutableStateFlow(false)
    val isGeneratingLevel: StateFlow<Boolean> = _isGeneratingLevel.asStateFlow()

    fun loadLevel(levelNumber: Int) {
        _levelConfig.value = levelRepository.getLevel(levelNumber)
    }


    private fun saveProgress(level: Int, score: Int) {
        viewModelScope.launch {
            Log.d("GameViewModel", "SAVING: Level=$level, Score=$score")

            userProgressRepository.saveLevelProgress(level)
            userProgressRepository.saveScoreProgress(score)
            Log.d("GameViewModel", "SAVED successfully")
        }
    }

    fun completeGame() {
        _accumulatedScore += 1
        saveProgress(level = _currentLevelNumber, score = _accumulatedScore)
        Log.d("GameViewModel", "🎮 Game completed! Level: $_currentLevelNumber, Score: $_accumulatedScore")
    }


    fun completeLevel() {
        _accumulatedScore += 10
        _currentLevelNumber += 1 // Increment immediately
        saveProgress(level = _currentLevelNumber, score = _accumulatedScore)
        Log.d("GameViewModel", "🏆 Level completed! Next Level: $_currentLevelNumber, Score: $_accumulatedScore")
    }


    fun nextLevel() {
        Log.d("GameViewModel", "⏭️ Loading level $_currentLevelNumber")

        val existingLevel = levelRepository.getLevel(_currentLevelNumber)

        if (existingLevel != null) {
            _levelConfig.value = existingLevel
            Log.d("GameViewModel", "📂 Loaded existing level $_currentLevelNumber")
        } else {
            Log.d("GameViewModel", "🤖 Generating new level $_currentLevelNumber")
            generateLevelWithGPT(_currentLevelNumber)
        }
    }

    fun generateLevelWithGPT(levelNumber: Int) {
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


}