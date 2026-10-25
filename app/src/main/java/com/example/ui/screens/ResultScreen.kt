package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Question
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.WrongRed
import com.example.ui.viewmodel.QuizViewModel

@Composable
fun ResultScreen(
    viewModel: QuizViewModel,
    onRetry: () -> Unit,
    onBackHome: () -> Unit
) {
    val quiz by viewModel.activeQuiz.collectAsState()
    val questions by viewModel.activeQuestions.collectAsState()
    val correctCount by viewModel.quizCorrectAnswers.collectAsState()
    val wrongCount by viewModel.quizWrongAnswers.collectAsState()
    val score by viewModel.quizScore.collectAsState()
    val coinsEarned by viewModel.coinsEarned.collectAsState()
    val xpEarned by viewModel.xpEarned.collectAsState()
    val timeTaken by viewModel.timeTaken.collectAsState()

    val totalQuestions = questions.size
    val accuracy = if (totalQuestions > 0) (correctCount.toDouble() / totalQuestions) * 100 else 0.0

    // Stars rating evaluation
    val stars = when {
        accuracy >= 90.0 -> 3
        accuracy >= 70.0 -> 2
        accuracy >= 40.0 -> 1
        else -> 0
    }

    var showReviews by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(top = 48.dp, bottom = 16.dp)
                .padding(horizontal = 16.dp)
        ) {
            Text("Quiz Finished", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stars illustration Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = "Success Trophy",
                            tint = GoldAccent,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (stars >= 2) "Congratulations!" else "Quiz Finished!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "You completed the '${quiz?.name ?: "Quiz"}'",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stars Display
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 1..3) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star",
                                tint = if (i <= stars) GoldAccent else Color.Gray.copy(alpha = 0.3f),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
            }

            // Results Details Grid Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Performance Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatResultItem(title = "Correct Answers", value = "$correctCount", sub = "/$totalQuestions", color = CorrectGreen, modifier = Modifier.weight(1f))
                        StatResultItem(title = "Incorrect Answers", value = "$wrongCount", sub = "/$totalQuestions", color = WrongRed, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatResultItem(title = "Accuracy Rate", value = "${String.format("%.0f", accuracy)}%", sub = "", modifier = Modifier.weight(1f))
                        StatResultItem(title = "Time Spent", value = "${timeTaken}s", sub = "", modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatResultItem(title = "XP Earned", value = "+$xpEarned XP", sub = "", color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                        StatResultItem(title = "Coins Earned", value = "+$coinsEarned", sub = "", color = GoldAccent, modifier = Modifier.weight(1f))
                    }
                }
            }

            // Interactive Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = "Retry")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Retry Quiz")
                }

                Button(
                    onClick = {
                        val shareText = "I scored $score in the ${quiz?.name ?: "Quiz"} on Quiz Master! accuracy: ${String.format("%.1f", accuracy)}%. Challenge me!"
                        Toast.makeText(context, "Shared: $shareText (Simulation)", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Score")
                }
            }

            Button(
                onClick = onBackHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Home, contentDescription = "Home")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Return to Dashboard", fontWeight = FontWeight.Bold)
            }

            // Review Questions Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showReviews = !showReviews },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = "Review", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Review Answers & Explanations", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(if (showReviews) "Collapse" else "Expand", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    AnimatedVisibility(visible = showReviews) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            questions.forEachIndexed { idx, q ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Q${idx + 1}: ${q.question}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Correct Option: ${q.correctAnswer}", color = CorrectGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        if (q.explanation.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Explanation: ${q.explanation}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatResultItem(title: String, value: String, sub: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    Card(
        modifier = modifier.padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color)
                if (sub.isNotEmpty()) {
                    Text(sub, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
