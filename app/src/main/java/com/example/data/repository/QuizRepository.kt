package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.QuizDatabase
import com.example.data.model.Category
import com.example.data.model.LeaderboardEntry
import com.example.data.model.Question
import com.example.data.model.Quiz
import com.example.data.model.User
import com.example.data.model.UserHistory
import com.example.data.model.RewardActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "quiz_master_prefs")

class QuizRepository(
    private val context: Context,
    private val database: QuizDatabase
) {
    private val userDao = database.userDao()
    private val categoryDao = database.categoryDao()
    private val quizDao = database.quizDao()
    private val questionDao = database.questionDao()
    private val leaderboardDao = database.leaderboardDao()
    private val userHistoryDao = database.userHistoryDao()
    private val rewardActivityDao = database.rewardActivityDao()

    // Session Management Keys
    private val KEY_CURRENT_USER_ID = stringPreferencesKey("current_user_id")

    val currentUserIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_CURRENT_USER_ID]
    }

    suspend fun getSessionUser(): User? {
        val uid = currentUserIdFlow.firstOrNull() ?: return null
        return userDao.getUserSync(uid)
    }

    suspend fun loginUser(email: String, name: String? = null): User? {
        val formattedEmail = email.trim().lowercase()
        // Check if user exists
        val users = userDao.getAllUsers()
        var user = users.find { it.email.lowercase() == formattedEmail }
        if (user == null) {
            // Auto register
            val newUid = UUID.randomUUID().toString()
            user = User(
                uid = newUid,
                name = name ?: email.substringBefore("@").replaceFirstChar { it.uppercase() },
                email = formattedEmail,
                coins = 200,
                xp = 50,
                level = 1,
                country = "United States"
            )
            userDao.insertUser(user)
            insertRewardActivity(
                RewardActivity(
                    activityId = UUID.randomUUID().toString(),
                    uid = newUid,
                    type = "WELCOME_BONUS",
                    title = "Welcome Bonus",
                    description = "Claimed your starter welcome pack!",
                    coinsEarned = 200,
                    xpEarned = 50
                )
            )
        }
        context.dataStore.edit { prefs ->
            prefs[KEY_CURRENT_USER_ID] = user.uid
        }
        return user
    }

    suspend fun registerUser(name: String, email: String): User {
        val formattedEmail = email.trim().lowercase()
        val newUid = UUID.randomUUID().toString()
        val user = User(
            uid = newUid,
            name = name.trim(),
            email = formattedEmail,
            coins = 250,
            xp = 100,
            level = 1,
            country = "United States"
        )
        userDao.insertUser(user)
        insertRewardActivity(
            RewardActivity(
                activityId = UUID.randomUUID().toString(),
                uid = newUid,
                type = "WELCOME_BONUS",
                title = "Welcome Bonus",
                description = "Account registered successfully!",
                coinsEarned = 250,
                xpEarned = 100
            )
        )
        context.dataStore.edit { prefs ->
            prefs[KEY_CURRENT_USER_ID] = user.uid
        }
        return user
    }

    suspend fun loginAsGuest(): User {
        val guestUid = "GUEST_" + UUID.randomUUID().toString().take(6)
        val guestUser = User(
            uid = guestUid,
            name = "Guest Master",
            email = "guest@quizmaster.com",
            coins = 100,
            xp = 0,
            level = 1,
            country = "Global"
        )
        userDao.insertUser(guestUser)
        insertRewardActivity(
            RewardActivity(
                activityId = UUID.randomUUID().toString(),
                uid = guestUid,
                type = "WELCOME_BONUS",
                title = "Guest Welcome Starter Pack",
                description = "Logged in as a Guest Explorer.",
                coinsEarned = 100,
                xpEarned = 0
            )
        )
        context.dataStore.edit { prefs ->
            prefs[KEY_CURRENT_USER_ID] = guestUid
        }
        return guestUser
    }

    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_CURRENT_USER_ID)
        }
    }

    suspend fun deleteAccount(uid: String) {
        // Simple mock delete - just clear session and keep DB record or delete
        logout()
    }

    fun getUserFlow(uid: String): Flow<User?> = userDao.getUserFlow(uid)

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    // Categories
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    suspend fun addCategory(category: Category) = categoryDao.insertCategory(category)
    suspend fun deleteCategory(categoryId: String) = categoryDao.deleteCategory(categoryId)

    // Quizzes
    fun getAllQuizzes(): Flow<List<Quiz>> = quizDao.getAllQuizzesFlow()
    fun getQuizzesByCategory(categoryId: String): Flow<List<Quiz>> = quizDao.getQuizzesByCategoryFlow(categoryId)
    suspend fun addQuiz(quiz: Quiz) = quizDao.insertQuiz(quiz)
    suspend fun deleteQuiz(quizId: String) {
        quizDao.deleteQuiz(quizId)
        questionDao.deleteQuestionsForQuiz(quizId)
    }

    // Questions
    suspend fun getQuestionsForQuiz(quizId: String): List<Question> = questionDao.getQuestionsForQuiz(quizId)
    suspend fun addQuestion(question: Question) = questionDao.insertQuestion(question)
    suspend fun deleteQuestion(questionId: String) = questionDao.deleteQuestion(questionId)

    // Leaderboard
    fun getLeaderboard(timeframe: String, scope: String): Flow<List<LeaderboardEntry>> =
        leaderboardDao.getLeaderboardFlow(timeframe, scope)

    // History
    fun getHistoryForUser(uid: String): Flow<List<UserHistory>> = userHistoryDao.getHistoryForUserFlow(uid)
    
    // Reward Activity Logging
    fun getRewardActivitiesForUser(uid: String): Flow<List<RewardActivity>> = rewardActivityDao.getRewardActivitiesForUserFlow(uid)
    suspend fun insertRewardActivity(activity: RewardActivity) = rewardActivityDao.insertRewardActivity(activity)

    suspend fun saveQuizHistory(history: UserHistory) {
        userHistoryDao.insertHistory(history)
        // Also update User profile stats
        val user = userDao.getUserSync(history.uid) ?: return
        val newCorrect = user.correctAnswers + history.correctAnswers
        val newWrong = user.wrongAnswers + history.wrongAnswers
        val newTotal = user.totalQuizPlayed + 1
        val addedXp = history.xpEarned
        val addedCoins = history.coinsEarned

        val totalXp = user.xp + addedXp
        val currentLevel = user.level
        val newLevel = (totalXp / 500) + 1 // Simple leveling rule: 500 XP per level

        // Badges calculation
        val currentBadges = user.achievements.split(",").filter { it.isNotEmpty() }.toSet()
        val badges = currentBadges.toMutableList()
        val newlyUnlocked = mutableListOf<String>()

        if (newTotal >= 1 && !currentBadges.contains("first_quiz")) {
            badges.add("first_quiz")
            newlyUnlocked.add("first_quiz")
        }
        if (newCorrect >= 10 && !currentBadges.contains("sharp_shooter")) {
            badges.add("sharp_shooter")
            newlyUnlocked.add("sharp_shooter")
        }
        if (newLevel >= 3 && !currentBadges.contains("level_3")) {
            badges.add("level_3")
            newlyUnlocked.add("level_3")
        }
        if (user.winStreak + 1 >= 3 && !currentBadges.contains("hot_streak")) {
            badges.add("hot_streak")
            newlyUnlocked.add("hot_streak")
        }

        // Coins & XP rewards for badges
        val levelUpBonus = if (newLevel > currentLevel) 100 else 0
        val achievementCoinsBonus = newlyUnlocked.size * 50
        val achievementXpBonus = newlyUnlocked.size * 50

        val finalXp = totalXp + achievementXpBonus
        val finalCoins = user.coins + addedCoins + levelUpBonus + achievementCoinsBonus
        val finalLevel = (finalXp / 500) + 1

        val updatedUser = user.copy(
            xp = finalXp,
            coins = finalCoins,
            level = finalLevel,
            correctAnswers = newCorrect,
            wrongAnswers = newWrong,
            totalQuizPlayed = newTotal,
            winStreak = if (history.wrongAnswers == 0) user.winStreak + 1 else 0,
            achievements = badges.joinToString(",")
        )
        userDao.insertUser(updatedUser)

        // Log Quiz Reward Activity
        if (addedCoins > 0 || addedXp > 0) {
            insertRewardActivity(
                RewardActivity(
                    activityId = UUID.randomUUID().toString(),
                    uid = history.uid,
                    type = "QUIZ_REWARD",
                    title = "Quiz Completed",
                    description = "Completed '${history.quizName}' with ${history.accuracy.toInt()}% accuracy.",
                    coinsEarned = addedCoins,
                    xpEarned = addedXp
                )
            )
        }

        // Log Level Up Activity
        if (finalLevel > currentLevel) {
            insertRewardActivity(
                RewardActivity(
                    activityId = UUID.randomUUID().toString(),
                    uid = history.uid,
                    type = "LEVEL_UP",
                    title = "Leveled Up!",
                    description = "Reached Level $finalLevel! Earned +100 bonus coins.",
                    coinsEarned = levelUpBonus,
                    xpEarned = 0
                )
            )
        }

        // Log Newly Unlocked Badge Achievements
        newlyUnlocked.forEach { badgeId ->
            val badgeName = when (badgeId) {
                "first_quiz" -> "First Steps Achievement"
                "sharp_shooter" -> "Sharp Shooter Achievement"
                "level_3" -> "Rising Star Achievement"
                "hot_streak" -> "On Fire Achievement"
                else -> "New Achievement"
            }
            val badgeDesc = when (badgeId) {
                "first_quiz" -> "Completed your first quiz event!"
                "sharp_shooter" -> "Answered 10 questions correctly!"
                "level_3" -> "Reached Level 3!"
                "hot_streak" -> "Achieved a 3-win streak!"
                else -> "Unlocked a new badge."
            }
            insertRewardActivity(
                RewardActivity(
                    activityId = UUID.randomUUID().toString(),
                    uid = history.uid,
                    type = "ACHIEVEMENT_UNLOCKED",
                    title = badgeName,
                    description = badgeDesc,
                    coinsEarned = 50,
                    xpEarned = 50
                )
            )
        }
    }

    // Seed Initial Data
    suspend fun seedDatabaseIfEmpty() {
        // Check if categories is empty
        val currentUsers = userDao.getAllUsers()
        if (currentUsers.isEmpty()) {
            // Seed default users for Leaderboard
            val dummyUsers = listOf(
                User("user1", "John Maverick", "john@maverick.com", "avatar_1", "United States", 1540, 450, 1, 4),
                User("user2", "Sana Ahmed", "sana@ahmed.com", "avatar_2", "Bangladesh", 1230, 310, 2, 3),
                User("user3", "Kenji Sato", "kenji@sato.com", "avatar_3", "Japan", 980, 220, 3, 2),
                User("user4", "Liam O'Connor", "liam@oconnor.com", "avatar_4", "Ireland", 750, 150, 4, 2),
                User("user5", "Elena Petrova", "elena@petrova.com", "avatar_5", "Global", 540, 120, 5, 2)
            )
            dummyUsers.forEach { userDao.insertUser(it) }

            // Seed Leaderboard entries
            val leaderboard = listOf(
                // Weekly
                LeaderboardEntry("lb_w1", "user1", "John Maverick", "avatar_1", "United States", 1540, 4, 1, "weekly", "global"),
                LeaderboardEntry("lb_w2", "user2", "Sana Ahmed", "avatar_2", "Bangladesh", 1230, 3, 2, "weekly", "global"),
                LeaderboardEntry("lb_w3", "user3", "Kenji Sato", "avatar_3", "Japan", 980, 2, 3, "weekly", "global"),
                LeaderboardEntry("lb_w4", "user4", "Liam O'Connor", "avatar_4", "Ireland", 750, 2, 4, "weekly", "global"),
                LeaderboardEntry("lb_w5", "user5", "Elena Petrova", "avatar_5", "Global", 540, 2, 5, "weekly", "global"),
                // Monthly
                LeaderboardEntry("lb_m1", "user1", "John Maverick", "avatar_1", "United States", 5420, 11, 1, "monthly", "global"),
                LeaderboardEntry("lb_m2", "user2", "Sana Ahmed", "avatar_2", "Bangladesh", 4100, 9, 2, "monthly", "global"),
                // Country specific
                LeaderboardEntry("lb_c1", "user2", "Sana Ahmed", "avatar_2", "Bangladesh", 1230, 3, 1, "weekly", "country")
            )
            leaderboardDao.insertLeaderboard(leaderboard)
        }

        val categoriesFlow = categoryDao.getAllCategories()
        val currentCategories = categoriesFlow.firstOrNull() ?: emptyList()
        if (currentCategories.isEmpty()) {
            // 1. Categories
            val categories = listOf(
                Category("cat_kotlin", "Kotlin Programming", "Modern, expressive and safe language for Android app development.", "ic_kotlin", 2, 8, "Medium"),
                Category("cat_android", "Android Development", "Core Android concepts, Jetpack Compose, Arch components & layouts.", "ic_android", 2, 8, "Hard"),
                Category("cat_science", "General Science", "Fascinating questions about physics, chemistry, biology and space.", "ic_science", 1, 4, "Easy"),
                Category("cat_programming", "Software Engineering", "Global computer science concepts, structures, & algorithm fundamentals.", "ic_programming", 1, 4, "Medium")
            )
            categoryDao.insertCategories(categories)

            // 2. Quizzes
            val quizzes = listOf(
                Quiz("quiz_kotlin_basics", "cat_kotlin", "Kotlin Basics", "Test your knowledge of variables, functions, and control structures.", "", 4, "Easy", 3),
                Quiz("quiz_kotlin_adv", "cat_kotlin", "Kotlin Advanced", "Test coroutines, sealed classes, extensions, and delegation.", "", 4, "Hard", 4),
                Quiz("quiz_android_compose", "cat_android", "Jetpack Compose", "Modern UI toolkit questions covering state management, recomposition and layouts.", "", 4, "Medium", 3),
                Quiz("quiz_android_arch", "cat_android", "Architecture Components", "MVVM, Clean Architecture, WorkManager, & Room database integrations.", "", 4, "Expert", 4),
                Quiz("quiz_science_space", "cat_science", "Cosmology & Space", "Planets, stars, black holes, and space explorations quiz.", "", 4, "Easy", 3),
                Quiz("quiz_prog_fundamentals", "cat_programming", "DS & Algorithms", "Basic structures: Lists, Stacks, Queues, Binary trees, and search algorithms.", "", 4, "Medium", 3)
            )
            quizDao.insertQuizzes(quizzes)

            // 3. Questions
            val questions = listOf(
                // Kotlin Basics Quiz (4 MCQ)
                Question(
                    "q_kb1", "quiz_kotlin_basics", "cat_kotlin",
                    "What keyword is used to declare a read-only local variable in Kotlin?",
                    options = "var,val,const,let",
                    correctAnswer = "val",
                    explanation = "'val' declares a read-only or immutable reference, while 'var' is for reassignable variables.",
                    difficulty = "Easy"
                ),
                Question(
                    "q_kb2", "quiz_kotlin_basics", "cat_kotlin",
                    "How do you handle potential null pointer exceptions in Kotlin?",
                    options = "By using Safe Call (?.),By using Force Operator (!!),By using Elvis Operator (?:),All of the above",
                    correctAnswer = "All of the above",
                    explanation = "Kotlin provides safe call (?.), double-bang force (!!), and elvis fallback (?:) to safely handle nullability.",
                    difficulty = "Easy"
                ),
                Question(
                    "q_kb3", "quiz_kotlin_basics", "cat_kotlin",
                    "Is Kotlin fully interoperable with the Java programming language?",
                    options = "True,False",
                    correctAnswer = "True",
                    explanation = "Yes, Kotlin is 100% interoperable with Java. You can call Kotlin code from Java and vice versa seamlessly.",
                    difficulty = "Easy",
                    type = "true_false"
                ),
                Question(
                    "q_kb4", "quiz_kotlin_basics", "cat_kotlin",
                    "Complete the keyword for defining a function in Kotlin: '___ calculateXP()'",
                    options = "func,function,fun,def",
                    correctAnswer = "fun",
                    explanation = "Kotlin functions are defined using the 'fun' keyword.",
                    difficulty = "Easy",
                    type = "fill_blank"
                ),

                // Kotlin Advanced (4 Qs)
                Question(
                    "q_ka1", "quiz_kotlin_adv", "cat_kotlin",
                    "Which dispatcher is optimized for running network or disk operations in Kotlin Coroutines?",
                    options = "Dispatchers.Main,Dispatchers.Default,Dispatchers.IO,Dispatchers.Unconfined",
                    correctAnswer = "Dispatchers.IO",
                    explanation = "Dispatchers.IO is designed to offload blocking I/O tasks like API queries or database actions to a shared pool of threads.",
                    difficulty = "Hard"
                ),
                Question(
                    "q_ka2", "quiz_kotlin_adv", "cat_kotlin",
                    "What type of class allows you to represent restricted class hierarchies in Kotlin?",
                    options = "Enum Class,Sealed Class,Inner Class,Abstract Class",
                    correctAnswer = "Sealed Class",
                    explanation = "Sealed classes are used to represent restricted class hierarchies, where a value can have one of the types from a limited set.",
                    difficulty = "Hard"
                ),
                Question(
                    "q_ka3", "quiz_kotlin_adv", "cat_kotlin",
                    "Delegated properties in Kotlin are implemented using which keyword?",
                    options = "by,delegate,extends,lazy",
                    correctAnswer = "by",
                    explanation = "The 'by' keyword specifies property delegation in Kotlin (e.g., val p by lazy { ... }).",
                    difficulty = "Hard",
                    type = "fill_blank"
                ),
                Question(
                    "q_ka4", "quiz_kotlin_adv", "cat_kotlin",
                    "Coroutines are lightweight threads that can block the main Android thread if not launched correctly.",
                    options = "True,False",
                    correctAnswer = "True",
                    explanation = "Even though coroutines are cooperative, blocking operations inside Main dispatcher can freeze the UI.",
                    difficulty = "Hard",
                    type = "true_false"
                ),

                // Jetpack Compose (4 Qs)
                Question(
                    "q_jc1", "quiz_android_compose", "cat_android",
                    "How does Compose ensure a Composable function triggers UI updates when variable values change?",
                    options = "By wrapping it in remember { mutableStateOf(...) },By utilizing livedata,By running a background thread,By re-inflating XML",
                    correctAnswer = "By wrapping it in remember { mutableStateOf(...) }",
                    explanation = "Remember stores state across recompositions, and mutableStateOf exposes values that Compose schedules for redraws.",
                    difficulty = "Medium"
                ),
                Question(
                    "q_jc2", "quiz_android_compose", "cat_android",
                    "Which modifier parameter should you apply to fill the entire available width of a container?",
                    options = "Modifier.fillMaxHeight(),Modifier.fillMaxWidth(),Modifier.fillMaxSize(),Modifier.width(100)",
                    correctAnswer = "Modifier.fillMaxWidth()",
                    explanation = "Modifier.fillMaxWidth() makes the view occupy all horizontal space offered by its parent.",
                    difficulty = "Medium"
                ),
                Question(
                    "q_jc3", "quiz_android_compose", "cat_android",
                    "In Compose, state hoisting is the pattern of moving state up to make a component stateless.",
                    options = "True,False",
                    correctAnswer = "True",
                    explanation = "Hoisting state means moving state to a Composable's caller, making the component reusable and easier to test.",
                    difficulty = "Medium",
                    type = "true_false"
                ),
                Question(
                    "q_jc4", "quiz_android_compose", "cat_android",
                    "Identify this basic Compose component which displays children vertically:",
                    options = "Row,Box,Column,Scaffold",
                    correctAnswer = "Column",
                    explanation = "Column places its children vertically one after another.",
                    difficulty = "Medium",
                    type = "image_quiz" // Image quiz where we show an icon or description
                ),

                // Architecture Components
                Question(
                    "q_ac1", "quiz_android_arch", "cat_android",
                    "In Clean Architecture, which layer contains the pure business rules and Use Cases?",
                    options = "Presentation,Data,Domain,Framework",
                    correctAnswer = "Domain",
                    explanation = "Domain contains pure Kotlin entities and Use Cases independent of any UI, library, or database.",
                    difficulty = "Expert"
                ),
                Question(
                    "q_ac2", "quiz_android_arch", "cat_android",
                    "Room DB provides real-time updates by returning which reactive primitive from DAO query methods?",
                    options = "Single,Flow,LiveData,Both Flow & LiveData",
                    correctAnswer = "Both Flow & LiveData",
                    explanation = "Room supports Flow (Kotlin) and LiveData (Android Jetpack) as return types for observable query streams.",
                    difficulty = "Expert"
                ),
                Question(
                    "q_ac3", "quiz_android_arch", "cat_android",
                    "MVVM separates View from Model using a ViewModel which handles state.",
                    options = "True,False",
                    correctAnswer = "True",
                    explanation = "ViewModel manages state and communication between View and Domain model safely during configuration changes.",
                    difficulty = "Expert",
                    type = "true_false"
                ),
                Question(
                    "q_ac4", "quiz_android_arch", "cat_android",
                    "Which Room annotation designates a specific data field as the main identifier of an entity?",
                    options = "ColumnInfo,PrimaryKey,Entity,Embedded",
                    correctAnswer = "PrimaryKey",
                    explanation = "Each Room entity must declare at least 1 field as the @PrimaryKey to uniquely identify the row.",
                    difficulty = "Expert",
                    type = "fill_blank"
                ),

                // Space Quiz
                Question(
                    "q_sp1", "quiz_science_space", "cat_science",
                    "Which planet in our solar system is nicknamed the Red Planet?",
                    options = "Venus,Mars,Jupiter,Saturn",
                    correctAnswer = "Mars",
                    explanation = "Mars appears reddish because of high amounts of iron oxide (rust) on its surface.",
                    difficulty = "Easy"
                ),
                Question(
                    "q_sp2", "quiz_science_space", "cat_science",
                    "What is the name of the closest galaxy to our Milky Way?",
                    options = "Andromeda,Triangulum,Centaurus,Sombrero",
                    correctAnswer = "Andromeda",
                    explanation = "Andromeda is a spiral galaxy approximately 2.5 million light-years away and is our closest major neighbor.",
                    difficulty = "Easy"
                ),
                Question(
                    "q_sp3", "quiz_science_space", "cat_science",
                    "The Sun is a planet of hot gas held together by gravity.",
                    options = "True,False",
                    correctAnswer = "False",
                    explanation = "The Sun is a Main-Sequence G-type Star, not a planet.",
                    difficulty = "Easy",
                    type = "true_false"
                ),
                Question(
                    "q_sp4", "quiz_science_space", "cat_science",
                    "How many planets are currently in our solar system (excluding Pluto)?",
                    options = "7,8,9,10",
                    correctAnswer = "8",
                    explanation = "There are 8 major planets recognized in our solar system since Pluto was reclassified as a dwarf planet in 2006.",
                    difficulty = "Easy",
                    type = "fill_blank"
                ),

                // DS & Algos
                Question(
                    "q_ds1", "quiz_prog_fundamentals", "cat_programming",
                    "Which data structure follows the LIFO (Last-In, First-Out) principle?",
                    options = "Queue,Stack,Array,Linked List",
                    correctAnswer = "Stack",
                    explanation = "A Stack processes items in Last-In First-Out order (e.g., undo operations, back stack navigation).",
                    difficulty = "Medium"
                ),
                Question(
                    "q_ds2", "quiz_prog_fundamentals", "cat_programming",
                    "What is the average time complexity of searching a value in a Balanced Binary Search Tree (BST)?",
                    options = "O(1),O(n),O(log n),O(n log n)",
                    correctAnswer = "O(log n)",
                    explanation = "Searching in balanced BST discards half of the tree paths in each step, yielding logarithmic search complexity.",
                    difficulty = "Medium"
                ),
                Question(
                    "q_ds3", "quiz_prog_fundamentals", "cat_programming",
                    "A linear Queue follows the FIFO (First-In, First-Out) scheduling principle.",
                    options = "True,False",
                    correctAnswer = "True",
                    explanation = "Queues add items at the rear and retrieve them from the front, forming First-In First-Out queue logic.",
                    difficulty = "Medium",
                    type = "true_false"
                ),
                Question(
                    "q_ds4", "quiz_prog_fundamentals", "cat_programming",
                    "Which structure is dynamic, consisting of nodes linked sequentially by memory pointers?",
                    options = "Array,Stack,Linked List,Hash Table",
                    correctAnswer = "Linked List",
                    explanation = "A Linked List is a linear set of nodes connected through pointers, enabling quick insertion/deletion.",
                    difficulty = "Medium",
                    type = "fill_blank"
                )
            )
            questionDao.insertQuestions(questions)
        }
    }
}
