package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.local.QuizDatabase
import com.example.data.model.Category
import com.example.data.model.LeaderboardEntry
import com.example.data.model.Question
import com.example.data.model.Quiz
import com.example.data.model.User
import com.example.data.model.UserHistory
import com.example.data.model.RewardActivity
import com.example.data.repository.QuizRepository
import com.example.ui.screens.CelebrationType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val database: QuizDatabase = Room.databaseBuilder(
        application,
        QuizDatabase::class.java,
        "quiz_master_db"
    ).build()

    private val repository = QuizRepository(application, database)

    // App Startup & Seeding
    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // 1. Authentication State
    val currentUserId: StateFlow<String?> = repository.currentUserIdFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val currentUser: StateFlow<User?> = currentUserId.flatMapLatest { uid ->
        if (uid != null) repository.getUserFlow(uid) else flowOf(null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // 2. Data Feeds
    val categories: StateFlow<List<Category>> = repository.getAllCategories().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val quizzes: StateFlow<List<Quiz>> = repository.getAllQuizzes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _leaderboardTimeframe = MutableStateFlow("weekly")
    val leaderboardTimeframe = _leaderboardTimeframe.asStateFlow()

    private val _leaderboardScope = MutableStateFlow("global")
    val leaderboardScope = _leaderboardScope.asStateFlow()

    val leaderboard: StateFlow<List<LeaderboardEntry>> = flatMapLatestTimeframeAndScope(
        _leaderboardTimeframe, _leaderboardScope
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun flatMapLatestTimeframeAndScope(
        timeframe: StateFlow<String>,
        scope: StateFlow<String>
    ): StateFlow<List<LeaderboardEntry>> {
        val result = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
        viewModelScope.launch {
            timeframe.collect { tf ->
                scope.collect { sc ->
                    repository.getLeaderboard(tf, sc).collect { data ->
                        result.value = data
                    }
                }
            }
        }
        return result.asStateFlow()
    }

    val userHistory: StateFlow<List<UserHistory>> = currentUserId.flatMapLatest { uid ->
        if (uid != null) repository.getHistoryForUser(uid) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val rewardActivities: StateFlow<List<RewardActivity>> = currentUserId.flatMapLatest { uid ->
        if (uid != null) repository.getRewardActivitiesForUser(uid) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Settings & Shared Preferences Local State
    private val _darkModeEnabled = MutableStateFlow(true)
    val darkModeEnabled = _darkModeEnabled.asStateFlow()

    private val _soundEffectsEnabled = MutableStateFlow(true)
    val soundEffectsEnabled = _soundEffectsEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled = _vibrationEnabled.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    // 3. Active Quiz Play State Machine
    private val _activeQuiz = MutableStateFlow<Quiz?>(null)
    val activeQuiz = _activeQuiz.asStateFlow()

    private val _activeQuestions = MutableStateFlow<List<Question>>(emptyList())
    val activeQuestions = _activeQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswer = MutableStateFlow<String?>(null)
    val selectedAnswer = _selectedAnswer.asStateFlow()

    private val _isAnswered = MutableStateFlow(false)
    val isAnswered = _isAnswered.asStateFlow()

    private val _isCorrect = MutableStateFlow<Boolean?>(null)
    val isCorrect = _isCorrect.asStateFlow()

    private val _secondsRemaining = MutableStateFlow(20)
    val secondsRemaining = _secondsRemaining.asStateFlow()

    private val _isQuizPaused = MutableStateFlow(false)
    val isQuizPaused = _isQuizPaused.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore = _quizScore.asStateFlow()

    private val _quizCorrectAnswers = MutableStateFlow(0)
    val quizCorrectAnswers = _quizCorrectAnswers.asStateFlow()

    private val _quizWrongAnswers = MutableStateFlow(0)
    val quizWrongAnswers = _quizWrongAnswers.asStateFlow()

    private val _coinsEarned = MutableStateFlow(0)
    val coinsEarned = _coinsEarned.asStateFlow()

    private val _xpEarned = MutableStateFlow(0)
    val xpEarned = _xpEarned.asStateFlow()

    private val _lifeline5050Used = MutableStateFlow(false)
    val lifeline5050Used = _lifeline5050Used.asStateFlow()

    private val _extraTimeUsed = MutableStateFlow(false)
    val extraTimeUsed = _extraTimeUsed.asStateFlow()

    private val _bookmarkedQuestions = MutableStateFlow<Set<String>>(emptySet())
    val bookmarkedQuestions = _bookmarkedQuestions.asStateFlow()

    private val _removedOptions = MutableStateFlow<Set<String>>(emptySet())
    val removedOptions = _removedOptions.asStateFlow()

    private val _quizCompleted = MutableStateFlow(false)
    val quizCompleted = _quizCompleted.asStateFlow()

    private val _timeTaken = MutableStateFlow(0)
    val timeTaken = _timeTaken.asStateFlow()

    private val _quizTypeMode = MutableStateFlow("Practice") // Practice or Exam
    val quizTypeMode = _quizTypeMode.asStateFlow()

    private var timerJob: Job? = null

    // Lucky Spin State
    private val _lastSpinTime = MutableStateFlow(0L)
    val lastSpinTime = _lastSpinTime.asStateFlow()

    private val _luckySpinResult = MutableStateFlow<String?>(null)
    val luckySpinResult = _luckySpinResult.asStateFlow()

    // Celebration State
    private val _celebrationEvent = MutableStateFlow<CelebrationType?>(null)
    val celebrationEvent = _celebrationEvent.asStateFlow()

    fun triggerCelebration(celebration: CelebrationType) {
        _celebrationEvent.value = celebration
        // Trigger haptic feedback for celebration
        if (_vibrationEnabled.value) {
            triggerHapticFeedback(isCorrect = true)
        }
    }

    fun dismissCelebration() {
        _celebrationEvent.value = null
    }

    // Admin Mode State
    private val _adminModeActive = MutableStateFlow(false)
    val adminModeActive = _adminModeActive.asStateFlow()

    // 4. Authentication Functions
    fun login(email: String, name: String? = null, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val user = repository.loginUser(email, name)
            if (user != null) {
                // Sync bookmarks
                val bookmarksSet = user.bookmarks.split(",").filter { it.isNotEmpty() }.toSet()
                _bookmarkedQuestions.value = bookmarksSet
                onSuccess()
            }
        }
    }

    fun register(name: String, email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.registerUser(name, email)
            onSuccess()
        }
    }

    fun loginGuest(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.loginAsGuest()
            onSuccess()
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onSuccess()
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val uid = currentUserId.value
            if (uid != null) {
                repository.deleteAccount(uid)
            }
        }
    }

    fun updateProfile(name: String, country: String, avatar: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val updated = user.copy(
                name = name.trim(),
                country = country.trim(),
                profilePhoto = avatar
            )
            repository.updateUser(updated)
        }
    }

    // 5. Leaderboard Controls
    fun setLeaderboardFilters(timeframe: String, scope: String) {
        _leaderboardTimeframe.value = timeframe
        _leaderboardScope.value = scope
    }

    // 6. Game Logic Functions
    fun startQuiz(quiz: Quiz, mode: String = "Practice") {
        viewModelScope.launch {
            _activeQuiz.value = quiz
            _quizTypeMode.value = mode
            val questions = repository.getQuestionsForQuiz(quiz.quizId)
            // Shuffle questions for random game experience
            _activeQuestions.value = questions.shuffled()
            _currentQuestionIndex.value = 0
            _selectedAnswer.value = null
            _isAnswered.value = false
            _isCorrect.value = null
            _quizScore.value = 0
            _quizCorrectAnswers.value = 0
            _quizWrongAnswers.value = 0
            _coinsEarned.value = 0
            _xpEarned.value = 0
            _lifeline5050Used.value = false
            _extraTimeUsed.value = false
            _removedOptions.value = emptySet()
            _quizCompleted.value = false
            _timeTaken.value = 0
            _isQuizPaused.value = false
            startTimer(questions.firstOrNull()?.timeLimit ?: 20)
        }
    }

    private fun startTimer(duration: Int) {
        timerJob?.cancel()
        _secondsRemaining.value = duration
        timerJob = viewModelScope.launch {
            while (_secondsRemaining.value > 0) {
                delay(1000)
                if (!_isQuizPaused.value) {
                    _secondsRemaining.value -= 1
                    _timeTaken.value += 1
                }
            }
            // Timer expired, auto skip or auto wrong
            if (!_isAnswered.value) {
                submitAnswer("")
            }
        }
    }

    fun togglePauseQuiz() {
        _isQuizPaused.value = !_isQuizPaused.value
    }

    fun submitAnswer(answer: String) {
        if (_isAnswered.value) return
        timerJob?.cancel()
        val currentQuestion = _activeQuestions.value.getOrNull(_currentQuestionIndex.value) ?: return
        val isCorrectAns = answer.lowercase() == currentQuestion.correctAnswer.lowercase()

        _selectedAnswer.value = answer
        _isAnswered.value = true
        _isCorrect.value = isCorrectAns

        triggerHapticFeedback(isCorrectAns)

        if (isCorrectAns) {
            _quizScore.value += currentQuestion.marks
            _quizCorrectAnswers.value += 1
            // Earn immediate coins & XP
            _coinsEarned.value += 5
            _xpEarned.value += 15
        } else {
            _quizScore.value -= currentQuestion.negativeMarks
            _quizWrongAnswers.value += 1
            _xpEarned.value += 2 // minimal XP for participating
        }
    }

    fun useLifeline5050() {
        if (_lifeline5050Used.value || _isAnswered.value) return
        val currentQuestion = _activeQuestions.value.getOrNull(_currentQuestionIndex.value) ?: return
        val wrongOptions = currentQuestion.options.split(",")
            .filter { it.lowercase() != currentQuestion.correctAnswer.lowercase() }
            .shuffled()
            .take(2)
        _removedOptions.value = wrongOptions.toSet()
        _lifeline5050Used.value = true
    }

    fun useExtraTime() {
        if (_extraTimeUsed.value || _isAnswered.value) return
        _secondsRemaining.value += 15
        _extraTimeUsed.value = true
    }

    fun toggleBookmark(questionId: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val currentBookmarks = _bookmarkedQuestions.value.toMutableSet()
            if (currentBookmarks.contains(questionId)) {
                currentBookmarks.remove(questionId)
            } else {
                currentBookmarks.add(questionId)
            }
            _bookmarkedQuestions.value = currentBookmarks
            val updatedUser = user.copy(bookmarks = currentBookmarks.joinToString(","))
            repository.updateUser(updatedUser)
        }
    }

    fun nextQuestion() {
        val nextIndex = _currentQuestionIndex.value + 1
        if (nextIndex < _activeQuestions.value.size) {
            _currentQuestionIndex.value = nextIndex
            _selectedAnswer.value = null
            _isAnswered.value = false
            _isCorrect.value = null
            _removedOptions.value = emptySet()
            startTimer(_activeQuestions.value[nextIndex].timeLimit)
        } else {
            completeQuiz()
        }
    }

    private fun completeQuiz() {
        timerJob?.cancel()
        _quizCompleted.value = true
        viewModelScope.launch {
            val uid = currentUserId.value ?: return@launch
            val quiz = _activeQuiz.value ?: return@launch
            val totalQuestions = _activeQuestions.value.size
            val correct = _quizCorrectAnswers.value
            val wrong = _quizWrongAnswers.value
            val accuracy = if (totalQuestions > 0) (correct.toDouble() / totalQuestions) * 100 else 0.0

            // Apply streak/difficulty multi
            var coinsAwarded = _coinsEarned.value
            var xpAwarded = _xpEarned.value
            if (accuracy >= 100.0) {
                coinsAwarded += 50 // Perfect score bonus
                xpAwarded += 100
            } else if (accuracy >= 80.0) {
                coinsAwarded += 20
                xpAwarded += 50
            }

            val history = UserHistory(
                historyId = UUID.randomUUID().toString(),
                uid = uid,
                quizId = quiz.quizId,
                quizName = quiz.name,
                score = _quizScore.value,
                accuracy = accuracy,
                xpEarned = xpAwarded,
                coinsEarned = coinsAwarded,
                correctAnswers = correct,
                wrongAnswers = wrong,
                totalQuestions = totalQuestions
            )
            
            // Capture old level from active user
            val oldUser = currentUser.value
            val oldLevel = oldUser?.level ?: 1
            val newLevel = if (oldUser != null) ((oldUser.xp + xpAwarded) / 500) + 1 else 1

            repository.saveQuizHistory(history)
            // Sync final updated coins/xp to local UI
            _coinsEarned.value = coinsAwarded
            _xpEarned.value = xpAwarded

            // Trigger celebration
            if (newLevel > oldLevel) {
                triggerCelebration(CelebrationType.LevelUp(newLevel, oldLevel))
            } else if (coinsAwarded > 0 || xpAwarded > 0) {
                triggerCelebration(CelebrationType.RewardsEarned(coinsAwarded, xpAwarded, "You completed ${quiz.name}!"))
            }
        }
    }

    fun forceResetQuizPlay() {
        timerJob?.cancel()
        _activeQuiz.value = null
        _quizCompleted.value = false
        _activeQuestions.value = emptyList()
    }

    // 7. Gamification & Spin Wheel
    fun spinWheel() {
        val user = currentUser.value ?: return
        if (user.coins < 20) {
            Toast.makeText(getApplication(), "You need at least 20 Coins to spin!", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            val rewards = listOf("50 Coins", "100 XP", "10 Coins", "No Luck", "200 Coins", "500 XP")
            val spinValue = rewards.random()
            _luckySpinResult.value = spinValue

            var coinsAwarded = 0
            var xpAwarded = 0

            var updatedUser = user.copy(coins = user.coins - 20) // spin cost
            when (spinValue) {
                "50 Coins" -> {
                    updatedUser = updatedUser.copy(coins = updatedUser.coins + 50)
                    coinsAwarded = 50
                }
                "10 Coins" -> {
                    updatedUser = updatedUser.copy(coins = updatedUser.coins + 10)
                    coinsAwarded = 10
                }
                "200 Coins" -> {
                    updatedUser = updatedUser.copy(coins = updatedUser.coins + 200)
                    coinsAwarded = 200
                }
                "100 XP" -> {
                    val newXp = updatedUser.xp + 100
                    val newLevel = (newXp / 500) + 1
                    updatedUser = updatedUser.copy(xp = newXp, level = newLevel)
                    xpAwarded = 100
                }
                "500 XP" -> {
                    val newXp = updatedUser.xp + 500
                    val newLevel = (newXp / 500) + 1
                    updatedUser = updatedUser.copy(xp = newXp, level = newLevel)
                    xpAwarded = 500
                }
            }
            
            val oldLevel = user.level
            val newLevel = updatedUser.level

            val levelUpBonus = if (newLevel > oldLevel) 100 else 0
            if (levelUpBonus > 0) {
                updatedUser = updatedUser.copy(coins = updatedUser.coins + levelUpBonus)
            }

            repository.updateUser(updatedUser)
            _lastSpinTime.value = System.currentTimeMillis()

            // Log Lucky Spin Activity
            repository.insertRewardActivity(
                RewardActivity(
                    activityId = UUID.randomUUID().toString(),
                    uid = user.uid,
                    type = "LUCKY_SPIN",
                    title = "Lucky Spin Winner",
                    description = if (spinValue == "No Luck") "Spun the Fortune Wheel but got no rewards." else "Won $spinValue from the Fortune Wheel!",
                    coinsEarned = coinsAwarded,
                    xpEarned = xpAwarded
                )
            )

            // Log Level Up Activity if it happened
            if (newLevel > oldLevel) {
                repository.insertRewardActivity(
                    RewardActivity(
                        activityId = UUID.randomUUID().toString(),
                        uid = user.uid,
                        type = "LEVEL_UP",
                        title = "Leveled Up!",
                        description = "Reached Level $newLevel! Earned +100 bonus coins.",
                        coinsEarned = levelUpBonus,
                        xpEarned = 0
                    )
                )
            }

            // Delay triggering celebration slightly so it perfectly coincides with wheel stopping / results
            delay(1500) 
            if (newLevel > oldLevel) {
                triggerCelebration(CelebrationType.LevelUp(newLevel, oldLevel))
            } else if (coinsAwarded > 0 || xpAwarded > 0) {
                triggerCelebration(CelebrationType.RewardsEarned(coinsAwarded, xpAwarded, "Lucky Spin Winnings!"))
            }
        }
    }

    fun clearSpinResult() {
        _luckySpinResult.value = null
    }

    fun earnDailyLoginBonus() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            val oldLevel = user.level
            val newLevel = ((user.xp + 50) / 500) + 1
            
            val levelUpBonus = if (newLevel > oldLevel) 100 else 0
            
            // Standard daily bonus: 50 coins & 50 XP + level up bonus if applicable
            val updatedUser = user.copy(
                coins = user.coins + 50 + levelUpBonus,
                xp = user.xp + 50,
                level = newLevel
            )
            repository.updateUser(updatedUser)
            Toast.makeText(getApplication(), "Daily Reward Claimed! +50 Coins, +50 XP", Toast.LENGTH_LONG).show()

            // Log Daily Login Activity
            repository.insertRewardActivity(
                RewardActivity(
                    activityId = UUID.randomUUID().toString(),
                    uid = user.uid,
                    type = "DAILY_LOGIN",
                    title = "Daily Login Reward",
                    description = "Claimed daily bonus reward.",
                    coinsEarned = 50,
                    xpEarned = 50
                )
            )

            // Log Level Up Activity if it happened
            if (newLevel > oldLevel) {
                repository.insertRewardActivity(
                    RewardActivity(
                        activityId = UUID.randomUUID().toString(),
                        uid = user.uid,
                        type = "LEVEL_UP",
                        title = "Leveled Up!",
                        description = "Reached Level $newLevel! Earned +100 bonus coins.",
                        coinsEarned = levelUpBonus,
                        xpEarned = 0
                    )
                )
            }

            if (newLevel > oldLevel) {
                triggerCelebration(CelebrationType.LevelUp(newLevel, oldLevel))
            } else {
                triggerCelebration(CelebrationType.RewardsEarned(50, 50, "Daily login streak bonus!"))
            }
        }
    }

    // Settings adjustments
    fun setDarkMode(enabled: Boolean) {
        _darkModeEnabled.value = enabled
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEffectsEnabled.value = enabled
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _vibrationEnabled.value = enabled
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    private fun triggerHapticFeedback(isCorrect: Boolean) {
        if (!_vibrationEnabled.value) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getApplication<Application>().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = if (isCorrect) {
                        android.os.VibrationEffect.createOneShot(100, android.os.VibrationEffect.DEFAULT_AMPLITUDE)
                    } else {
                        android.os.VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 150), intArrayOf(0, 255, 0, 255), -1)
                    }
                    it.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    if (isCorrect) it.vibrate(100) else it.vibrate(longArrayOf(0, 150, 100, 150), -1)
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("QuizViewModel", "Failed to trigger haptic feedback", e)
        }
    }

    // 8. Admin CRUD Actions
    fun setAdminMode(active: Boolean) {
        _adminModeActive.value = active
    }

    fun adminAddCategory(name: String, description: String) {
        viewModelScope.launch {
            val id = "cat_" + name.replace(" ", "_").lowercase() + "_" + UUID.randomUUID().toString().take(4)
            val category = Category(
                categoryId = id,
                name = name,
                description = description,
                image = "ic_category_custom"
            )
            repository.addCategory(category)
        }
    }

    fun adminDeleteCategory(categoryId: String) {
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
        }
    }

    fun adminAddQuiz(categoryId: String, name: String, description: String, difficulty: String, estTime: Int) {
        viewModelScope.launch {
            val id = "quiz_" + name.replace(" ", "_").lowercase() + "_" + UUID.randomUUID().toString().take(4)
            val quiz = Quiz(
                quizId = id,
                categoryId = categoryId,
                name = name,
                description = description,
                difficulty = difficulty,
                estimatedTime = estTime,
                totalQuestions = 0
            )
            repository.addQuiz(quiz)
        }
    }

    fun adminDeleteQuiz(quizId: String) {
        viewModelScope.launch {
            repository.deleteQuiz(quizId)
        }
    }

    fun adminAddQuestion(
        quizId: String,
        categoryId: String,
        questionText: String,
        optionsList: List<String>,
        correctAnswer: String,
        explanation: String,
        difficulty: String,
        type: String = "multiple_choice"
    ) {
        viewModelScope.launch {
            val id = "q_" + UUID.randomUUID().toString().take(8)
            val question = Question(
                questionId = id,
                quizId = quizId,
                categoryId = categoryId,
                question = questionText,
                options = optionsList.joinToString(","),
                correctAnswer = correctAnswer,
                explanation = explanation,
                difficulty = difficulty,
                type = type
            )
            repository.addQuestion(question)

            // Update quiz totalQuestions count
            val quiz = quizzes.value.find { it.quizId == quizId }
            if (quiz != null) {
                val updatedQuiz = quiz.copy(totalQuestions = quiz.totalQuestions + 1)
                repository.addQuiz(updatedQuiz)
            }
        }
    }
}
