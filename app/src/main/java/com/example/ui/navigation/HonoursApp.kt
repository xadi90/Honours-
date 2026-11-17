package com.example.ui.navigation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.ui.components.OfflineStatusBanner
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AiSolverScreen
import com.example.ui.screens.ChapterDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.McqExamScreen
import com.example.ui.screens.PaperDetailScreen
import com.example.ui.screens.PdfViewerScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QuestionBankScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StudyHubScreen
import com.example.ui.screens.StudyPlannerScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.screens.SuggestionsScreen
import com.example.ui.viewmodel.AiSolverViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.McqExamViewModel

private data class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val topLevelDestinations = listOf(
    TopLevelDestination(
        route = Routes.HOME,
        labelRes = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "bottom_nav_home"
    ),
    TopLevelDestination(
        route = Routes.SUBJECTS,
        labelRes = R.string.nav_subjects,
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook,
        testTag = "bottom_nav_subjects"
    ),
    TopLevelDestination(
        route = Routes.AI_SOLVER,
        labelRes = R.string.nav_ai_solver,
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
        testTag = "bottom_nav_ai"
    ),
    TopLevelDestination(
        route = Routes.STUDY_HUB,
        labelRes = R.string.nav_study,
        selectedIcon = Icons.Filled.School,
        unselectedIcon = Icons.Outlined.School,
        testTag = "bottom_nav_study"
    ),
    TopLevelDestination(
        route = Routes.PROFILE,
        labelRes = R.string.nav_profile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTag = "bottom_nav_profile"
    )
)

