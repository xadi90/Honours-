package com.example.data.model

/**
 * Permanent, fixed, and unchangeable application & developer metadata.
 * Cannot be modified by users, content editors, or administrators.
 */
object DeveloperConstants {
    const val APP_NAME = "Honours Solution"
    const val APP_SUBTITLE_BN = "অনার্স রাষ্ট্রবিজ্ঞান সম্পূর্ণ সমাধান"
    const val SPLASH_TAGLINE_BN = "আপনার অনার্স পড়াশোনার নির্ভরযোগ্য সঙ্গী"
    const val UNIVERSITY = "National University, Bangladesh"
    const val DEPARTMENT = "Political Science (রাষ্ট্রবিজ্ঞান)"
    const val DEVELOPER_NAME = "Xadi Ahmed"
    const val DEVELOPER_PHONE = "01878864913"
    const val DEVELOPER_EMAIL = "xadiahmed23@gmail.com"
    const val APP_VERSION = "1.0.0"
    const val COPYRIGHT = "© 2026 Xadi Ahmed • Honours Solution. All rights reserved."
    const val SUGGESTION_DISCLAIMER_BN =
        "সতর্কীকরণ: এখানে প্রদত্ত সাজেশনসমূহ পূর্ববর্তী পরীক্ষার প্রশ্নপত্র ও সিলেবাস বিশ্লেষণের ভিত্তিতে প্রস্তুতিমূলক সহায়িকা হিসেবে প্রকাশিত। এগুলো চূড়ান্ত পরীক্ষায় প্রশ্ন আসার শতভাগ নিশ্চয়তা প্রদান করে না।"
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val canRetry: Boolean = true) : UiState<Nothing>
}

enum class AcademicYearLevel(
    val yearNumber: Int,
    val titleEn: String,
    val titleBn: String,
    val shortBn: String
) {
    FIRST_YEAR(1, "Honours 1st Year", "অনার্স ১ম বর্ষ", "১ম বর্ষ"),
    SECOND_YEAR(2, "Honours 2nd Year", "অনার্স ২য় বর্ষ", "২য় বর্ষ"),
    THIRD_YEAR(3, "Honours 3rd Year", "অনার্স ৩য় বর্ষ", "৩য় বর্ষ"),
    FOURTH_YEAR(4, "Honours 4th Year", "অনার্স ৪র্থ বর্ষ", "৪র্থ বর্ষ");

    companion object {
        fun fromYear(year: Int): AcademicYearLevel =
            entries.firstOrNull { it.yearNumber == year } ?: FIRST_YEAR
    }
}

enum class QuestionCategory(val labelBn: String, val labelEn: String) {
    VERY_SHORT("অতি সংক্ষিপ্ত প্রশ্ন", "Very Short"),
    SHORT("সংক্ষিপ্ত প্রশ্ন", "Short"),
    BROAD("রচনামূলক প্রশ্ন", "Broad Essay"),
    ANALYTICAL("বিশ্লেষণধর্মী প্রশ্ন", "Analytical"),
    IMPORTANT("গুরুত্বপূর্ণ প্রশ্ন", "Important"),
    PREVIOUS_YEAR("বিগত সালের প্রশ্ন", "Past Exam");

    companion object {
        fun fromName(name: String): QuestionCategory =
            entries.firstOrNull { it.name == name } ?: SHORT
    }
}

enum class AnswerLengthOption(val labelBn: String, val descriptionBn: String) {
    VERY_SHORT("অতি সংক্ষিপ্ত", "১–২ বাক্যে মূল উত্তর (পার্শ্বনোটের জন্য)"),
    SHORT("সংক্ষিপ্ত", "৪–৫ মিনিটে লেখার উপযোগী পয়েন্টভিত্তিক উত্তর"),
    MEDIUM("মাঝারি", "৮–১০ পয়েন্টে সুসংগঠিত বিশ্লেষণমূলক উত্তর"),
    DETAILED("বিস্তারিত", "সম্পূর্ণ একাডেমিক কাঠামোসহ গভীর আলোচনা"),
    EXAM_READY("পরীক্ষার উপযোগী", "জাতীয় বিশ্ববিদ্যালয়ের ১০ নম্বরের রচনামূলক ফরম্যাটে")
}

enum class AiQuickTool(val labelBn: String, val instructionKey: String) {
    SIMPLIFY("সহজ ভাষায় বুঝিয়ে দিন", "simplify"),
    EXPLAIN_DETAIL("বিস্তারিত ব্যাখ্যা করুন", "detail"),
    WITH_EXAMPLES("উদাহরণসহ বুঝিয়ে দিন", "examples"),
    TRANSLATE_EN("ইংরেজিতে অনুবাদ", "translate_en"),
    TRANSLATE_BN("বাংলায় অনুবাদ", "translate_bn"),
    KEY_POINTS("গুরুত্বপূর্ণ পয়েন্ট তৈরি করুন", "key_points"),
    GENERATE_MCQ("MCQ তৈরি করুন", "mcq"),
    EXTRA_QUESTIONS("অতিরিক্ত প্রশ্ন তৈরি করুন", "extra_questions"),
    SHORTEN("উত্তর সংক্ষিপ্ত করুন", "shorten"),
    EXPAND("উত্তর আরও বিস্তারিত করুন", "expand")
}

enum class SuggestionCategory(val labelBn: String) {
    IMPORTANT("গুরুত্বপূর্ণ প্রশ্ন"),
    CHAPTER_WISE("অধ্যায়ভিত্তিক সাজেশন"),
    SHORT("সংক্ষিপ্ত সাজেশন"),
    BROAD("রচনামূলক সাজেশন"),
    FINAL_PREP("চূড়ান্ত পরীক্ষা প্রস্তুতি");

    companion object {
        fun fromName(name: String): SuggestionCategory =
            entries.firstOrNull { it.name == name } ?: IMPORTANT
    }
}

enum class MaterialType(val labelBn: String) {
    PDF("PDF"),
    NOTE("নোটস"),
    IMAGE("ছবি / চার্ট")
}

enum class AdminRole(val titleEn: String, val titleBn: String, val canEditStructure: Boolean, val canManageSettings: Boolean) {
    SUPER_ADMIN("Super Admin", "সুপার অ্যাডমিন", true, true),
    CONTENT_MANAGER("Content Manager", "কনটেন্ট ম্যানেজার", true, false),
    CONTENT_EDITOR("Content Editor", "কনটেন্ট এডিটর", false, false)
}
