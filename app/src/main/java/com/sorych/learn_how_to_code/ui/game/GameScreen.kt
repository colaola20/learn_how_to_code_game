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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sorych.learn_how_to_code.R
import com.sorych.learn_how_to_code.ui.theme.Learn_how_to_codeTheme
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt



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
    val density = LocalDensity.current
    // Top padding in DP for UI elements above the grid (if any)
    val topPaddingDp = 100.dp

    // Animation state
    var isPlaying by remember { mutableStateOf(false) }
    var currentPathIndex by remember { mutableIntStateOf(0) }
    var playerSequence by remember { mutableStateOf<List<Int>>(emptyList()) }
    var showError by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    // Get the correct sequence from the first path
    val correctSequence = levelConfig.paths.map { it.correctSequence }
    val startingGridPosition = levelConfig.paths.firstOrNull()?.startCell ?: IntOffset(0, 0)
    // Current position - this is the TARGET position for the turtle
    var gridPosition by remember(levelConfig) { mutableStateOf(startingGridPosition) }

    // Animate through paths sequentially
    LaunchedEffect(isPlaying, currentPathIndex) {
        if (isPlaying && currentPathIndex < levelConfig.paths.size) {
            delay(1000)
            val currentPath = levelConfig.paths[currentPathIndex]

            // After animation, update position to end of current path
            gridPosition = currentPath.endCell

            // Move to next path
            val nextIndex = currentPathIndex + 1

            // Check if all paths are complete
            if (nextIndex >= levelConfig.paths.size) {
                isPlaying = false
                currentPathIndex = 0
                showSuccess = true
            } else {
                currentPathIndex = nextIndex
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(levelConfig.backgroundColor)
    ) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight - topPaddingDp
        val gridWidthPx = with(density) { availableWidth.toPx() }
        val gridHeightPx = with(density) { availableHeight.toPx() }

        // Calculate cell size based on available space
        // Use min() to ensure cells are PERFECT SQUARES that fit in available space
        val cellSizePx = remember(gridWidthPx, gridHeightPx) {
            val widthPerCell = gridWidthPx / gridConfig.cols
            val heightPerCell = gridHeightPx / gridConfig.rows
            min(widthPerCell, heightPerCell)
        }

        val gridTotalWidth = cellSizePx * gridConfig.cols
        val gridTotalHeight = cellSizePx * gridConfig.rows

        // Center the grid horizontally
        val gridOffsetXPx = (gridWidthPx - gridTotalWidth) / 2
        val gridOffsetYPx = (gridHeightPx - gridTotalHeight) / 2f

        val tileBitmap = remember(cellSizePx) {
            val tileSizePx = cellSizePx
            val drawable = AppCompatResources.getDrawable(context, R.drawable.sea_waves2)!!
            drawable.toBitmap(
                width = tileSizePx.toInt(),
                height = tileSizePx.toInt(),
                config = Bitmap.Config.ARGB_8888
            ).asImageBitmap()
        }

        val icon: Painter = painterResource(id = R.drawable.starfish)
        TiledBackground(
            tileBitmap = tileBitmap
        )

        GridCanvas(
            gridConfig = gridConfig,
            paths = levelConfig.paths,
            starIcon = icon,
            cellSizePx = cellSizePx,
            gridOffsetXPx = gridOffsetXPx,
            gridOffsetYPx = gridOffsetYPx
        )

        AnimatedTurtle(
            isPlaying = isPlaying,
            currentPathIndex = currentPathIndex,
            paths = levelConfig.paths,
            gridPosition = gridPosition,
            cellSizePx = cellSizePx,
            gridOffsetXPx = gridOffsetXPx,
            gridOffsetYPx = gridOffsetYPx
        )

        // UI Layer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10f)
        ) {

            GameControls(
                paths = levelConfig.paths,
                onPlayClicked = { sequence ->
                    // Validate the sequence
                    if (sequence.size == correctSequence.size &&
                        sequence == correctSequence) {
                        // Correct! Start animation
                        playerSequence = sequence
                        isPlaying = true
                        currentPathIndex = 0
                        showError = false
                        showSuccess = false
                        gridPosition = startingGridPosition // Reset to start
                    } else {
                        // Wrong sequence
                        showError = true
                        showSuccess = false
                    }
                }
            )

        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Error message - top of the screen
            AnimatedVisibility(
                visible = showError,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "Incorrect sequence! Try again.",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 40.dp)
                        .background(
                            color = Color(0xFFE53935), // deep red
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                )
                // Auto-hide after 3 seconds
                LaunchedEffect(showError) {
                    delay(3000)
                    showError = false
                }
            }

            // Success message - center overlay
            AnimatedVisibility(
                visible = showSuccess,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Text(
                    text = "Success! Level Complete! 🎉",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .background(
                            color = Color(0xFF43A047), // deep green
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .shadow(6.dp, RoundedCornerShape(16.dp))
                )
                // Auto-hide after 3 seconds
                LaunchedEffect(showSuccess) {
                    delay(3000)
                    showSuccess = false
                }

            }
        }
    }
}


