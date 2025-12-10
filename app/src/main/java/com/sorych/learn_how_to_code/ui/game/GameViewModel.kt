package com.sorych.learn_how_to_code.ui.game

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sorych.learn_how_to_code.TurtleTailAppClass
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel: ViewModel() {

    // Grid configuration
    val GridConfig = GridConfig(
        rows = 7,
        cols = 13
    )

    private val _currentLevel = MutableStateFlow(1)
    val currentLevel: StateFlow<Int> = _currentLevel.asStateFlow()

    private val _levelConfig = MutableStateFlow(LevelConfigs.getLevel(1))
    val levelConfig: StateFlow<LevelConfig> = _levelConfig.asStateFlow()

    fun loadLevel(levelNumber: Int) {
        _currentLevel.value = levelNumber
        _levelConfig.value = LevelConfigs.getLevel(levelNumber)
    }

    fun nextLevel() {
        loadLevel(_currentLevel.value + 1)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                GameViewModel()
            }
        }
    }
}