package com.asana.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asana.app.ChatMessage
import com.asana.app.ChatViewModel

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onMicPressed: () -> Unit,
    onMicReleased: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCream)
    ) {
        // Top App Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(PrimaryPurple, Color(0xFF6D28D9))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🧘", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Asana",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Yoga Pose Guide",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message = message)
            }

            if (isLoading) {
                item { TypingIndicator() }
            }
        }

        // Input Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Message input field" },
                    placeholder = { Text("Ask about a yoga pose...", color = TextSecondary) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryPurple,
                        unfocusedBorderColor = BorderLavender,
                        focusedContainerColor = MutedLavender,
                        unfocusedContainerColor = MutedLavender
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(inputText.trim())
                            inputText = ""
                        }
                    }),
                    singleLine = true,
                    maxLines = 1
                )

                // Send button
                AnimatedVisibility(visible = inputText.isNotBlank()) {
                    FilledIconButton(
                        onClick = {
                            viewModel.sendMessage(inputText.trim())
                            inputText = ""
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .semantics { contentDescription = "Send message" },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = PrimaryPurple
                        )
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                    }
                }

                // Mic button — hold to speak
                AnimatedVisibility(visible = inputText.isBlank()) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AccentGreen)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        onMicPressed()
                                        tryAwaitRelease()
                                        onMicReleased()
                                    }
                                )
                            }
                            .semantics { contentDescription = "Hold to speak" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎤", fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(PrimaryPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🧘", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            if (!isUser && message.isImageLoading) {
                /*val shimmerAlpha by rememberInfiniteTransition(label = "imgShimmer").animateFloat(
                    initialValue = 0.2f, targetValue = 0.5f,
                    animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
                    label = "shimmerAlpha"
                )
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PrimaryPurple.copy(alpha = shimmerAlpha))
                        .padding(bottom = 6.dp)
                )*/
            } else {
                /*message.imageUrl?.let { url ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(url)
                            .decoderFactory(SvgDecoder.Factory())
                            .crossfade(true)
                            .build(),
                        contentDescription = "Yoga pose illustration",
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .padding(bottom = 6.dp)
                    )
                }*/
            }
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(if (isUser) UserBubble else AiBubble)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.content,
                    fontSize = 15.sp,
                    color = if (isUser) Color.White else TextPrimary,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    val dotAlpha by rememberInfiniteTransition(label = "typing").animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "dotAlpha"
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            modifier = Modifier.size(32.dp).background(PrimaryPurple, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text("🧘", fontSize = 14.sp) }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                .background(AiBubble)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text("● ● ●", fontSize = 14.sp, color = TextSecondary.copy(alpha = dotAlpha))
        }
    }
}
