package com.asana.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue
import kotlin.math.sin

private val ListeningBlue = Color(0xFF3B82F6)
private val ListeningBlueDark = Color(0xFF1D4ED8)

@Composable
fun VoiceRecordingScreen(isListening: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "voiceAnim")

    // Pulse rings
    val ring1 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseOutCubic), RepeatMode.Restart),
        label = "ring1"
    )
    val ring2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(1200, 400, easing = EaseOutCubic), RepeatMode.Restart),
        label = "ring2"
    )
    val ring3 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(1200, 800, easing = EaseOutCubic), RepeatMode.Restart),
        label = "ring3"
    )

    // Waveform animation — single phase drives all 32 bars via sine
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart),
        label = "wave"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background glow
        Box(
            modifier = Modifier
                .size(360.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ListeningBlue.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ripple rings + mic button
            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(ring3)
                        .background(ListeningBlue.copy(alpha = 0.08f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(ring2)
                        .background(ListeningBlue.copy(alpha = 0.12f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(ring1)
                        .background(ListeningBlue.copy(alpha = 0.18f), CircleShape)
                )

                // Mic button
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(ListeningBlue, ListeningBlueDark)
                            ),
                            CircleShape
                        )
                        .semantics { contentDescription = "Listening, release to send" },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎤", fontSize = 44.sp)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Waveform bars (32 bars driven by sine wave)
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(64.dp)
            ) {
                for (i in 0 until 32) {
                    val heightFraction = ((sin(wavePhase + i * 0.4f) + 1f) / 2f)
                    val barHeightDp = (6 + heightFraction * 50).dp
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(barHeightDp)
                            .background(
                                ListeningBlue.copy(alpha = 0.6f + heightFraction * 0.4f),
                                CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (isListening) "Listening..." else "Processing...",
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Release to send",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }

        // Top label
        Text(
            text = "🧘 Asana",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
        )
    }
}
