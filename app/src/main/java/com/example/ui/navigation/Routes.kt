package com.example.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val SUBJECTS = "subjects"
    const val SUBJECTS_YEAR = "subjects/{year}"
    const val PAPER_DETAIL = "paper_detail/{paperId}"
    const val CHAPTER_DETAIL = "chapter_detail/{chapterId}"
    const val AI_SOLVER = "ai_solver"
    const val STUDY_HUB = "study_hub"
    const val PROFILE = "profile"
    const val SEARCH = "search"
    const val QUESTION_BANK = "question_bank"
    const val SUGGESTIONS = "suggestions"
    const val LIBRARY = "library"
    const val PDF_VIEWER = "pdf_viewer/{materialId}"
    const val MCQ_EXAM = "mcq_exam"
    const val STUDY_PLANNER = "study_planner"
    const val ADMIN_PANEL = "admin_panel"
    const val ABOUT = "about"

    fun subjectsByYear(year: Int) = "subjects/$year"
    fun paperDetail(paperId: Long) = "paper_detail/$paperId"
    fun chapterDetail(chapterId: Long) = "chapter_detail/$chapterId"
    fun pdfViewer(materialId: Long) = "pdf_viewer/$materialId"
}
