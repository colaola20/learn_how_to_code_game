package com.sorych.learn_how_to_code.ui.game

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sorych.learn_how_to_code.R
import com.sorych.learn_how_to_code.ui.theme.Learn_how_to_codeTheme
import kotlin.math.pow
import kotlin.math.sqrt



fun Float.dpToPx(context: Context): Int =
    (this * context.resources.displayMetrics.density).toInt()

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel(
        factory = GameViewModel.Factory
    )
) {
    val context = LocalContext.current
    val levelConfig by viewModel.levelConfig.collectAsState()
    val gridConfig = viewModel.GridConfig
    val tileSize = 64.dp

    val tileBitmap = remember {
        val drawable = AppCompatResources.getDrawable(context, R.drawable.sea_waves2)!!
        drawable.toBitmap(
            width = tileSize.value.dpToPx(context),
            height = tileSize.value.dpToPx(context),
            config = Bitmap.Config.ARGB_8888
        ).asImageBitmap()
    }

    val icon: Painter = painterResource(id = R.drawable.starfish)
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(levelConfig.backgroundColor)
    ) {
        val topPaddingPx = with(density) { 100.dp.toPx() }.toInt()
        val cellSizePx = with(density) { gridConfig.cellSize.toPx() }.toInt()

        // Calculate starting position from first path
        val startingGridPosition = remember(levelConfig) {
            levelConfig.paths.firstOrNull()?.startCell ?: IntOffset(0, 0)
        }

        var gridPosition by remember(levelConfig) { mutableStateOf(startingGridPosition) }
        var isPlaying by remember { mutableStateOf(0) }

        TiledBackground(tileBitmap = tileBitmap)

        GridCanvas(
            gridConfig = gridConfig,
            paths = levelConfig.paths,
            starIcon = icon
        )

        // UI Layer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f)
        ) {
            GameControls(
                boxCount = levelConfig.paths.size,
                isPlaying = isPlaying,
                gridPosition = gridPosition,
                cellSizePx = cellSizePx,
                topPaddingPx = topPaddingPx,
                onDirectionSelected = { direction ->
                    isPlaying = direction
                },
                onAnimationComplete = {
                    // Update grid position after animation
                    gridPosition = when (isPlaying) {
                        1 -> gridPosition + IntOffset(0, -1)  // up
                        2 -> gridPosition + IntOffset(0, 1)   // down
                        3 -> gridPosition + IntOffset(-1, 0)  // left
                        4 -> gridPosition + IntOffset(1, 0)   // right
                        else -> gridPosition
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
    gridPosition: IntOffset,
    cellSizePx: Int,
    topPaddingPx: Int,
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
        Row(modifier = Modifier.fillMaxWidth()) {
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
                                modifier = Modifier.fillMaxSize(),
                                tint = Color.Unspecified
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.left),
                    contentDescription = "Draggable Arrow",
                    modifier = Modifier
                        .size(64.dp)
                        .dragAndDropSource(
                            transferData = { offset ->
                                DragAndDropTransferData(
                                    clipData = ClipData.newPlainText("left", "Left")
                                )
                            }
                        ),
                    tint = Color.Unspecified
                )
                Icon(
                    painter = painterResource(R.drawable.up),
                    contentDescription = "Draggable Arrow",
                    modifier = Modifier
                        .size(64.dp)
                        .dragAndDropSource(
                            transferData = { offset ->
                                DragAndDropTransferData(
                                    clipData = ClipData.newPlainText("up", "Up")
                                )
                            }
                        ),
                    tint = Color.Unspecified
                )
                Icon(
                    painter = painterResource(R.drawable.right),
                    contentDescription = "Draggable Arrow",
                    modifier = Modifier
                        .size(64.dp)
                        .dragAndDropSource(
                            transferData = { offset ->
                                DragAndDropTransferData(
                                    clipData = ClipData.newPlainText("right", "Right")
                                )
                            }
                        ),
                    tint = Color.Unspecified
                )
                Icon(
                    painter = painterResource(R.drawable.down),
                    contentDescription = "Draggable Arrow",
                    modifier = Modifier
                        .size(64.dp)
                        .dragAndDropSource(
                            transferData = { offset ->
                                DragAndDropTransferData(
                                    clipData = ClipData.newPlainText("down", "Down")
                                )
                            }
                        ),
                    tint = Color.Unspecified
                )
            }
        }

        // Animated turtle
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart
        ) {
            AnimatedTurtle(
                isPlaying = isPlaying,
                gridPosition = gridPosition,
                cellSizePx = cellSizePx,
                topPaddingPx = topPaddingPx,
                onAnimationComplete = onAnimationComplete
            )
        }
    }
}


