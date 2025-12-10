package com.sorych.learn_how_to_code.ui.start

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sorych.learn_how_to_code.R
import com.sorych.learn_how_to_code.ui.theme.DeepOceanBlue
import com.sorych.learn_how_to_code.ui.theme.SandyBeige

@Composable
fun StartGameScreen(
    onStartClick: () -> Unit = {}
){
    Column (modifier = Modifier
        .fillMaxSize()
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFA8EDEA), Color(0xFF2F9EB3), Color(0xFF003F5C))
            )
        )
        .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterVertically // center items vertically
        )
    ) {
        Button(
            onClick = {
                onStartClick()
            },
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(0.3f),
            colors = ButtonDefaults.buttonColors(
                containerColor = SandyBeige,
                contentColor = DeepOceanBlue
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp) // space between icon and text
            ) {
                Icon(
                    painter = painterResource(R.drawable.play_game),
                    tint = Color.Unspecified,
                    contentDescription = "Start game icon",
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "Start",
                    fontSize = 16.sp
                )
            }
        }
    }
}