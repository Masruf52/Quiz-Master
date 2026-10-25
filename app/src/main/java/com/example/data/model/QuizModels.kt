package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String,
    val profilePhoto: String = "",
    val country: String = "Global",
    val xp: Int = 0,
    val coins: Int = 100,
    val rank: Int = 0,
    val level: Int = 1,
    val winStreak: Int = 0,
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val totalQuizPlayed: Int = 0,
    val achievements: String = "", // Comma-separated list of badge/achievement IDs
    val bookmarks: String = "", // Comma-separated list of bookmarked Question IDs
    val quizHistory: String = "" // Comma-separated list of completed Quiz IDs
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val categoryId: String,
    val name: String,
    val description: String,
    val image: String = "", // Icon or background drawable name or URL
    val totalQuiz: Int = 0,
    val totalQuestions: Int = 0,
    val difficulty: String = "Medium",
    val createdDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "quizzes")
data class Quiz(
    @PrimaryKey val quizId: String,
    val categoryId: String,
    val name: String,
    val description: String,
    val image: String = "",
    val totalQuestions: Int = 10,
    val difficulty: String = "Medium", // Easy, Medium, Hard, Expert
    val estimatedTime: Int = 5 // in minutes
)

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey val questionId: String,
    val quizId: String,
    val categoryId: String,
    val question: String,
    val image: String = "", // Optional image for Image Quiz
    val audio: String = "", // Optional audio for Audio Quiz
    val options: String, // Comma-separated or serialized list of options (for multiple choice)
    val correctAnswer: String, // Correct option text or value
    val explanation: String = "",
    val difficulty: String = "Medium",
    val timeLimit: Int = 20, // in seconds
    val marks: Int = 10,
    val negativeMarks: Int = 0,
    val type: String = "multiple_choice", // multiple_choice, true_false, image_quiz, audio_quiz, fill_blank
    val tags: String = "" // comma-separated tags
)

@Entity(tableName = "leaderboard")
data class LeaderboardEntry(
    @PrimaryKey val entryId: String,
    val uid: String,
    val name: String,
    val profilePhoto: String = "",
    val country: String = "Global",
    val xp: Int = 0,
    val level: Int = 1,
    val rank: Int = 0,
    val timeframe: String = "weekly", // weekly, monthly, all_time
    val scope: String = "global" // global, country, friends
)

@Entity(tableName = "user_history")
data class UserHistory(
    @PrimaryKey val historyId: String,
    val uid: String,
    val quizId: String,
    val quizName: String,
    val score: Int = 0,
    val accuracy: Double = 0.0,
    val xpEarned: Int = 0,
    val coinsEarned: Int = 0,
    val datePlayed: Long = System.currentTimeMillis(),
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val totalQuestions: Int = 0
)

@Entity(tableName = "reward_activities")
data class RewardActivity(
    @PrimaryKey val activityId: String,
    val uid: String,
    val type: String, // "LEVEL_UP", "QUIZ_REWARD", "DAILY_LOGIN", "LUCKY_SPIN", "WELCOME_BONUS", "ACHIEVEMENT_UNLOCKED"
    val title: String,
    val description: String,
    val coinsEarned: Int,
    val xpEarned: Int,
    val dateLogged: Long = System.currentTimeMillis()
)

