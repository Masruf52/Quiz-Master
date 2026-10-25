package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
fun QuizPlayScreen(
    viewModel: QuizViewModel,
    onNavigateToResult: () -> Unit,
    onBackToHome: () -> Unit
) {
    val quiz by viewModel.activeQuiz.collectAsState()
    val questions by viewModel.activeQuestions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedAnswer by viewModel.selectedAnswer.collectAsState()
    val isAnswered by viewModel.isAnswered.collectAsState()
    val isCorrect by viewModel.isCorrect.collectAsState()
    val secondsRemaining by viewModel.secondsRemaining.collectAsState()
    val isPaused by viewModel.isQuizPaused.collectAsState()
    val bookmarks by viewModel.bookmarkedQuestions.collectAsState()
    val lifeline5050Used by viewModel.lifeline5050Used.collectAsState()
    val extraTimeUsed by viewModel.extraTimeUsed.collectAsState()
    val removedOptions by viewModel.removedOptions.collectAsState()
    val quizCompleted by viewModel.quizCompleted.collectAsState()
    val totalScore by viewModel.quizScore.collectAsState()

    // Monitor for completion
    if (quizCompleted) {
        onNavigateToResult()
    }

    val currentQuestion = questions.getOrNull(currentIndex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (currentQuestion == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading Questions...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(top = 32.dp)
            ) {
                // Header Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBackToHome) {
                        Icon(Icons.Default.Close, contentDescription = "Exit")
                    }

                    Text(
                        text = quiz?.name ?: "Quiz",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )

                    Row {
                        IconButton(onClick = { viewModel.toggleBookmark(currentQuestion.questionId) }) {
                            Icon(
                                imageVector = if (bookmarks.contains(currentQuestion.questionId)) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (bookmarks.contains(currentQuestion.questionId)) GoldAccent else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { viewModel.togglePauseQuiz() }) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar
                val progress = (currentIndex.toFloat() / questions.size.toFloat())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Question ${currentIndex + 1}/${questions.size}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Timer & Score indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = "Timer",
                            tint = if (secondsRemaining <= 5) WrongRed else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${secondsRemaining}s",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = if (secondsRemaining <= 5) WrongRed else MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Text(
                        text = "Score: $totalScore",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Question Card & Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Question container
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Custom decorations for quiz formats
                            when (currentQuestion.type) {
                                "image_quiz" -> {
                                    Icon(
                                        Icons.Default.Image,
                                        contentDescription = "Image Quiz Decoration",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .padding(bottom = 10.dp)
                                    )
                                }
                                "audio_quiz" -> {
                                    Icon(
                                        Icons.Default.Hearing,
                                        contentDescription = "Audio Quiz Playback",
                                        tint = GoldAccent,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .padding(bottom = 10.dp)
                                    )
                                    Text("[Simulated Audio Prompt Playing]", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                }
                            }

                            Text(
                                text = currentQuestion.question,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Inputs / Option Builders
                    when (currentQuestion.type) {
                        "fill_blank" -> {
                            FillBlankLayout(
                                isAnswered = isAnswered,
                                correctAnswer = currentQuestion.correctAnswer,
                                onSubmit = { viewModel.submitAnswer(it) }
                            )
                        }
                        "true_false" -> {
                            TrueFalseLayout(
                                isAnswered = isAnswered,
                                selectedAnswer = selectedAnswer,
                                correctAnswer = currentQuestion.correctAnswer,
                                onSelect = { viewModel.submitAnswer(it) }
                            )
                        }
                        else -> {
                            // MCQ, Image Quiz, Audio Quiz all use segment option grids
                            MultipleChoiceLayout(
                                options = currentQuestion.options.split(","),
                                isAnswered = isAnswered,
                                selectedAnswer = selectedAnswer,
                                correctAnswer = currentQuestion.correctAnswer,
                                removedOptions = removedOptions,
                                onSelect = { viewModel.submitAnswer(it) }
                            )
                        }
                    }

                    // Explanation Card visible after answer
                    if (isAnswered) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCorrect == true) CorrectGreen.copy(alpha = 0.08f) else WrongRed.copy(alpha = 0.08f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (isCorrect == true) CorrectGreen else WrongRed
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isCorrect == true) Icons.Default.CheckCircle else Icons.Default.Close,
                                        contentDescription = "Status",
                                        tint = if (isCorrect == true) CorrectGreen else WrongRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isCorrect == true) "Correct Answer!" else "Incorrect. Correct is: ${currentQuestion.correctAnswer}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isCorrect == true) CorrectGreen else WrongRed
                                    )
                                }
                                if (currentQuestion.explanation.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        currentQuestion.explanation,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Lifelines or Controls footer
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 12.dp))

                if (!isAnswered) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 50-50 Lifeline
                        Button(
                            onClick = { viewModel.useLifeline5050() },
                            enabled = !lifeline5050Used,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("50-50 Lifeline")
                        }

                        // Extra Time Lifeline
                        Button(
                            onClick = { viewModel.useExtraTime() },
                            enabled = !extraTimeUsed,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("+15s Time")
                        }
                    }
                } else {
                    Button(
                        onClick = { viewModel.nextQuestion() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("quiz_next_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = if (currentIndex + 1 < questions.size) "Next Question" else "Finish & View Results",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Pause/Resume Modal Overlay
        AnimatedVisibility(
            visible = isPaused,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Pause,
                            contentDescription = "Paused",
                            tint = GoldAccent,
                            modifier = Modifier.size(50.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Quiz Paused", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Timer has been frozen. You can resume play whenever you're ready.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 12.dp))

                        Button(
                            onClick = { viewModel.togglePauseQuiz() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resume Play", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                viewModel.forceResetQuizPlay()
                                onBackToHome()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WrongRed),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Exit Quiz", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MultipleChoiceLayout(
    options: List<String>,
    isAnswered: Boolean,
    selectedAnswer: String?,
    correctAnswer: String,
    removedOptions: Set<String>,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEachIndexed { idx, option ->
            val isRemoved = removedOptions.contains(option)
            if (!isRemoved) {
                val isSelected = selectedAnswer == option
                val isCorrectAnswer = option.lowercase() == correctAnswer.lowercase()

                val cardBg = when {
                    !isAnswered -> MaterialTheme.colorScheme.surface
                    isSelected && isCorrectAnswer -> CorrectGreen.copy(alpha = 0.15f)
                    isSelected && !isCorrectAnswer -> WrongRed.copy(alpha = 0.15f)
                    isCorrectAnswer -> CorrectGreen.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surface
                }

                val borderStroke = when {
                    !isAnswered && isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    !isAnswered -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    isSelected && isCorrectAnswer -> BorderStroke(2.dp, CorrectGreen)
                    isSelected && !isCorrectAnswer -> BorderStroke(2.dp, WrongRed)
                    isCorrectAnswer -> BorderStroke(2.dp, CorrectGreen)
                    else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardBg)
                        .clickable(enabled = !isAnswered) { onSelect(option) }
                        .border(borderStroke.width, borderStroke.color, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ('A'.code + idx).toChar().toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = option,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAnswered && !isCorrectAnswer && !isSelected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun TrueFalseLayout(
    isAnswered: Boolean,
    selectedAnswer: String?,
    correctAnswer: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        listOf("True", "False").forEach { value ->
            val isSelected = selectedAnswer == value
            val isCorrectAnswer = value.lowercase() == correctAnswer.lowercase()

            val cardBg = when {
                !isAnswered -> MaterialTheme.colorScheme.surface
                isSelected && isCorrectAnswer -> CorrectGreen.copy(alpha = 0.15f)
                isSelected && !isCorrectAnswer -> WrongRed.copy(alpha = 0.15f)
                isCorrectAnswer -> CorrectGreen.copy(alpha = 0.15f)
                else -> MaterialTheme.colorScheme.surface
            }

            val strokeColor = when {
                !isAnswered && isSelected -> MaterialTheme.colorScheme.primary
                !isAnswered -> MaterialTheme.colorScheme.outlineVariant
                isSelected && isCorrectAnswer -> CorrectGreen
                isSelected && !isCorrectAnswer -> WrongRed
                isCorrectAnswer -> CorrectGreen
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .clickable(enabled = !isAnswered) { onSelect(value) }
                    .border(1.5.dp, strokeColor, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun FillBlankLayout(
    isAnswered: Boolean,
    correctAnswer: String,
    onSubmit: (String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = textInput,
            onValueChange = { if (!isAnswered) textInput = it },
            placeholder = { Text("Type your answer here...") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isAnswered,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (!isAnswered) {
            Button(
                onClick = {
                    if (textInput.isNotBlank()) onSubmit(textInput.trim())
                },
                modifier = Modifier.fillMaxWidth(0.5f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Submit Answer", fontWeight = FontWeight.Bold)
            }
        }
    }
}

data class BorderStroke(val width: androidx.compose.ui.unit.Dp, val color: Color)
