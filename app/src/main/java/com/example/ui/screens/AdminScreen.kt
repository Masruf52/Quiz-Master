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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WrongRed
import com.example.ui.viewmodel.QuizViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: QuizViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val categories by viewModel.categories.collectAsState()
    val quizzes by viewModel.quizzes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(top = 48.dp, bottom = 16.dp)
                .padding(horizontal = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.DeveloperMode, contentDescription = "Admin", tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Admin Control Panel", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
            }
        }

        // Dashboard Tabs
        TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.surface) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Categories", fontWeight = FontWeight.Bold, modifier = Modifier.padding(14.dp), fontSize = 13.sp)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Quizzes", fontWeight = FontWeight.Bold, modifier = Modifier.padding(14.dp), fontSize = 13.sp)
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("Questions", fontWeight = FontWeight.Bold, modifier = Modifier.padding(14.dp), fontSize = 13.sp)
            }
        }

        when (selectedTab) {
            0 -> AdminCategoriesTab(viewModel = viewModel)
            1 -> AdminQuizzesTab(viewModel = viewModel)
            2 -> AdminQuestionsTab(viewModel = viewModel)
        }
    }
}

// ==========================================
// A. CATEGORIES TAB
// ==========================================
@Composable
fun AdminCategoriesTab(viewModel: QuizViewModel) {
    val categories by viewModel.categories.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Add Form Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Add New Category", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (name.isBlank() || description.isBlank()) {
                            Toast.makeText(context, "Fields cannot be blank", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.adminAddCategory(name.trim(), description.trim())
                            name = ""
                            description = ""
                            Toast.makeText(context, "Category Added!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Category", fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("Current Categories", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(categories) { cat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(cat.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { viewModel.adminDeleteCategory(cat.categoryId) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = WrongRed)
                    }
                }
            }
        }
    }
}