@Composable
fun HonoursApp(
    mainViewModel: MainViewModel = viewModel(),
    aiSolverViewModel: AiSolverViewModel = viewModel(),
    mcqExamViewModel: McqExamViewModel = viewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }

    val isOnline by mainViewModel.isOnline.collectAsStateWithLifecycle()
    val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
    val profile by mainViewModel.studentProfile.collectAsStateWithLifecycle()
    val notificationPrefs by mainViewModel.notificationPrefs.collectAsStateWithLifecycle()
    val aiSettings by mainViewModel.aiSettings.collectAsStateWithLifecycle()
    val studyPlannerPrefs by mainViewModel.studyPlannerPrefs.collectAsStateWithLifecycle()

    val papers by mainViewModel.allPapers.collectAsStateWithLifecycle()
    val chapters by mainViewModel.allChapters.collectAsStateWithLifecycle()
    val questions by mainViewModel.allQuestions.collectAsStateWithLifecycle()
    val bookmarkedQuestions by mainViewModel.bookmarkedQuestions.collectAsStateWithLifecycle()
    val savedOfflineQuestions by mainViewModel.savedOfflineQuestions.collectAsStateWithLifecycle()
    val pastExamQuestions by mainViewModel.pastExamQuestions.collectAsStateWithLifecycle()
    val allMaterials by mainViewModel.allStudyMaterials.collectAsStateWithLifecycle()
    val recentMaterials by mainViewModel.recentMaterials.collectAsStateWithLifecycle()
    val offlineMaterials by mainViewModel.offlineMaterials.collectAsStateWithLifecycle()
    val suggestions by mainViewModel.suggestions.collectAsStateWithLifecycle()
    val announcements by mainViewModel.announcements.collectAsStateWithLifecycle()
    val studyGoals by mainViewModel.studyGoals.collectAsStateWithLifecycle()
    val examResults by mainViewModel.examResults.collectAsStateWithLifecycle()
    val searchHistory by mainViewModel.searchHistory.collectAsStateWithLifecycle()
    val searchQuery by mainViewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by mainViewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by mainViewModel.isSearching.collectAsStateWithLifecycle()
    val offlineStorageKb by mainViewModel.offlineStorageKb.collectAsStateWithLifecycle()

    val aiUiState by aiSolverViewModel.uiState.collectAsStateWithLifecycle()
    val aiMessages by aiSolverViewModel.activeMessages.collectAsStateWithLifecycle()
    val aiConversations by aiSolverViewModel.conversations.collectAsStateWithLifecycle()

    val allMcqs by mcqExamViewModel.allMcqs.collectAsStateWithLifecycle()
    val mcqSession by mcqExamViewModel.session.collectAsStateWithLifecycle()

    val showTopLevelChrome = currentRoute in setOf(
        Routes.HOME,
        Routes.SUBJECTS,
        Routes.SUBJECTS_YEAR,
        Routes.AI_SOLVER,
        Routes.STUDY_HUB,
        Routes.PROFILE
    )

    BackHandler(enabled = currentRoute == Routes.HOME) {
        showExitDialog = true
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (showTopLevelChrome && isWideScreen) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    topLevelDestinations.forEach { dest ->
                        val selected = isTopLevelSelected(currentRoute, dest.route)
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigateTopLevel(navController, dest.route) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                    contentDescription = stringResource(dest.labelRes)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(dest.labelRes),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier.testTag(dest.testTag)
                        )
                    }
                }
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                contentWindowInsets = WindowInsets(0.dp),
                bottomBar = {
                    if (showTopLevelChrome && !isWideScreen) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            topLevelDestinations.forEach { dest ->
                                val selected = isTopLevelSelected(currentRoute, dest.route)
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTopLevel(navController, dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = stringResource(dest.labelRes)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(dest.labelRes),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                val navModifier = if (currentRoute != Routes.SPLASH && (!showTopLevelChrome || isWideScreen)) {
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .navigationBarsPadding()
                } else {
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                }

                Box(
                    modifier = navModifier,
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = if (currentRoute == Routes.SPLASH) {
                            Modifier.fillMaxSize()
                        } else {
                            Modifier
                                .fillMaxSize()
                                .widthIn(max = 920.dp)
                        }
                    ) {
                        if (currentRoute != Routes.SPLASH) {
                            OfflineStatusBanner(isOnline = isOnline)
                        }

                        NavHost(
                            navController = navController,
                            startDestination = Routes.SPLASH,
                            modifier = Modifier.weight(1f)
                        ) {
                            composable(Routes.SPLASH) {
                                SplashScreen(
                                    onFinished = {
                                        navController.navigate(Routes.HOME) {
                                            popUpTo(Routes.SPLASH) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }

                            composable(Routes.HOME) {
                                HomeScreen(
                                    profile = profile,
                                    papers = papers,
                                    recentMaterials = recentMaterials,
                                    latestMaterials = allMaterials,
                                    bookmarkedQuestions = bookmarkedQuestions,
                                    announcements = announcements,
                                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                                    onOpenAiSolver = { navigateTopLevel(navController, Routes.AI_SOLVER) },
                                    onOpenYear = { year -> navController.navigate(Routes.subjectsByYear(year)) },
                                    onOpenQuestionBank = { navController.navigate(Routes.QUESTION_BANK) },
                                    onOpenSuggestions = { navController.navigate(Routes.SUGGESTIONS) },
                                    onOpenMcqExam = { navController.navigate(Routes.MCQ_EXAM) },
                                    onOpenLibrary = { navController.navigate(Routes.LIBRARY) },
                                    onOpenStudyPlanner = { navController.navigate(Routes.STUDY_PLANNER) },
                                    onOpenMaterial = { material ->
                                        mainViewModel.openMaterial(material)
                                        navController.navigate(Routes.pdfViewer(material.id))
                                    }
                                )
                            }

                            composable(Routes.SUBJECTS) {
                                SubjectsScreen(
                                    initialYear = profile.academicYear,
                                    papers = papers,
                                    chapters = chapters,
                                    onPaperClick = { paperId -> navController.navigate(Routes.paperDetail(paperId)) },
                                    onOpenSearch = { navController.navigate(Routes.SEARCH) }
                                )
                            }

                            composable(
                                route = Routes.SUBJECTS_YEAR,
                                arguments = listOf(navArgument("year") { type = NavType.IntType })
                            ) { entry ->
                                val year = entry.arguments?.getInt("year") ?: 1
                                SubjectsScreen(
                                    initialYear = year,
                                    papers = papers,
                                    chapters = chapters,
                                    onPaperClick = { paperId -> navController.navigate(Routes.paperDetail(paperId)) },
                                    onOpenSearch = { navController.navigate(Routes.SEARCH) }
                                )
                            }

                            composable(
                                route = Routes.PAPER_DETAIL,
                                arguments = listOf(navArgument("paperId") { type = NavType.LongType })
                            ) { entry ->
                                val paperId = entry.arguments?.getLong("paperId") ?: 0L
                                val paper by remember(paperId) {
                                    mainViewModel.observePaperById(paperId)
                                }.collectAsStateWithLifecycle(initialValue = papers.find { it.id == paperId })
                                val paperChapters by remember(paperId) {
                                    mainViewModel.observeChaptersByPaper(paperId)
                                }.collectAsStateWithLifecycle(initialValue = emptyList())
                                val paperQuestions by remember(paperId) {
                                    mainViewModel.observeQuestionsByPaper(paperId)
                                }.collectAsStateWithLifecycle(initialValue = emptyList())

                                PaperDetailScreen(
                                    paper = paper,
                                    chapters = paperChapters,
                                    questionCount = paperQuestions.size,
                                    onBack = { navController.popBackStack() },
                                    onChapterClick = { chId -> navController.navigate(Routes.chapterDetail(chId)) },
                                    onToggleChapterComplete = mainViewModel::toggleChapterCompleted,
                                    onStartPaperMcq = { selectedPaper ->
                                        mcqExamViewModel.startExam(
                                            titleBn = "${selectedPaper.code} • ${selectedPaper.titleBn}",
                                            paperId = selectedPaper.id,
                                            chapterId = null,
                                            timedMode = true
                                        )
                                        navController.navigate(Routes.MCQ_EXAM)
                                    }
                                )
                            }

                            composable(
                                route = Routes.CHAPTER_DETAIL,
                                arguments = listOf(navArgument("chapterId") { type = NavType.LongType })
                            ) { entry ->
                                val chapterId = entry.arguments?.getLong("chapterId") ?: 0L
                                val chapter by remember(chapterId) {
                                    mainViewModel.observeChapterById(chapterId)
                                }.collectAsStateWithLifecycle(initialValue = chapters.find { it.id == chapterId })
                                val paper = remember(chapter, papers) {
                                    papers.find { it.id == chapter?.paperId }
                                }
                                val chapterQuestions by remember(chapterId) {
                                    mainViewModel.observeQuestionsByChapter(chapterId)
                                }.collectAsStateWithLifecycle(initialValue = emptyList())
                                val chapterMcqs by remember(chapterId) {
                                    mainViewModel.observeMcqsByChapter(chapterId)
                                }.collectAsStateWithLifecycle(initialValue = emptyList())

                                ChapterDetailScreen(
                                    paper = paper,
                                    chapter = chapter,
                                    questions = chapterQuestions,
                                    mcqs = chapterMcqs,
                                    onBack = { navController.popBackStack() },
                                    onToggleBookmark = mainViewModel::toggleQuestionBookmark,
                                    onToggleSaveOffline = mainViewModel::toggleQuestionSavedOffline,
                                    onStartChapterMcq = { ch ->
                                        mcqExamViewModel.startExam(
                                            titleBn = "অধ্যায় ${ch.chapterNumber}: ${ch.titleBn}",
                                            paperId = ch.paperId,
                                            chapterId = ch.id,
                                            timedMode = false
                                        )
                                        navController.navigate(Routes.MCQ_EXAM)
                                    },
                                    onAskAi = {
                                        chapter?.let { ch ->
                                            paper?.let { p ->
                                                aiSolverViewModel.selectYear(p.yearNumber, "${p.titleBn} (${p.code})")
                                            }
                                            aiSolverViewModel.updateQuestionInput("অধ্যায়: ${ch.titleBn} — ")
                                        }
                                        navigateTopLevel(navController, Routes.AI_SOLVER)
                                    }
                                )
                            }

                            composable(Routes.AI_SOLVER) {
                                AiSolverScreen(
                                    viewModel = aiSolverViewModel,
                                    uiState = aiUiState,
                                    messages = aiMessages,
                                    conversations = aiConversations,
                                    papers = papers,
                                    isImageSolverEnabled = aiSettings.imageSolverEnabled
                                )
                            }

                            composable(Routes.STUDY_HUB) {
                                StudyHubScreen(
                                    pastQuestionCount = pastExamQuestions.size,
                                    suggestionCount = suggestions.size,
                                    materialCount = allMaterials.size,
                                    mcqCount = allMcqs.size,
                                    activeGoalCount = studyGoals.count { !it.isCompleted },
                                    onOpenQuestionBank = { navController.navigate(Routes.QUESTION_BANK) },
                                    onOpenSuggestions = { navController.navigate(Routes.SUGGESTIONS) },
                                    onOpenLibrary = { navController.navigate(Routes.LIBRARY) },
                                    onOpenMcqExam = { navController.navigate(Routes.MCQ_EXAM) },
                                    onOpenStudyPlanner = { navController.navigate(Routes.STUDY_PLANNER) }
                                )
                            }

                            composable(Routes.PROFILE) {
                                ProfileScreen(
                                    profile = profile,
                                    themeMode = themeMode,
                                    notificationPrefs = notificationPrefs,
                                    bookmarkedQuestions = bookmarkedQuestions,
                                    savedQuestions = savedOfflineQuestions,
                                    offlineMaterials = offlineMaterials,
                                    aiConversations = aiConversations,
                                    chapters = chapters,
                                    offlineStorageKb = offlineStorageKb,
                                    onUpdateProfile = mainViewModel::updateProfile,
                                    onUpdateAvatar = mainViewModel::updateAvatarUri,
                                    onLogoutToGuest = mainViewModel::logoutToGuest,
                                    onSetThemeMode = mainViewModel::setThemeMode,
                                    onUpdateNotificationPrefs = mainViewModel::updateNotificationPrefs,
                                    onOpenQuestionBank = { navController.navigate(Routes.QUESTION_BANK) },
                                    onOpenLibrary = { navController.navigate(Routes.LIBRARY) },
                                    onOpenAiSolver = { navigateTopLevel(navController, Routes.AI_SOLVER) },
                                    onOpenAdminPanel = { navController.navigate(Routes.ADMIN_PANEL) },
                                    onOpenAbout = { navController.navigate(Routes.ABOUT) }
                                )
                            }

                            composable(Routes.SEARCH) {
                                SearchScreen(
                                    query = searchQuery,
                                    isSearching = isSearching,
                                    results = searchResults,
                                    history = searchHistory,
                                    onQueryChange = mainViewModel::performSearch,
                                    onClearHistory = mainViewModel::clearSearchHistory,
                                    onPaperClick = { paperId -> navController.navigate(Routes.paperDetail(paperId)) },
                                    onChapterClick = { chId -> navController.navigate(Routes.chapterDetail(chId)) },
                                    onToggleBookmark = mainViewModel::toggleQuestionBookmark,
                                    onToggleSaveOffline = mainViewModel::toggleQuestionSavedOffline,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(Routes.QUESTION_BANK) {
                                QuestionBankScreen(
                                    questions = questions,
                                    papers = papers,
                                    onBack = { navController.popBackStack() },
                                    onToggleBookmark = mainViewModel::toggleQuestionBookmark,
                                    onToggleSaveOffline = mainViewModel::toggleQuestionSavedOffline
                                )
                            }

                            composable(Routes.SUGGESTIONS) {
                                SuggestionsScreen(
                                    suggestions = suggestions,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(Routes.LIBRARY) {
                                LibraryScreen(
                                    materials = allMaterials,
                                    offlineStorageKb = offlineStorageKb,
                                    onOpenMaterial = { material ->
                                        mainViewModel.openMaterial(material)
                                        navController.navigate(Routes.pdfViewer(material.id))
                                    },
                                    onToggleDownload = mainViewModel::toggleMaterialDownload,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(
                                route = Routes.PDF_VIEWER,
                                arguments = listOf(navArgument("materialId") { type = NavType.LongType })
                            ) { entry ->
                                val materialId = entry.arguments?.getLong("materialId") ?: 0L
                                val material by remember(materialId) {
                                    mainViewModel.observeMaterialById(materialId)
                                }.collectAsStateWithLifecycle(initialValue = allMaterials.find { it.id == materialId })

                                PdfViewerScreen(
                                    material = material,
                                    onBack = { navController.popBackStack() },
                                    onBookmarkPage = mainViewModel::bookmarkMaterialPage,
                                    onToggleDownload = mainViewModel::toggleMaterialDownload
                                )
                            }

                            composable(Routes.MCQ_EXAM) {
                                McqExamScreen(
                                    viewModel = mcqExamViewModel,
                                    session = mcqSession,
                                    papers = papers,
                                    examResults = examResults,
                                    totalMcqCount = allMcqs.size,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(Routes.STUDY_PLANNER) {
                                StudyPlannerScreen(
                                    goals = studyGoals,
                                    chapters = chapters,
                                    examResults = examResults,
                                    plannerPrefs = studyPlannerPrefs,
                                    onAddGoal = mainViewModel::addStudyGoal,
                                    onToggleGoal = mainViewModel::toggleStudyGoal,
                                    onDeleteGoal = mainViewModel::deleteStudyGoal,
                                    onUpdatePlannerPrefs = mainViewModel::updateStudyPlannerPrefs,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(Routes.ADMIN_PANEL) {
                                AdminPanelScreen(
                                    papers = papers,
                                    chapters = chapters,
                                    questionCount = questions.size,
                                    mcqCount = allMcqs.size,
                                    aiSettings = aiSettings,
                                    onAddPaper = mainViewModel::adminAddPaper,
                                    onAddChapter = mainViewModel::adminAddChapter,
                                    onAddQuestion = mainViewModel::adminAddQuestion,
                                    onAddSuggestion = mainViewModel::adminAddSuggestion,
                                    onAddAnnouncement = mainViewModel::adminAddAnnouncement,
                                    onUpdateAiSettings = mainViewModel::adminUpdateAiSettings,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable(Routes.ABOUT) {
                                AboutScreen(onBack = { navController.popBackStack() })
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.exit_confirm_title)) },
            text = { Text(stringResource(R.string.exit_confirm_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        (context as? Activity)?.finish()
                    },
                    modifier = Modifier.testTag("exit_confirm_button")
                ) {
                    Text(stringResource(R.string.action_exit))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitDialog = false },
                    modifier = Modifier.testTag("exit_cancel_button")
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

private fun isTopLevelSelected(currentRoute: String?, destRoute: String): Boolean {
    if (destRoute == Routes.SUBJECTS && currentRoute == Routes.SUBJECTS_YEAR) return true
    return currentRoute == destRoute
}

private fun navigateTopLevel(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(Routes.HOME) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
