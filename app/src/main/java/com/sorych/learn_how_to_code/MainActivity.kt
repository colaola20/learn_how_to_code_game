package com.sorych.learn_how_to_code

import com.sorych.learn_how_to_code.ui.game.GameScreen
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.sorych.learn_how_to_code.ui.theme.Learn_how_to_codeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        enableEdgeToEdge()
        setContent {
            Learn_how_to_codeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    NavClass()
                }
            }
//            GameScreen()
        }
    }
}