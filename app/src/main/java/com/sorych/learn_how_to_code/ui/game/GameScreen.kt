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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.sorych.learn_how_to_code.R
import com.sorych.learn_how_to_code.ui.theme.Learn_how_to_codeTheme
import java.nio.file.Files.size

@Composable
fun GameScreen(modifier: Modifier) {
    val context = LocalContext.current
    val tilePainter = painterResource(R.drawable.sea_waves2)
    val tileSize =64.dp

    fun Float.dpToPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()
    val tileBitmap = remember {
        val drawable = AppCompatResources.getDrawable(context, R.drawable.sea_waves2)!!
        drawable.toBitmap(
            width = tileSize.value.dpToPx(context),
            height = tileSize.value.dpToPx(context),
            config = Bitmap.Config.ARGB_8888
        ).asImageBitmap()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF82d4fa)) // base color
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {

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

        // Your real UI content goes here
        Column(Modifier.fillMaxSize()) {
            Text("Hello!", Modifier.padding(20.dp))
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    Learn_how_to_codeTheme {
        GameScreen(modifier = Modifier)
    }
}