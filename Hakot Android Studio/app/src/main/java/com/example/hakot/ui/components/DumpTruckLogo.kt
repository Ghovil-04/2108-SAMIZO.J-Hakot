package com.example.hakot.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.hakot.ui.theme.EcoGreenPrimary

@Composable
fun DumpTruckLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(130.dp, 75.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height

            // 3 Speed Motion Lines on Left
            drawLine(
                color = EcoGreenPrimary,
                start = Offset(width * 0.02f, height * 0.40f),
                end = Offset(width * 0.15f, height * 0.40f),
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = EcoGreenPrimary,
                start = Offset(width * 0.05f, height * 0.50f),
                end = Offset(width * 0.18f, height * 0.50f),
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = EcoGreenPrimary,
                start = Offset(width * 0.08f, height * 0.60f),
                end = Offset(width * 0.20f, height * 0.60f),
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )

            // 1. Green Cargo Compartment
            val bedPath = Path().apply {
                moveTo(width * 0.18f, height * 0.32f)
                lineTo(width * 0.68f, height * 0.32f)
                lineTo(width * 0.68f, height * 0.72f)
                lineTo(width * 0.18f, height * 0.72f)
                close()
            }
            drawPath(bedPath, color = EcoGreenPrimary)

            // Top Angled Compartment Lip
            val lipPath = Path().apply {
                moveTo(width * 0.18f, height * 0.32f)
                lineTo(width * 0.26f, height * 0.22f)
                lineTo(width * 0.68f, height * 0.22f)
                lineTo(width * 0.68f, height * 0.32f)
                close()
            }
            drawPath(lipPath, color = EcoGreenPrimary)

            // 2. Abstract Geometric White Logo Mark on Green Compartment
            val markPath1 = Path().apply {
                moveTo(width * 0.30f, height * 0.62f)
                lineTo(width * 0.38f, height * 0.38f)
                lineTo(width * 0.44f, height * 0.55f)
                close()
            }
            drawPath(markPath1, color = Color.White)

            val markPath2 = Path().apply {
                moveTo(width * 0.40f, height * 0.50f)
                lineTo(width * 0.47f, height * 0.35f)
                lineTo(width * 0.54f, height * 0.62f)
                close()
            }
            drawPath(markPath2, color = Color.White)

            // 3. White Truck Cabin
            val cabPath = Path().apply {
                moveTo(width * 0.68f, height * 0.38f)
                lineTo(width * 0.86f, height * 0.38f)
                quadraticTo(width * 0.94f, height * 0.38f, width * 0.96f, height * 0.50f)
                lineTo(width * 0.96f, height * 0.72f)
                lineTo(width * 0.68f, height * 0.72f)
                close()
            }
            drawPath(cabPath, color = Color.White)

            // Windshield Cutout
            val windshieldPath = Path().apply {
                moveTo(width * 0.74f, height * 0.44f)
                lineTo(width * 0.88f, height * 0.44f)
                lineTo(width * 0.92f, height * 0.58f)
                lineTo(width * 0.74f, height * 0.58f)
                close()
            }
            drawPath(windshieldPath, color = Color(0xFF0A0F18))

            // 4. Two Circular Wheels
            val wheelRadius = 13.dp.toPx()
            val innerRadius = 5.dp.toPx()

            // Rear Wheel
            drawCircle(color = Color(0xFF1D2939), radius = wheelRadius, center = Offset(width * 0.34f, height * 0.80f))
            drawCircle(color = Color.White, radius = innerRadius, center = Offset(width * 0.34f, height * 0.80f))

            // Front Wheel
            drawCircle(color = Color(0xFF1D2939), radius = wheelRadius, center = Offset(width * 0.84f, height * 0.80f))
            drawCircle(color = Color.White, radius = innerRadius, center = Offset(width * 0.84f, height * 0.80f))
        }
    }
}