// ==========================================
// B. QUIZZES TAB
// ==========================================
@Composable
fun AdminQuizzesTab(viewModel: QuizViewModel) {
    val categories by viewModel.categories.collectAsState()
    val quizzes by viewModel.quizzes.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Medium") }
    var estTime by remember { mutableStateOf("5") }

    var expandedCatDropdown by remember { mutableStateOf(false) }
    var expandedDiffDropdown by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(categories) {
        if (selectedCategoryId.isEmpty() && categories.isNotEmpty()) {
            selectedCategoryId = categories.first().categoryId
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .weight(1f, fill = false)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Add New Quiz", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                // Category Selection Spinner
                val activeCategory = categories.find { it.categoryId == selectedCategoryId }
                Box {
                    OutlinedTextField(
                        value = activeCategory?.name ?: "Select Category",
                        onValueChange = {},
                        label = { Text("Parent Category") },
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Drop") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedCatDropdown = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    DropdownMenu(
                        expanded = expandedCatDropdown,
                        onDismissRequest = { expandedCatDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategoryId = cat.categoryId
                                    expandedCatDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Quiz Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Quiz Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Difficulty Spinner
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = difficulty,
                            onValueChange = {},
                            label = { Text("Difficulty") },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Drop") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedDiffDropdown = true },
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        DropdownMenu(
                            expanded = expandedDiffDropdown,
                            onDismissRequest = { expandedDiffDropdown = false }
                        ) {
                            listOf("Easy", "Medium", "Hard", "Expert").forEach { diff ->
                                DropdownMenuItem(
                                    text = { Text(diff) },
                                    onClick = {
                                        difficulty = diff
                                        expandedDiffDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Estimated time input
                    OutlinedTextField(
                        value = estTime,
                        onValueChange = { estTime = it },
                        label = { Text("Est. Time (min)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Button(
                    onClick = {
                        val parsedTime = estTime.toIntOrNull()
                        if (name.isBlank() || description.isBlank() || parsedTime == null || selectedCategoryId.isEmpty()) {
                            Toast.makeText(context, "Invalid fields. Time must be a number.", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.adminAddQuiz(selectedCategoryId, name.trim(), description.trim(), difficulty, parsedTime)
                            name = ""
                            description = ""
                            Toast.makeText(context, "Quiz Created!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Quiz", fontWeight = FontWeight.Bold)
                }
            }
        }

        Text("Current Quizzes", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(quizzes) { quiz ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(quiz.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(quiz.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Difficulty: ${quiz.difficulty} | Time: ${quiz.estimatedTime}m", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { viewModel.adminDeleteQuiz(quiz.quizId) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = WrongRed)
                    }
                }
            }
        }
    }
}

// ==========================================
// C. QUESTIONS TAB
// ==========================================
@Composable
fun AdminQuestionsTab(viewModel: QuizViewModel) {
    val quizzes by viewModel.quizzes.collectAsState()

    var selectedQuizId by remember { mutableStateOf("") }
    var questionText by remember { mutableStateOf("") }
    var optionA by remember { mutableStateOf("") }
    var optionB by remember { mutableStateOf("") }
    var optionC by remember { mutableStateOf("") }
    var optionD by remember { mutableStateOf("") }
    var correctOption by remember { mutableStateOf("A") }
    var explanation by remember { mutableStateOf("") }

    var expandedQuizDropdown by remember { mutableStateOf(false) }
    var expandedCorrectDropdown by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(quizzes) {
        if (selectedQuizId.isEmpty() && quizzes.isNotEmpty()) {
            selectedQuizId = quizzes.first().quizId
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Add New MCQ Question", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                // Select Quiz
                val activeQuiz = quizzes.find { it.quizId == selectedQuizId }
                Box {
                    OutlinedTextField(
                        value = activeQuiz?.name ?: "Select Quiz",
                        onValueChange = {},
                        label = { Text("Parent Quiz") },
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Drop") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedQuizDropdown = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    DropdownMenu(
                        expanded = expandedQuizDropdown,
                        onDismissRequest = { expandedQuizDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        quizzes.forEach { qz ->
                            DropdownMenuItem(
                                text = { Text(qz.name) },
                                onClick = {
                                    selectedQuizId = qz.quizId
                                    expandedQuizDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Question Text") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = optionA,
                        onValueChange = { optionA = it },
                        label = { Text("Option A") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = optionB,
                        onValueChange = { optionB = it },
                        label = { Text("Option B") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = optionC,
                        onValueChange = { optionC = it },
                        label = { Text("Option C") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = optionD,
                        onValueChange = { optionD = it },
                        label = { Text("Option D") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Correct option picker
                Box {
                    OutlinedTextField(
                        value = "Option $correctOption",
                        onValueChange = {},
                        label = { Text("Correct Answer") },
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = "Drop") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedCorrectDropdown = true },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    DropdownMenu(
                        expanded = expandedCorrectDropdown,
                        onDismissRequest = { expandedCorrectDropdown = false }
                    ) {
                        listOf("A", "B", "C", "D").forEach { letter ->
                            DropdownMenuItem(
                                text = { Text("Option $letter") },
                                onClick = {
                                    correctOption = letter
                                    expandedCorrectDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Educational Explanation") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val correctValue = when (correctOption) {
                            "A" -> optionA
                            "B" -> optionB
                            "C" -> optionC
                            else -> optionD
                        }

                        if (questionText.isBlank() || optionA.isBlank() || optionB.isBlank() || optionC.isBlank() || optionD.isBlank() || selectedQuizId.isEmpty()) {
                            Toast.makeText(context, "Please fill in all fields & options", Toast.LENGTH_SHORT).show()
                        } else {
                            val optionsList = listOf(optionA.trim(), optionB.trim(), optionC.trim(), optionD.trim())
                            val quizObj = quizzes.find { it.quizId == selectedQuizId }
                            viewModel.adminAddQuestion(
                                quizId = selectedQuizId,
                                categoryId = quizObj?.categoryId ?: "",
                                questionText = questionText.trim(),
                                optionsList = optionsList,
                                correctAnswer = correctValue.trim(),
                                explanation = explanation.trim(),
                                difficulty = quizObj?.difficulty ?: "Medium"
                            )
                            questionText = ""
                            optionA = ""
                            optionB = ""
                            optionC = ""
                            optionD = ""
                            explanation = ""
                            Toast.makeText(context, "Question Added successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Question", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
