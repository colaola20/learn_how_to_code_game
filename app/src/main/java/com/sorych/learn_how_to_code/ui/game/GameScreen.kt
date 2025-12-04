import android.content.Context
import android.graphics.Bitmap
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sorych.learn_how_to_code.R
import com.sorych.learn_how_to_code.ui.game.GameViewModel
import com.sorych.learn_how_to_code.ui.game.PathConfig
import com.sorych.learn_how_to_code.ui.theme.Learn_how_to_codeTheme


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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(levelConfig.backgroundColor) // base color
    ) {
        TiledBackground(tileBitmap = tileBitmap)

        // Your real UI content goes here
        Column(Modifier.fillMaxSize()) {
            // Draw all paths for this level
            levelConfig.paths.forEach { pathConfig ->
                PathDrawer(
                    icon = icon,
                    pathConfig = pathConfig
                )
            }
        }
    }
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
                    colorFilter = ColorFilter.tint(Color(0xFF2096f3))
                )
            }
        }
    }
}


@Composable
fun PathDrawer(
    icon: Painter,
    pathConfig: PathConfig,
    iconSize: Dp = 48.dp,
    ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val start = Offset(x = size.width * pathConfig.startX, y = size.height * pathConfig.startY)
        val end = Offset(x = size.width * pathConfig.endX, y = size.height * pathConfig.endY)
        drawLine(
            color = pathConfig.pathColor,
            start = start,
            end = end,
            strokeWidth = pathConfig.strokeWidth,
            cap = StrokeCap.Round
        )

        // Calculate distance and direction
        val dx = end.x - start.x
        val dy = end.y - start.y
        val iconSizePx = iconSize.toPx()

        val distance = kotlin.math.sqrt(dx*dx + dy*dy)
        val stepX = dx / distance * pathConfig.iconGap
        val stepY = dy / distance * pathConfig.iconGap

        // Draw icons along the line
        var x = start.x
        var y = start.y
        while (kotlin.math.sqrt((x - start.x)*(x - start.x) + (y - start.y)*(y - start.y)) < distance) {
            drawIntoCanvas { canvas ->
                canvas.save()
                canvas.translate(x - iconSizePx/2, y - iconSizePx/2)

                with(icon) {
                    draw(
                        size = Size(iconSizePx, iconSizePx)
                    )
                }

                canvas.restore()
            }
            x += stepX
            y += stepY
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