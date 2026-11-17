package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.AiSolveRequest
import com.example.data.ai.AiStreamEvent
import com.example.data.ai.PlaceholderAiRepository
import com.example.data.local.HonoursDatabase
import com.example.data.model.AnswerLengthOption
import com.example.data.model.DeveloperConstants
import com.example.data.model.QuestionCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app metadata and permanent developer constants are intact`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("Honours Solution", context.getString(R.string.app_name))
        assertEquals("Xadi Ahmed", DeveloperConstants.DEVELOPER_NAME)
        assertEquals("01878864913", DeveloperConstants.DEVELOPER_PHONE)
        assertEquals("xadiahmed23@gmail.com", DeveloperConstants.DEVELOPER_EMAIL)
        assertEquals("National University, Bangladesh", DeveloperConstants.UNIVERSITY)
    }

    @Test
    fun `verified NU Political Science syllabus populates all 4 years`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, HonoursDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            HonoursDatabase.ensureSeeded(db)
            val papers = db.academicDao().observeAllPapers().first()
            assertEquals(30, papers.size)
            assertTrue(papers.any { it.yearNumber == 1 && it.code == "211901" })
            assertTrue(papers.any { it.yearNumber == 2 && it.code == "221901" })
            assertTrue(papers.any { it.yearNumber == 3 && it.code == "231901" })
            assertTrue(papers.any { it.yearNumber == 4 && it.code == "241901" })
        } finally {
            db.close()
        }
    }

    @Test
    fun `placeholder AI repository returns clearly labeled sample output`() = runTest {
        val aiRepo = PlaceholderAiRepository()
        assertFalse(aiRepo.isLiveGeminiConnected)
        val events = aiRepo.solveQuestionStream(
            AiSolveRequest(
                questionText = "সার্বভৌমত্ব কাকে বলে?",
                academicYear = 1,
                paperTitle = "রাজনৈতিক তত্ত্ব (211903)",
                questionType = QuestionCategory.SHORT,
                answerLength = AnswerLengthOption.SHORT
            )
        ).toList()
        val completed = events.filterIsInstance<AiStreamEvent.Completed>().firstOrNull()
        assertTrue(completed != null)
        assertTrue(completed!!.fullText.contains("নমুনা AI উত্তর"))
    }
}
