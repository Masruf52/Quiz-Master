package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.Category
import com.example.data.model.LeaderboardEntry
import com.example.data.model.Question
import com.example.data.model.Quiz
import com.example.data.model.User
import com.example.data.model.UserHistory
import com.example.data.model.RewardActivity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid = :uid")
    fun getUserFlow(uid: String): Flow<User?>

    @Query("SELECT * FROM users WHERE uid = :uid")
    suspend fun getUserSync(uid: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("DELETE FROM categories WHERE categoryId = :categoryId")
    suspend fun deleteCategory(categoryId: String)
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quizzes")
    fun getAllQuizzesFlow(): Flow<List<Quiz>>

    @Query("SELECT * FROM quizzes WHERE categoryId = :categoryId")
    fun getQuizzesByCategoryFlow(categoryId: String): Flow<List<Quiz>>

    @Query("SELECT * FROM quizzes WHERE quizId = :quizId")
    suspend fun getQuizById(quizId: String): Quiz?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizzes(quizzes: List<Quiz>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: Quiz)

    @Query("DELETE FROM quizzes WHERE quizId = :quizId")
    suspend fun deleteQuiz(quizId: String)
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE quizId = :quizId")
    suspend fun getQuestionsForQuiz(quizId: String): List<Question>

    @Query("SELECT * FROM questions WHERE questionId = :questionId")
    suspend fun getQuestionById(questionId: String): Question?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question)

    @Query("DELETE FROM questions WHERE questionId = :questionId")
    suspend fun deleteQuestion(questionId: String)

    @Query("DELETE FROM questions WHERE quizId = :quizId")
    suspend fun deleteQuestionsForQuiz(quizId: String)
}

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leaderboard WHERE timeframe = :timeframe AND scope = :scope ORDER BY xp DESC")
    fun getLeaderboardFlow(timeframe: String, scope: String): Flow<List<LeaderboardEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboard(entries: List<LeaderboardEntry>)
}

@Dao
interface UserHistoryDao {
    @Query("SELECT * FROM user_history WHERE uid = :uid ORDER BY datePlayed DESC")
    fun getHistoryForUserFlow(uid: String): Flow<List<UserHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: UserHistory)
}

@Dao
interface RewardActivityDao {
    @Query("SELECT * FROM reward_activities WHERE uid = :uid ORDER BY dateLogged DESC")
    fun getRewardActivitiesForUserFlow(uid: String): Flow<List<RewardActivity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewardActivity(activity: RewardActivity)
}

@Database(
    entities = [
        User::class,
        Category::class,
        Quiz::class,
        Question::class,
        LeaderboardEntry::class,
        UserHistory::class,
        RewardActivity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class QuizDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun quizDao(): QuizDao
    abstract fun questionDao(): QuestionDao
    abstract fun leaderboardDao(): LeaderboardDao
    abstract fun userHistoryDao(): UserHistoryDao
    abstract fun rewardActivityDao(): RewardActivityDao
}