@Composable
fun GridCanvas(
    gridConfig: GridConfig,
    paths: List<PathConfig>,
    starIcon: Painter
) {
    val density = LocalDensity.current
    val cellSizePx = with(density) { gridConfig.cellSize.toPx() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp)
    ) {
        // Draw grid lines
        for (i in 0..gridConfig.cols) {
            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(i * cellSizePx, 0f),
                end = Offset(i * cellSizePx, size.height),
                strokeWidth = 1.dp.toPx()
            )
        }
        for (i in 0..gridConfig.rows) {
            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(0f, i * cellSizePx),
                end = Offset(size.width, i * cellSizePx),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw paths and stars
        paths.forEach { path ->
            val startPx = Offset(
                x = path.startCell.x * cellSizePx + cellSizePx / 2,
                y = path.startCell.y * cellSizePx + cellSizePx / 2
            )
            val endPx = Offset(
                x = path.endCell.x * cellSizePx + cellSizePx / 2,
                y = path.endCell.y * cellSizePx + cellSizePx / 2
            )

            drawLine(
                color = Color(0xFF2096f3),
                start = startPx,
                end = endPx,
                strokeWidth = 8.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Draw stars along the path
            val dx = endPx.x - startPx.x
            val dy = endPx.y - startPx.y
            val distance = sqrt(dx * dx + dy * dy)
            val stepX = dx / distance * cellSizePx
            val stepY = dy / distance * cellSizePx

            var x = startPx.x
            var y = startPx.y
            val iconSizePx = cellSizePx * 0.6f

            while (sqrt((x - startPx.x).pow(2) + (y - startPx.y).pow(2)) <= distance) {
                drawIntoCanvas { canvas ->
                    canvas.save()
                    canvas.translate(x - iconSizePx / 2, y - iconSizePx / 2)
                    with(starIcon) {
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
}

@Composable
fun AnimatedTurtle(
    isPlaying: Int,
    gridPosition: IntOffset,
    cellSizePx: Int,
    topPaddingPx: Int,
    onAnimationComplete: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")

    // Convert grid position to pixel position (center of cell)
    val currentPixelPos = IntOffset(
        x = gridPosition.x * cellSizePx + cellSizePx / 2,
        y = gridPosition.y * cellSizePx + cellSizePx / 2 + topPaddingPx
    )

    val targetPixelPos = when (isPlaying) {
        0 -> currentPixelPos
        1 -> currentPixelPos + IntOffset(0, -cellSizePx)  // up
        2 -> currentPixelPos + IntOffset(0, cellSizePx)   // down
        3 -> currentPixelPos + IntOffset(-cellSizePx, 0)  // left
        else -> currentPixelPos + IntOffset(cellSizePx, 0) // right
    }

    val pOffset by animateIntOffsetAsState(
        targetValue = targetPixelPos,
        animationSpec = tween(1500, easing = LinearEasing),
        label = "position",
        finishedListener = { onAnimationComplete() }
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
            .offset { pOffset - IntOffset(24.dp.value.toInt(), 24.dp.value.toInt()) }
            .rotate(rotation),
        tint = Color.Unspecified
    )
}

@Composable
fun TiledBackground(tileBitmap: ImageBitmap) {
    Canvas(modifier = Modifier
        .fillMaxSize()
        .padding(top = 100.dp)
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