package com.example.hakot

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.hakot.data.HakotRepository
import com.example.hakot.ui.theme.DarkSurfaceCard
import com.example.hakot.ui.theme.HakotTheme
import kotlinx.coroutines.delay
import org.maplibre.android.MapLibre

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        enableEdgeToEdge()
        setContent {
            HakotTheme {
                SplashScreen {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val context = LocalContext.current
    var startAnimation by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("Initializing Hakot System...") }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "AlphaAnimation"
    )

    LaunchedEffect(Unit) {
        startAnimation = true

        delay(300)
        loadingMessage = "Restoring Secure Session..."
        try {
            HakotRepository.restoreSession(context)
        } catch (_: Exception) {}

        delay(300)
        loadingMessage = "Pre-warming MapLibre GIS Engine..."
        try {
            MapLibre.getInstance(context.applicationContext)
        } catch (_: Exception) {}

        delay(300)
        loadingMessage = "Loading Municipal Waste Schedules..."
        try {
            val res = HakotRepository.fetchSchedulesFromSupabase(context)
            Log.d("HAKOT_SPLASH", "Fetch schedules result: $res, count=${HakotRepository.schedules.size}")
        } catch (e: Exception) {
            Log.e("HAKOT_SPLASH", "Error fetching schedules on launch", e)
        }

        delay(250)
        loadingMessage = "Ready for Operations!"
        delay(200)

        onTimeout()
    }

    // Strict 60-30-10 Palette: #1F513B Forest Green background (60%), Soft Sage White #F4F7F2, Safety Orange #F28C28
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkSurfaceCard),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .alpha(alphaAnim)
        ) {
            // Official Hakot Logo
            Image(
                painter = painterResource(id = R.drawable.hakot_logo),
                contentDescription = "Hakot Logo",
                modifier = Modifier.size(170.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Bold white uppercase title HAKOT
            Text(
                text = "HAKOT",
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle A Cleaner Tomorrow Starts Today
            Text(
                text = "A Cleaner Tomorrow Starts Today",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp,
                color = Color(0xFFF4F7F2), // Soft Sage White
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Dynamic loading prompt message in Safety Orange (#F28C28)
            Text(
                text = loadingMessage,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF28C28), // Safety Orange
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Continuous horizontal loading line with running brightness highlight (Shimmer effect) in Safety Orange (#F28C28)
            ShimmerLoadingLine()
        }
    }
}

@Composable
fun ShimmerLoadingLine(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "ShimmerTransition")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth(0.75f)
            .height(6.dp)
            .clip(CircleShape)
            .background(Color(0xFF0F2D20)) // Darker green track
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF28C28), // Safety Orange
                            Color(0xFFFED7AA), // Increasing white/hue brightness highlight
                            Color(0xFFF28C28)  // Safety Orange
                        ),
                        start = Offset(size.width * (shimmerOffset - 1f), 0f),
                        end = Offset(size.width * shimmerOffset, 0f)
                    )
                    onDrawBehind {
                        drawRect(brush)
                    }
                }
        )
    }
}
