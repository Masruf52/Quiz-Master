package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.LeaderboardEntry
import com.example.data.model.Quiz
import com.example.data.model.User
import com.example.data.model.RewardActivity
import androidx.compose.material3.TextButton
import com.example.ui.theme.Bronze
import com.example.ui.theme.CorrectGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Silver
import com.example.ui.theme.WrongRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.SlateSecondary
import com.example.ui.theme.frostedCard
import com.example.ui.viewmodel.QuizViewModel
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ==========================================
// 1. HOME SCREEN
// ==========================================
@Composable
fun HomeScreen(
    viewModel: QuizViewModel,
    onNavigateToQuizPlay: (Quiz) -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToRewards: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val quizzes by viewModel.quizzes.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedDifficultyFilter by remember { mutableStateOf("All") }
    var showFilters by remember { mutableStateOf(false) }

    val filteredQuizzes = quizzes.filter { quiz ->
        (searchQuery.isBlank() || quiz.name.contains(searchQuery, ignoreCase = true) || quiz.description.contains(searchQuery, ignoreCase = true)) &&
        (selectedDifficultyFilter == "All" || quiz.difficulty.equals(selectedDifficultyFilter, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        HomeHeader(user = user)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar & Filter Toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search Quizzes...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_search_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showFilters = !showFilters },
                        modifier = Modifier
                            .size(52.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Filters Panel
            if (showFilters) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Difficulty Filter", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("All", "Easy", "Medium", "Hard", "Expert").forEach { diff ->
                                    val isSelected = selectedDifficultyFilter == diff
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                            .clickable { selectedDifficultyFilter = diff }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            diff,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Daily Challenge / Promotion Card
            item {
                DailyChallengeCard(
                    onPlayClick = {
                        val spaceQuiz = quizzes.find { it.quizId == "quiz_science_space" }
                        if (spaceQuiz != null) onNavigateToQuizPlay(spaceQuiz)
                    }
                )
            }

            // Browse Categories
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Categories", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    TextButton(onClick = onNavigateToCategories) {
                        Text("See All", color = MaterialTheme.colorScheme.primary)
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(categories) { cat ->
                        CategoryHomeItem(category = cat, onClick = onNavigateToCategories)
                    }
                }
            }

            // Live Spin Promotion Banner
            item {
                SpinWheelBanner(onActionClick = onNavigateToRewards)
            }

            // Available / Popular Quizzes
            item {
                Text(
                    text = if (searchQuery.isNotBlank() || selectedDifficultyFilter != "All") "Search Results" else "Popular Quizzes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (filteredQuizzes.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "No Results", modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No quizzes found", fontWeight = FontWeight.Bold)
                            Text("Try adjust search query or filters", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(filteredQuizzes) { quiz ->
                    QuizListItem(quiz = quiz, onPlay = onNavigateToQuizPlay)
                }
            }
        }
    }
}