@Composable
fun GameControls(
    paths: List<PathConfig>, // Pass all paths to know how many boxes and correct sequences
    onPlayClicked: (List<Int>) -> Unit, // Callback with player's sequence
    modifier: Modifier = Modifier
) {
    // Store a map of box index to direction (1=up, 2=down, 3=left, 4=right)
    var droppedArrows by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Row(modifier = Modifier.fillMaxWidth()) {
            // Drop zones
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(8.dp)
            ) {
                repeat(paths.size) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .size(80.dp)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFf1d6bd))  // light blue-gray background
                            .border(2.dp, Color(0xFF3F51B5), RoundedCornerShape(12.dp))
                            .dragAndDropTarget(
                                shouldStartDragAndDrop = { event ->
                                    event
                                        .mimeTypes()
                                        .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                                },
                                target = remember {
                                    object : DragAndDropTarget {
                                        override fun onDrop(event: DragAndDropEvent): Boolean {

                                            // Extract the dropped arrow type from ClipData
                                            val item = event.toAndroidDragEvent().clipData.getItemAt(0)
                                            val droppedText = item.text.toString()

                                            // Map arrow text to direction
                                            val direction = when (droppedText.lowercase()) {
                                                "up" -> 1  // up
                                                "down" -> 2  // down
                                                "left" -> 3  // left
                                                "right" -> 4  // right
                                                else -> -1
                                            }
                                            // Add this arrow to the map at this index
                                            droppedArrows = droppedArrows + (index to direction)

                                            return true
                                        }
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Check if this box has an arrow dropped in it
                        val arrowDirection = droppedArrows[index]

                        this@Row.AnimatedVisibility(
                            visible = arrowDirection != null,
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            // Show the correct arrow based on what was dropped
                            val iconRes = when (arrowDirection) {
                                1 -> R.drawable.up
                                2 -> R.drawable.down
                                3 -> R.drawable.left
                                4 -> R.drawable.right
                                else -> R.drawable.right // fallback
                            }

                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = "Dropped Arrow",
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
                                    clipData = ClipData.newPlainText("arrow", "left")
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
                                    clipData = ClipData.newPlainText("arrow", "up")
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
                                    clipData = ClipData.newPlainText("arrow", "right")
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
                                    clipData = ClipData.newPlainText("arrow", "down")
                                )
                            }
                        ),
                    tint = Color.Unspecified
                )
                // Play Button
                Button(
                    onClick = {
                        // Convert map to ordered list
                        val playerSequence = (0 until paths.size).mapNotNull { index ->
                            droppedArrows[index]
                        }
                        onPlayClicked(playerSequence)
                    },
                    contentPadding = PaddingValues(5.dp), // Remove default padding
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = 2.dp,
                            color = Color(0xFF3f51b5),
                            shape = CircleShape
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3f51b5) // background color
                    ),

                    ) {
                    Icon(
                        painter = painterResource(R.drawable.play),
                        tint = Color(0xFFf1d6bd),
                        contentDescription = "Play icon",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun GridCanvas(
    gridConfig: GridConfig,
    paths: List<PathConfig>,
    starIcon: Painter,
    cellSizePx: Float,
    gridOffsetXPx: Float,
    gridOffsetYPx: Float
) {

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp)
    ) {
        val cellSize = min(
            size.width /gridConfig.cols,
            size.height / gridConfig.rows
        )

        // Use draw scope translate so everything inside is relative to grid top-left
        translate(left = gridOffsetXPx, top = gridOffsetYPx) {
            val width = cellSizePx * gridConfig.cols
            val height = cellSizePx * gridConfig.rows

            // Draw grid lines
            for (i in 0..gridConfig.cols) {
                drawLine(
                    color = Color.White.copy(alpha = 0.3f),
                    start = Offset(i * cellSizePx, 0f),
                    end = Offset(i * cellSizePx, height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            for (i in 0..gridConfig.rows) {
                drawLine(
                    color = Color.White.copy(alpha = 0.3f),
                    start = Offset(0f, i * cellSizePx),
                    end = Offset(width, i * cellSizePx),
                    strokeWidth = 1.dp.toPx()
                )
            }


            // Helper: center of a cell (in grid local coordinates)
            fun cellCenterPx(cellX: Int, cellY: Int): Offset {
                return Offset(
                    x = cellX * cellSizePx + cellSizePx / 2f,
                    y = cellY * cellSizePx + cellSizePx / 2f
                )
            }

            // Draw paths and stars
            paths.forEach { path ->
                // Convert start/end cell -> center pixel positions (in grid-local coords)
                val startCenter = cellCenterPx(path.startCell.x, path.startCell.y)
                val endCenter = cellCenterPx(path.endCell.x, path.endCell.y)

                drawLine(
                    color = Color(0xFF2096f3),
                    start = startCenter,
                    end = endCenter,
                    strokeWidth = 8.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Determine steps (number of cells between start and end, inclusive)
                val dxCells = path.endCell.x - path.startCell.x
                val dyCells = path.endCell.y - path.startCell.y
                val steps = maxOf(kotlin.math.abs(dxCells), kotlin.math.abs(dyCells))
                if (steps == 0) {
                    // single-cell path: draw a star at start
                    drawIntoCanvas { canvas ->
                        canvas.save()
                        canvas.translate(
                            startCenter.x - cellSizePx * 0.5f,
                            startCenter.y - cellSizePx * 0.5f
                        )
                        with(starIcon) {
                            draw(size = Size(cellSizePx, cellSizePx), alpha = 0.85f)
                        }
                        canvas.restore()
                    }
                } else {
                    // step along the path using linear interpolation between centers
                    for (i in 0..steps) {
                        val t = i.toFloat() / steps.toFloat() // 0..1
                        val xPx = startCenter.x + (endCenter.x - startCenter.x) * t
                        val yPx = startCenter.y + (endCenter.y - startCenter.y) * t

                        drawIntoCanvas { canvas ->
                            canvas.save()
                            // translate so star's top-left aligns properly (centered in cell)
                            canvas.translate(xPx - (cellSizePx * 0.5f), yPx - (cellSizePx * 0.5f))
                            with(starIcon) {
                                draw(size = Size(cellSizePx, cellSizePx), alpha = 0.85f)
                            }
                            canvas.restore()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedTurtle(
    isPlaying: Boolean,
    currentPathIndex: Int,
    paths: List<PathConfig>,
    gridPosition: IntOffset,
    cellSizePx: Float,
    gridOffsetXPx: Float,
    gridOffsetYPx: Float
) {
    // Convert cell size to Dp
    val turtleSizeDp = with(LocalDensity.current) { cellSizePx.toDp() }
    val turtleSizePx = cellSizePx
    val turtleHalf = turtleSizePx / 2f

    // Determine the target position based on animation state
    val targetGridPosition = if (isPlaying && currentPathIndex < paths.size) {
        // Currently animating - move to end of current path
        paths[currentPathIndex].endCell
    } else {
        // Not animating - stay at current position
        gridPosition
    }

    // Calculate current center position in GRID-RELATED coordinates
    val targetPixelPos = Offset(
        x = gridOffsetXPx + gridPosition.x * cellSizePx + cellSizePx / 2f,
        y = gridOffsetYPx + gridPosition.y * cellSizePx + cellSizePx / 2f
    )

    // Smooth animation for position
    val animatedOffset by animateIntOffsetAsState(
        targetValue = IntOffset(
            targetPixelPos.x.toInt(),
            targetPixelPos.y.toInt()
        ),
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "turtle position"
    )

    // Turtle continuous rotation
    val infinite = rememberInfiniteTransition()
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(3000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(top = 100.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.turtle),
            contentDescription = "Animated Turtle",
            modifier = Modifier
                .size(turtleSizeDp)
                .offset {
                    // Convert center position to top-left
                    IntOffset(
                        animatedOffset.x - turtleHalf.toInt(),
                        animatedOffset.y - turtleHalf.toInt()
                    )
                }
                .rotate(rotation)
                .zIndex(5f),
            tint = Color.Unspecified
        )
    }
}

@Composable
fun TiledBackground(
    tileBitmap: ImageBitmap
) {
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