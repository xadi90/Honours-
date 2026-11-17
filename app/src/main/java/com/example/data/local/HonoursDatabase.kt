package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Database(
    entities = [
        PaperEntity::class,
        ChapterEntity::class,
        QuestionAnswerEntity::class,
        McqEntity::class,
        ExamResultEntity::class,
        StudyMaterialEntity::class,
        SuggestionEntity::class,
        AnnouncementEntity::class,
        StudyGoalEntity::class,
        AiConversationEntity::class,
        AiMessageEntity::class,
        SearchHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HonoursDatabase : RoomDatabase() {
    abstract fun academicDao(): AcademicDao
    abstract fun mcqAndStudyDao(): McqAndStudyDao
    abstract fun aiAndSearchDao(): AiAndSearchDao

    companion object {
        @Volatile
        private var INSTANCE: HonoursDatabase? = null
        private val seedMutex = Mutex()

        fun getInstance(context: Context): HonoursDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HonoursDatabase::class.java,
                    "honours_solution_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        suspend fun ensureSeeded(db: HonoursDatabase) = withContext(Dispatchers.IO) {
            seedMutex.withLock {
                if (db.academicDao().countPapers() == 0) {
                    SeedData.populate(db)
                }
            }
        }
    }
}
