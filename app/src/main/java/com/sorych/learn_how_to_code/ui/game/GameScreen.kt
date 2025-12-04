package com.sorych.learn_how_to_code.ui.game

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Bitmap
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sorych.learn_how_to_code.R
import com.sorych.learn_how_to_code.ui.theme.Learn_how_to_codeTheme
import kotlin.math.sqrt


fun Float.dpToPx(context: Context): Int =
    (this * context.resources.displayMetrics.density).toInt()

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(
        factory = GameViewModel.Factory
    )
) {
    val context = LocalContext.current
    val levelConfig by viewModel.levelConfig.collectAsState()
    val tileSize =64.dp

    // setting for background
    val tileBitmap = remember {
        val drawable = AppCompatResources.getDrawable(context, R.drawable.sea_waves2)!!
        drawable.toBitmap(
            width = tileSize.value.dpToPx(context),
            height = tileSize.value.dpToPx(context),
            config = Bitmap.Config.ARGB_8888
        ).asImageBitmap()
    }

    val icon: Painter = painterResource(id = R.drawable.starfish)

    // Animation state
    var isPlaying by remember { mutableStateOf(1) }
    var position by remember { mutableStateOf(IntOffset(0, 0)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(levelConfig.backgroundColor) // base color
    ) {
        TiledBackground(tileBitmap = tileBitmap)

        // Draws paths
        Canvas(Modifier
            .fillMaxSize()
            .padding(top=100.dp)
        ) {
            val pathWidth: Float = 64f
            // Draw all paths for this level
            levelConfig.paths.forEach { pathConfig ->
                val start = Offset(x = size.width * pathConfig.startX, y = size.height * pathConfig.startY)
                val end = Offset(x = size.width * pathConfig.endX, y = size.height * pathConfig.endY)

                drawLine(
                    color = pathConfig.pathColor,
                    start = start,
                    end = end,
                    strokeWidth = pathConfig.strokeWidth,
                    cap = StrokeCap.Round
                )

                // Draw icons
                val dx = end.x - start.x
                val dy = end.y - start.y
                val iconSizePx = 48.dp.toPx()
                val distance = sqrt(dx*dx + dy*dy)
                val stepX = dx / distance * pathConfig.iconGap
                val stepY = dy / distance * pathConfig.iconGap

                var x = start.x
                var y = start.y

                while (sqrt((x - start.x)*(x - start.x) + (y - start.y)*(y - start.y)) + iconSizePx < distance) {
                    drawIntoCanvas { canvas ->
                        canvas.save()
                        canvas.translate(x - iconSizePx/2, y - iconSizePx/2)

                        with(icon) {
                            draw(
                                size = Size(iconSizePx, iconSizePx),
                                alpha = 0.8f
                            )
                        }

                        canvas.restore()
                    }
                    x += stepX
                    y += stepY
                }
            }
        }

        // UI Layer on top
        Column(modifier = Modifier
            .fillMaxSize()
            .zIndex(10f)
        ) {
            GameControls(
                boxCount = levelConfig.paths.size,
                isPlaying = isPlaying,
                position = position,
                onDirectionSelected = { direction ->
                    isPlaying = direction
                },
                onAnimationComplete = {
                    // Update position after animation
                    position = when (isPlaying) {
                        1 -> position + IntOffset(0, -100) // up
                        2 -> position + IntOffset(0, 100)  // down
                        3 -> position + IntOffset(-170, 0) // left
                        4 -> position + IntOffset(170, 0)  // right
                        else -> position
                    }
                    isPlaying = 0
                }
            )
        }
    }
}

@Composable
fun GameControls(
    boxCount: Int,
    isPlaying: Int,
    position: IntOffset,
    onDirectionSelected: (Int) -> Unit,
    onAnimationComplete: () -> Unit
) {
    var dragBoxIndex by remember { mutableIntStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Drop zones
        Row(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(8.dp)
        ) {
            repeat(boxCount) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .size(80.dp)
                        .padding(10.dp)
                        .border(1.dp, Color.Black)
                        .dragAndDropTarget(
                            shouldStartDragAndDrop = { event ->
                                event
                                    .mimeTypes()
                                    .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                            },
                            target = remember {
                                object : DragAndDropTarget {
                                    override fun onDrop(event: DragAndDropEvent): Boolean {
                                        dragBoxIndex = index

                                        val direction = when (index) {
                                            0 -> 1  // up
                                            1 -> 2  // down
                                            2 -> 3  // left
                                            else -> 4  // right
                                        }
                                        onDirectionSelected(direction)

                                        return true
                                    }
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    this@Row.AnimatedVisibility(
                        visible = index == dragBoxIndex,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.right),
                            contentDescription = "Right Arrow",
                            modifier = Modifier
                                .fillMaxSize(),
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        }


        // Draggable arrow icon - THIS is what the user drags
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.right),
                contentDescription = "Draggable Arrow",
                modifier = Modifier
                    .size(64.dp)
                    .dragAndDropSource(
                        transferData = { offset ->
                            DragAndDropTransferData(
                                clipData = ClipData.newPlainText("arrow", "Arrow")
                            )
                        }
                    ),
                tint = Color.Unspecified
            )
        }



        // Animated turtle on the path
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp),
            contentAlignment = Alignment.TopStart
        ) {
            AnimatedTurtle(
                isPlaying = isPlaying,
                position = position,
                onAnimationComplete = onAnimationComplete
            )
        }
    }
}

@Composable
fun AnimatedTurtle(
    isPlaying: Int,
    position: IntOffset,
    onAnimationComplete: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")

    val pOffset by animateIntOffsetAsState(
        targetValue = when (isPlaying) {
            0 -> position
            1 -> position + IntOffset(0, -100)  // up
            2 -> position + IntOffset(0, 100)   // down
            3 -> position + IntOffset(-170, 0)  // left
            else -> position + IntOffset(170, 0) // right
        },
        animationSpec = tween(1500, easing = LinearEasing),
        label = "position"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(3000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Icon(
        painter = painterResource(R.drawable.turtle),
        contentDescription = "Animated Turtle",
        modifier = Modifier
            .size(48.dp)
            .offset { pOffset }
            .rotate(rotation),
        tint = Color.Unspecified
    )
}

@Composable
fun TiledBackground(tileBitmap: ImageBitmap) {
    Canvas(modifier = Modifier
        .fillMaxSize()
        .padding(top=100.dp)
    ) {
        val tileW = tileBitmap.width.toFloat()
        val tileH = tileBitmap.height.toFloat()

        val spacingFactor = 1f

        // Tile across width & height
        for (x in 0..(size.width / tileW).toInt()) {
            for (y in 0..(size.height / tileH).toInt()) {
                drawImage(
                    image = tileBitmap,
                    topLeft = Offset(x * tileW*spacingFactor, y * tileH*spacingFactor),
                    colorFilter = ColorFilter.tint(Color(0xFF2096f3)),
                    alpha = 0.4f
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    Learn_how_to_codeTheme {
        GameScreen()
    }
}