@Composable
fun HomeHeader(user: User?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(top = 28.dp), // notch safety padding
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome Back",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = user?.name ?: "Guest",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Coins/XP Frosted Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .frostedCard(RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Default.Stars,
                    contentDescription = "Coins",
                    tint = GoldAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${user?.coins ?: 0}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Lvl ${user?.level ?: 1}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // User Initials Avatar
            val initials = (user?.name ?: "Guest").split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
            
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun DailyChallengeCard(onPlayClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF6750A4),
                            Color(0xFF4F378B)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .background(Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "DAILY CHALLENGE",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Cosmology & Space Quiz",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Complete today's cosmology challenge to earn a streak multiplier & +100 bonus coins!",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text("QUEST", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            Text("15 Qs", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("BONUS", fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            Text("+100 Coins", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onPlayClick,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD0BCFF),
                            contentColor = Color(0xFF381E72)
                        ),
                        modifier = Modifier.height(44.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text("Start Quiz", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Arrow", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHomeItem(category: Category, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .frostedCard(RoundedCornerShape(24.dp))
                .padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            val (iconBg, iconColor) = when (category.name.lowercase()) {
                "science" -> Pair(Color(0xFFFFD8E4), Color(0xFFC33C54))
                "art" -> Pair(Color(0xFFF7F2FA), Color(0xFF6750A4))
                "history" -> Pair(Color(0xFFE8DEF8), Color(0xFF381E72))
                else -> Pair(Color(0xFFEADDFF), Color(0xFF6750A4))
            }
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                val emoji = when (category.name.lowercase()) {
                    "science" -> "🧪"
                    "art" -> "🎨"
                    "history" -> "⚔️"
                    "sports" -> "⚽"
                    "geography" -> "🗺️"
                    else -> "🚀"
                }
                Text(emoji, fontSize = 20.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                category.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "12 Quizzes",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SpinWheelBanner(onActionClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onActionClick() }
            .shadow(2.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .frostedCard(RoundedCornerShape(24.dp))
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("LUCKY SPIN WHEEL", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 11.sp, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Spin & Win Free Coins!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
                Text("Try your luck daily to unlock premium badges & hint lifelines.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(GoldAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocalActivity, contentDescription = "Spin", tint = Color.Black)
            }
        }
    }
}

@Composable
fun QuizListItem(quiz: Quiz, onPlay: (Quiz) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay(quiz) }
            .shadow(1.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .frostedCard(RoundedCornerShape(20.dp))
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = "Quiz", tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(quiz.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                Text(quiz.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Difficulty tag
                    val diffColor = when (quiz.difficulty.lowercase()) {
                        "easy" -> CorrectGreen
                        "medium" -> GoldAccent
                        "hard" -> WrongRed
                        else -> Color.Magenta
                    }
                    Text(
                        quiz.difficulty,
                        color = diffColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .background(diffColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    // Timer tag
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, contentDescription = "Time", modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("${quiz.estimatedTime}m", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Play", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

// ==========================================
// 2. CATEGORIES SCREEN
// ==========================================
@Composable
fun CategoriesScreen(
    viewModel: QuizViewModel,
    onNavigateToQuizPlay: (Quiz) -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val quizzes by viewModel.quizzes.collectAsState()

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(categories) {
        if (selectedCategoryId == null && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().categoryId
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 16.dp)
                .padding(horizontal = 16.dp)
        ) {
            Text("Quiz Categories", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }

        // Horizontal Category Tabs
        ScrollableTabRow(
            selectedTabIndex = categories.indexOfFirst { it.categoryId == selectedCategoryId }.coerceAtLeast(0),
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            categories.forEach { cat ->
                Tab(
                    selected = selectedCategoryId == cat.categoryId,
                    onClick = { selectedCategoryId = cat.categoryId },
                    text = { Text(cat.name, fontWeight = FontWeight.Bold) }
                )
            }
        }

        val activeCategory = categories.find { it.categoryId == selectedCategoryId }
        val categoryQuizzes = quizzes.filter { it.categoryId == selectedCategoryId }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (activeCategory != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(
                            modifier = Modifier
                                .frostedCard(RoundedCornerShape(24.dp))
                                .padding(16.dp)
                        ) {
                            Text(activeCategory.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(activeCategory.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("Difficulty: ${activeCategory.difficulty}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Total Quizzes: ${categoryQuizzes.size}", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            if (categoryQuizzes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Quizzes in this category yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(categoryQuizzes) { quiz ->
                    QuizListItem(quiz = quiz, onPlay = onNavigateToQuizPlay)
                }
            }
        }
    }
}

// ==========================================
// 3. LEADERBOARD SCREEN
// ==========================================
@Composable
fun LeaderboardScreen(viewModel: QuizViewModel) {
    val leaderboard by viewModel.leaderboard.collectAsState()
    val currentTf by viewModel.leaderboardTimeframe.collectAsState()
    val currentSc by viewModel.leaderboardScope.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 12.dp)
                .padding(horizontal = 16.dp)
        ) {
            Text("Global Leaderboard", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }

        // Timeframe selector tabs
        TabRow(
            selectedTabIndex = when (currentTf) {
                "weekly" -> 0
                "monthly" -> 1
                else -> 2
            },
            containerColor = Color.Transparent
        ) {
            listOf("weekly" to "Weekly", "monthly" to "Monthly", "all_time" to "All Time").forEachIndexed { idx, pair ->
                Tab(
                    selected = currentTf == pair.first,
                    onClick = { viewModel.setLeaderboardFilters(pair.first, currentSc) },
                    text = { Text(pair.second, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        // Scope Selector Pilling
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("global" to "Global", "country" to "Country", "friends" to "Friends").forEach { pair ->
                val isSelected = currentSc == pair.first
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                        .clickable { viewModel.setLeaderboardFilters(currentTf, pair.first) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(pair.second, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // Top 3 Podium
        val top3 = leaderboard.take(3)
        val rest = leaderboard.drop(3)

        if (leaderboard.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No leaderboard records found.")
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                if (top3.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // 2nd Place
                        if (top3.size > 1) {
                            PodiumColumn(entry = top3[1], height = 110.dp, color = Silver, placement = "2")
                        } else {
                            Spacer(modifier = Modifier.width(90.dp))
                        }

                        // 1st Place
                        PodiumColumn(entry = top3[0], height = 140.dp, color = GoldAccent, placement = "1")

                        // 3rd Place
                        if (top3.size > 2) {
                            PodiumColumn(entry = top3[2], height = 90.dp, color = Bronze, placement = "3")
                        } else {
                            Spacer(modifier = Modifier.width(90.dp))
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(horizontal = 16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(rest) { idx, entry ->
                        LeaderboardRow(entry = entry, position = idx + 4)
                    }
                }
            }
        }
    }
}

@Composable
fun PodiumColumn(entry: LeaderboardEntry, height: androidx.compose.ui.unit.Dp, color: Color, placement: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(entry.name.take(2).uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(entry.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text("${entry.xp} XP", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(
                    Brush.verticalGradient(colors = listOf(color, color.copy(alpha = 0.4f))),
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("#$placement", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
        }
    }
}

@Composable
fun LeaderboardRow(entry: LeaderboardEntry, position: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .frostedCard(RoundedCornerShape(16.dp))
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$position",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(entry.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(entry.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(entry.country, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(
                text = "${entry.xp} XP",
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.sp
            )
        }
    }
}

// ==========================================
// 4. REWARDS SCREEN (Lucky Spin & Badges)
// ==========================================
@Composable
fun RewardsScreen(viewModel: QuizViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val spinResult by viewModel.luckySpinResult.collectAsState()

    var isSpinning by remember { mutableStateOf(false) }
    val spinAngle = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 12.dp)
                .padding(horizontal = 16.dp)
        ) {
            Text("Rewards & Gamification", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Spin wheel card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .frostedCard(RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Daily Lucky Wheel", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Spin once for 20 Coins. Win massive Coins or level-up XP!", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 12.dp))

                    // Simulated wheel canvas
                    Box(
                        modifier = Modifier.size(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(spinAngle.value)
                        ) {
                            val colors = listOf(GoldAccent, WrongRed, InfoBlue, CorrectGreen, Color.Magenta, Color.DarkGray)
                            val size = Size(size.width, size.height)
                            for (i in 0 until 6) {
                                drawArc(
                                    color = colors[i],
                                    startAngle = i * 60f,
                                    sweepAngle = 60f,
                                    useCenter = true,
                                    size = size
                                )
                            }
                            // draw central pin
                            drawCircle(color = Color.White, radius = 15.dp.toPx())
                        }
                        // Arrow pointing top
                        Box(
                            modifier = Modifier
                                .offset(y = (-90).dp)
                                .size(20.dp)
                                .background(Color.White, CircleShape)
                                .border(2.dp, Color.Black, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (!isSpinning) {
                                if ((user?.coins ?: 0) < 20) {
                                    Toast.makeText(context, "Insufficent Coins!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isSpinning = true
                                scope.launch {
                                    // Spin animation
                                    val spins = (3..6).random()
                                    val finalAngle = spins * 360f + (0..359).random().toFloat()
                                    spinAngle.animateTo(
                                        targetValue = finalAngle,
                                        animationSpec = tween(
                                            durationMillis = 3000,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                    viewModel.spinWheel()
                                    isSpinning = false
                                }
                            }
                        },
                        enabled = !isSpinning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isSpinning) "Spinning..." else "SPIN (Cost: 20 Coins)", fontWeight = FontWeight.Bold)
                    }

                    if (spinResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Congrats! You won ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("$spinResult!", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = { viewModel.clearSpinResult() }) {
                                    Text("OK", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Daily Login Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .frostedCard(RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Daily Reward Bonus", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Claim +50 Coins and +50 XP once daily.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = { viewModel.earnDailyLoginBonus() },
                        colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen)
                    ) {
                        Text("Claim", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Achievements Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .frostedCard(RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Text("Achievement Badges", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))

                    val badgesList = listOf(
                        Triple("first_quiz", "First Blood", "Complete 1 single quiz successfully."),
                        Triple("sharp_shooter", "Sharp Shooter", "Get 10 correct answers in total."),
                        Triple("level_3", "Knowledge Master", "Reach Level 3 profile status."),
                        Triple("hot_streak", "Hot Streak", "Achieve a 3-win streak.")
                    )

                    val userBadges = user?.achievements?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        badgesList.forEach { badge ->
                            val unlocked = userBadges.contains(badge.first)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (unlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(1.dp, if (unlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(if (unlocked) GoldAccent else Color.Gray.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.MilitaryTech, contentDescription = badge.second, tint = if (unlocked) Color.Black else Color.Gray)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(badge.second, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (unlocked) MaterialTheme.colorScheme.onSurface else Color.Gray)
                                    Text(badge.third, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. PROFILE SCREEN
// ==========================================
@Composable
fun ProfileScreen(
    viewModel: QuizViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToRewardsLog: () -> Unit = {},
    onLogoutSuccess: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val history by viewModel.userHistory.collectAsState()
    val rewardActivities by viewModel.rewardActivities.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editCountry by remember { mutableStateOf("") }
    var showRewardsLog by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        user?.let {
            editName = it.name
            editCountry = it.country
        }
    }

    if (showRewardsLog) {
        RewardsLogScreen(
            viewModel = viewModel,
            onBack = { showRewardsLog = false }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 16.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("User Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Row {
                        IconButton(
                            onClick = {
                                showRewardsLog = true
                                onNavigateToRewardsLog()
                            }
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = "Rewards Log",
                                tint = GoldAccent
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Notifications, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier
                            .frostedCard(RoundedCornerShape(24.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar Placeholder
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (user?.name ?: "P").take(2).uppercase(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (!isEditing) {
                            Text(user?.name ?: "Loading...", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(user?.email ?: "email@domain.com", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(user?.country ?: "Global", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { isEditing = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Edit Profile", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Display Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = editCountry,
                                onValueChange = { editCountry = it },
                                label = { Text("Country") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        viewModel.updateProfile(editName, editCountry, "")
                                        isEditing = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CorrectGreen)
                                ) {
                                    Text("Save")
                                }
                                Button(
                                    onClick = { isEditing = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = WrongRed)
                                ) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }

                // XP Level Meter Card with Smooth Animated Progress Bar Transition
                val currentXp = user?.xp ?: 0
                val currentLevel = user?.level ?: 1
                val xpInCurrentLevel = (currentXp % 500).coerceAtLeast(0)
                val xpTargetForLevel = 500
                val rawProgress = (xpInCurrentLevel.toFloat() / xpTargetForLevel.toFloat()).coerceIn(0f, 1f)

                // Smooth animated progress bar transition for XP meter
                val animatedProgress by animateFloatAsState(
                    targetValue = rawProgress,
                    animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                    label = "xp_meter_progress_anim"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(3.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier
                            .frostedCard(RoundedCornerShape(24.dp))
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color(0x26EAB308), CircleShape)
                                        .border(1.5.dp, GoldAccent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Level",
                                        tint = GoldAccent,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Level $currentLevel",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "Next Tier: Level ${currentLevel + 1}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // XP Ratio Pill
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$xpInCurrentLevel / $xpTargetForLevel XP",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Smooth Animated Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedProgress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                SlateSecondary,
                                                GoldAccent
                                            )
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${(animatedProgress * 100).toInt()}% towards Level ${currentLevel + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(xpTargetForLevel - xpInCurrentLevel).coerceAtLeast(0)} XP needed",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Coins", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${user?.coins ?: 0} 🪙", fontSize = 15.sp, fontWeight = FontWeight.Black, color = GoldAccent)
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(28.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total XP", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${user?.xp ?: 0} ✨", fontSize = 15.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(28.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Win Streak", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${user?.winStreak ?: 0} 🔥", fontSize = 15.sp, fontWeight = FontWeight.Black, color = CorrectGreen)
                            }
                        }
                    }
                }

                // Chronological Rewards & Achievements Log Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(24.dp))
                        .clickable {
                            showRewardsLog = true
                            onNavigateToRewardsLog()
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier
                            .frostedCard(RoundedCornerShape(24.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color(0x26EC4899), CircleShape)
                                        .border(1.5.dp, Color(0xFFEC4899), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = "Rewards Log",
                                        tint = Color(0xFFEC4899),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Rewards & Achievements Log",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "Chronological history of rewards & level-ups",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open Log",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (rewardActivities.isEmpty()) {
                            Text(
                                text = "No recorded reward milestones yet. Complete quizzes or spins to start earning!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                rewardActivities.take(2).forEach { activity ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(
                                                        when (activity.type) {
                                                            "LEVEL_UP" -> GoldAccent
                                                            "QUIZ_REWARD" -> CorrectGreen
                                                            else -> MaterialTheme.colorScheme.primary
                                                        },
                                                        CircleShape
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = activity.title,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = activity.description,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        if (activity.coinsEarned > 0 || activity.xpEarned > 0) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                if (activity.coinsEarned > 0) {
                                                    Text("+${activity.coinsEarned}c", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                if (activity.xpEarned > 0) {
                                                    Text("+${activity.xpEarned}xp", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    showRewardsLog = true
                                    onNavigateToRewardsLog()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "View Full Activity Log (${rewardActivities.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

            // Statistics Grid Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .frostedCard(RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Text("Quiz Statistics", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))

                    val totalPlayed = user?.totalQuizPlayed ?: 0
                    val correct = user?.correctAnswers ?: 0
                    val wrong = user?.wrongAnswers ?: 0
                    val accuracy = if (totalPlayed > 0) {
                        (correct.toDouble() / (correct + wrong).coerceAtLeast(1)) * 100
                    } else 0.0

                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatBox(title = "Played", value = "$totalPlayed", modifier = Modifier.weight(1f))
                        StatBox(title = "Correct", value = "$correct", modifier = Modifier.weight(1f), color = CorrectGreen)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatBox(title = "Wrong", value = "$wrong", modifier = Modifier.weight(1f), color = WrongRed)
                        StatBox(title = "Accuracy", value = "${String.format("%.1f", accuracy)}%", modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatBox(title = "Streak", value = "${user?.winStreak ?: 0} 🔥", modifier = Modifier.weight(1f), color = GoldAccent)
                        StatBox(title = "Rank", value = "#${if (user?.rank == 0) "None" else user?.rank}", modifier = Modifier.weight(1f))
                    }
                }
            }

            // History Card List
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .frostedCard(RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Text("Recent Quiz Activity", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))

                    if (history.isEmpty()) {
                        Text("No completed quizzes yet. Start playing!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            history.take(5).forEach { record ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(record.quizName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Score: ${record.score} | Accuracy: ${String.format("%.0f", record.accuracy)}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("+${record.xpEarned} XP", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("+${record.coinsEarned} Coins", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Log out Button
            Button(
                onClick = {
                    viewModel.logout {
                        onLogoutSuccess()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WrongRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Log Out Account", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
}

@Composable
fun StatBox(title: String, value: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    Card(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .frostedCard(RoundedCornerShape(12.dp))
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color)
        }
    }
}
