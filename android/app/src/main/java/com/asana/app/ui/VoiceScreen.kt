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

@Composable
fun VoiceRecordingScreen(isListening: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "voiceAnim")

    // 3 ripple rings with staggered delays
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A0533), Color(0xFF2D1B69))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ripple rings + mic button
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Ring 3 (outermost)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(ring3)
                        .background(AccentGreen.copy(alpha = 0.1f), CircleShape)
                )
                // Ring 2
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(ring2)
                        .background(AccentGreen.copy(alpha = 0.15f), CircleShape)
                )
                // Ring 1 (innermost)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(ring1)
                        .background(AccentGreen.copy(alpha = 0.2f), CircleShape)
                )

                // Mic button
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(AccentGreen, Color(0xFF047857))
                            ),
                            CircleShape
                        )
                        .semantics { contentDescription = "Listening, release to send" },
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎤", fontSize = 40.sp)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = if (isListening) "Listening..." else "Processing...",
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Release to send",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }

        // Top label
        Text(
            text = "🧘 Asana",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp)
        )
    }
}
