package com.example.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "honours_prefs")

data class StudentProfileState(
    val studentName: String = "অতিথি শিক্ষার্থী",
    val academicYear: Int = 1,
    val collegeName: String = "জাতীয় বিশ্ববিদ্যালয় অধিভুক্ত কলেজ",
    val department: String = "রাষ্ট্রবিজ্ঞান (Political Science)",
    val email: String = "",
    val avatarUri: String = "",
    val isGuestMode: Boolean = true
)

data class NotificationPrefs(
    val newNotes: Boolean = true,
    val newSuggestions: Boolean = true,
    val examNotices: Boolean = true,
    val studyReminders: Boolean = true
)

data class AiSettingsState(
    val dailyLimit: Int = 25,
    val usedToday: Int = 0,
    val imageSolverEnabled: Boolean = true,
    val providerLabel: String = "Firebase AI Logic (Gemini 3.8 Flash — Preview Sample Mode)"
)

data class StudyPlannerPrefs(
    val dailyGoalMinutes: Int = 90,
    val examCountdownTitle: String = "অনার্স চূড়ান্ত পরীক্ষা ২০২৬",
    val examTargetDateMillis: Long = System.currentTimeMillis() + 45L * 24 * 60 * 60 * 1000L
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val STUDENT_NAME = stringPreferencesKey("student_name")
        val ACADEMIC_YEAR = intPreferencesKey("academic_year")
        val COLLEGE_NAME = stringPreferencesKey("college_name")
        val DEPARTMENT = stringPreferencesKey("department")
        val STUDENT_EMAIL = stringPreferencesKey("student_email")
        val AVATAR_URI = stringPreferencesKey("avatar_uri")
        val IS_GUEST_MODE = booleanPreferencesKey("is_guest_mode")

        val NOTIF_NOTES = booleanPreferencesKey("notif_notes")
        val NOTIF_SUGGESTIONS = booleanPreferencesKey("notif_suggestions")
        val NOTIF_EXAMS = booleanPreferencesKey("notif_exams")
        val NOTIF_REMINDERS = booleanPreferencesKey("notif_reminders")

        val AI_DAILY_LIMIT = intPreferencesKey("ai_daily_limit")
        val AI_USED_TODAY = intPreferencesKey("ai_used_today")
        val AI_IMAGE_ENABLED = booleanPreferencesKey("ai_image_enabled")

        val DAILY_GOAL_MINUTES = intPreferencesKey("daily_goal_minutes")
        val EXAM_COUNTDOWN_TITLE = stringPreferencesKey("exam_countdown_title")
        val EXAM_TARGET_MILLIS = longPreferencesKey("exam_target_millis")
    }

    val themeMode: Flow<AppThemeMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
        AppThemeMode.entries.firstOrNull { it.name == raw } ?: AppThemeMode.SYSTEM
    }

    val studentProfile: Flow<StudentProfileState> = context.dataStore.data.map { prefs ->
        StudentProfileState(
            studentName = prefs[Keys.STUDENT_NAME] ?: "অতিথি শিক্ষার্থী",
            academicYear = prefs[Keys.ACADEMIC_YEAR] ?: 1,
            collegeName = prefs[Keys.COLLEGE_NAME] ?: "জাতীয় বিশ্ববিদ্যালয় অধিভুক্ত কলেজ",
            department = prefs[Keys.DEPARTMENT] ?: "রাষ্ট্রবিজ্ঞান (Political Science)",
            email = prefs[Keys.STUDENT_EMAIL] ?: "",
            avatarUri = prefs[Keys.AVATAR_URI] ?: "",
            isGuestMode = prefs[Keys.IS_GUEST_MODE] ?: true
        )
    }

    val notificationPrefs: Flow<NotificationPrefs> = context.dataStore.data.map { prefs ->
        NotificationPrefs(
            newNotes = prefs[Keys.NOTIF_NOTES] ?: true,
            newSuggestions = prefs[Keys.NOTIF_SUGGESTIONS] ?: true,
            examNotices = prefs[Keys.NOTIF_EXAMS] ?: true,
            studyReminders = prefs[Keys.NOTIF_REMINDERS] ?: true
        )
    }

    val aiSettings: Flow<AiSettingsState> = context.dataStore.data.map { prefs ->
        AiSettingsState(
            dailyLimit = prefs[Keys.AI_DAILY_LIMIT] ?: 25,
            usedToday = prefs[Keys.AI_USED_TODAY] ?: 0,
            imageSolverEnabled = prefs[Keys.AI_IMAGE_ENABLED] ?: true
        )
    }

    val studyPlannerPrefs: Flow<StudyPlannerPrefs> = context.dataStore.data.map { prefs ->
        StudyPlannerPrefs(
            dailyGoalMinutes = prefs[Keys.DAILY_GOAL_MINUTES] ?: 90,
            examCountdownTitle = prefs[Keys.EXAM_COUNTDOWN_TITLE] ?: "অনার্স চূড়ান্ত পরীক্ষা ২০২৬",
            examTargetDateMillis = prefs[Keys.EXAM_TARGET_MILLIS]
                ?: (System.currentTimeMillis() + 45L * 24 * 60 * 60 * 1000L)
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun updateProfile(
        name: String,
        year: Int,
        college: String,
        email: String,
        isGuest: Boolean
    ) {
        context.dataStore.edit {
            it[Keys.STUDENT_NAME] = name.ifBlank { "অতিথি শিক্ষার্থী" }
            it[Keys.ACADEMIC_YEAR] = year.coerceIn(1, 4)
            it[Keys.COLLEGE_NAME] = college.ifBlank { "জাতীয় বিশ্ববিদ্যালয় অধিভুক্ত কলেজ" }
            it[Keys.STUDENT_EMAIL] = email
            it[Keys.IS_GUEST_MODE] = isGuest
        }
    }

    suspend fun updateAvatarUri(uri: String) {
        context.dataStore.edit { it[Keys.AVATAR_URI] = uri }
    }

    suspend fun logoutToGuest() {
        context.dataStore.edit {
            it[Keys.STUDENT_NAME] = "অতিথি শিক্ষার্থী"
            it[Keys.STUDENT_EMAIL] = ""
            it[Keys.IS_GUEST_MODE] = true
        }
    }

    suspend fun updateNotificationPrefs(prefsValue: NotificationPrefs) {
        context.dataStore.edit {
            it[Keys.NOTIF_NOTES] = prefsValue.newNotes
            it[Keys.NOTIF_SUGGESTIONS] = prefsValue.newSuggestions
            it[Keys.NOTIF_EXAMS] = prefsValue.examNotices
            it[Keys.NOTIF_REMINDERS] = prefsValue.studyReminders
        }
    }

    suspend fun incrementAiUsage() {
        context.dataStore.edit {
            val current = it[Keys.AI_USED_TODAY] ?: 0
            it[Keys.AI_USED_TODAY] = current + 1
        }
    }

    suspend fun updateAiAdminSettings(dailyLimit: Int, imageEnabled: Boolean) {
        context.dataStore.edit {
            it[Keys.AI_DAILY_LIMIT] = dailyLimit.coerceIn(5, 500)
            it[Keys.AI_IMAGE_ENABLED] = imageEnabled
        }
    }

    suspend fun updateStudyPlannerPrefs(dailyGoalMinutes: Int, examTitle: String, examDateMillis: Long) {
        context.dataStore.edit {
            it[Keys.DAILY_GOAL_MINUTES] = dailyGoalMinutes.coerceIn(15, 600)
            it[Keys.EXAM_COUNTDOWN_TITLE] = examTitle.ifBlank { "অনার্স চূড়ান্ত পরীক্ষা" }
            it[Keys.EXAM_TARGET_MILLIS] = examDateMillis
        }
    }
}
