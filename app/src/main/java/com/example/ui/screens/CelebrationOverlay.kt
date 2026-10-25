package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.*
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.frostedCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class CelebrationType {
    data class LevelUp(val level: Int, val oldLevel: Int) : CelebrationType()
    data class RewardsEarned(val coins: Int, val xp: Int, val message: String = "") : CelebrationType()
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CelebrationOverlay(
    celebration: CelebrationType?,
    onDismiss: () -> Unit,
    soundEnabled: Boolean = true
) {
    if (celebration == null) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable(enabled = true, onClick = onDismiss)
                .testTag("celebration_overlay_container"),
            contentAlignment = Alignment.Center
        ) {
            // Background Canvas Confetti Fallback (Robust Offline Effect)
            ConfettiCanvas()

            // Lottie Confetti Animation Overlay
            val composition by rememberLottieComposition(
                LottieCompositionSpec.Url("https://assets3.lottiefiles.com/packages/lf20_nouy4gqi.json")
            )
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever
            )

            if (composition != null) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(1.2f)
                )
            }

            // Central Interactive Premium Celebration Card
            var cardVisible by remember { mutableStateOf(false) }
            LaunchedEffect(celebration) {
                cardVisible = true
            }

            AnimatedVisibility(
                visible = cardVisible,
                enter = scaleIn(animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .widthIn(max = 380.dp)
                        .frostedCard(RoundedCornerShape(32.dp))
                        .clickable(enabled = false) {} // Prevent click-throughs dismissing card itself
                        .testTag("celebration_card"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Animated pulsing background glow for the Icon
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val scaleGlow by infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.15f,
                            animationSpec = infiniteRepeatable(
                                animation = twinSmoothTween(),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale"
                        )

                        when (celebration) {
                            is CelebrationType.LevelUp -> {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(110.dp)
                                        .graphicsLayer {
                                            scaleX = scaleGlow
                                            scaleY = scaleGlow
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(
                                                        GoldAccent.copy(alpha = 0.35f),
                                                        Color.Transparent
                                                    )
                                                ),
                                                CircleShape
                                            )
                                    )
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = "Level Up Trophy",
                                        tint = GoldAccent,
                                        modifier = Modifier.size(64.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "LEVEL UP!",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.5.sp
                                    ),
                                    color = GoldAccent,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Level ${celebration.oldLevel} → Level ${celebration.level}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "🚀 NEW UNLOCKS",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = GoldAccent
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "You earned +100 bonus coins!\nDouble lifeline capacity activated.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            is CelebrationType.RewardsEarned -> {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(110.dp)
                                        .graphicsLayer {
                                            scaleX = scaleGlow
                                            scaleY = scaleGlow
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                                        Color.Transparent
                                                    )
                                                ),
                                                CircleShape
                                            )
                                    )
                                    Icon(
                                        imageVector = Icons.Default.CardGiftcard,
                                        contentDescription = "Rewards Box",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(64.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "REWARDS EARNED!",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )

                                if (celebration.message.isNotEmpty()) {
                                    Text(
                                        text = celebration.message,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Coins Badge
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = Color(0x20FFD700)),
                                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Coins",
                                                tint = GoldAccent,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "+${celebration.coins} Coins",
                                                fontWeight = FontWeight.ExtraBold,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = GoldAccent
                                            )
                                        }
                                    }

                                    // XP Badge
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = Color(0x2000FFFF)),
                                        border = BorderStroke(1.dp, Color(0xFF00FFFF).copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "XP",
                                                tint = Color(0xFF00FFFF),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "+${celebration.xp} XP",
                                                fontWeight = FontWeight.ExtraBold,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color(0xFF00FFFF)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (celebration is CelebrationType.LevelUp) GoldAccent else MaterialTheme.colorScheme.primary,
                                contentColor = if (celebration is CelebrationType.LevelUp) Color.Black else Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("celebration_dismiss_button")
                        ) {
                            Text(
                                text = "Awesome!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Custom interpolation helper for pulse glowing
private fun twinSmoothTween(): TweenSpec<Float> {
    return tween(
        durationMillis = 1000,
        easing = FastOutSlowInEasing
    )
}

// Particle class for Canvas-based fallback Confetti animation
private data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var speedY: Float,
    var speedX: Float,
    val size: Float,
    val color: Color,
    var rotation: Float,
    val rotationSpeed: Float,
    val isSquare: Boolean
)

@Composable
fun ConfettiCanvas() {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val colors = listOf(
        Color(0xFFFFC107), // Yellow
        Color(0xFFFF5722), // Orange
        Color(0xFFE91E63), // Pink
        Color(0xFF4CAF50), // Green
        Color(0xFF00BCD4), // Cyan
        Color(0xFF9C27B0)  // Purple
    )

    val particles = remember {
        mutableStateListOf<ConfettiParticle>().apply {
            repeat(80) {
                add(
                    ConfettiParticle(
                        x = (0..100).random().toFloat() / 100f * screenWidthPx,
                        y = -(0..100).random().toFloat() / 100f * screenHeightPx,
                        speedY = (4..12).random().toFloat(),
                        speedX = (-3..3).random().toFloat(),
                        size = (15..35).random().toFloat(),
                        color = colors.random(),
                        rotation = (0..360).random().toFloat(),
                        rotationSpeed = (-8..8).random().toFloat(),
                        isSquare = (0..1).random() == 1
                    )
                )
            }
        }
    }

    // High performance frame loop updating particle configurations
    val infiniteTransition = rememberInfiniteTransition(label = "confetti_loop")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Trigger a side effect to update particle values safely
        particles.forEach { p ->
            p.y += p.speedY
            p.x += p.speedX
            p.rotation += p.rotationSpeed

            // Reset particle when it drops off screen
            if (p.y > size.height) {
                p.y = -p.size
                p.x = (0..100).random().toFloat() / 100f * size.width
                p.speedY = (4..12).random().toFloat()
            }

            rotate(p.rotation, pivot = Offset(p.x + p.size/2, p.y + p.size/2)) {
                if (p.isSquare) {
                    drawRect(
                        color = p.color,
                        topLeft = Offset(p.x, p.y),
                        size = Size(p.size, p.size)
                    )
                } else {
                    drawCircle(
                        color = p.color,
                        radius = p.size / 2,
                        center = Offset(p.x + p.size/2, p.y + p.size/2)
                    )
                }
            }
        }
    }
}
