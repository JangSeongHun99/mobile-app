package com.example.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapp.data.ToeicCategoryClassifier
import com.example.myapp.data.ToeicWords
import com.example.myapp.data.UserProgress
import com.example.myapp.data.UserProgressStorage
import com.example.myapp.data.WordStorage
import com.example.myapp.models.DefaultCategories
import com.example.myapp.models.QuizMode
import com.example.myapp.models.WordEntry
import com.example.myapp.navigation.Screen
import com.example.myapp.ui.screens.BadgesScreen
import com.example.myapp.ui.screens.FlashcardScreen
import com.example.myapp.ui.screens.HomeScreen
import com.example.myapp.ui.screens.QuizScreen
import com.example.myapp.ui.screens.TypingGameScreen
import com.example.myapp.ui.screens.WordManagerScreen
import com.example.myapp.ui.theme.MyApplicationTheme
import com.example.myapp.utils.autoUnlockBadges
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WordQuizApp()
                }
            }
        }
    }
}

@Composable
fun WordQuizApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val wordStorage = remember { WordStorage(context) }
    val progressStorage = remember { UserProgressStorage(context) }
    val scope = rememberCoroutineScope()
    val words by wordStorage.wordsFlow.collectAsState(initial = emptyList())
    val userProgress by progressStorage.progressFlow.collectAsState(initial = UserProgress())

    var newTerm by rememberSaveable { mutableStateOf("") }
    var newMeaning by rememberSaveable { mutableStateOf("") }
    var newCategoryId by rememberSaveable { mutableStateOf(DefaultCategories.UNCATEGORIZED.id) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editingTerm by rememberSaveable { mutableStateOf("") }
    var editingMeaning by rememberSaveable { mutableStateOf("") }
    var editingCategoryId by rememberSaveable { mutableStateOf(DefaultCategories.UNCATEGORIZED.id) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategoryId by rememberSaveable { mutableStateOf(DefaultCategories.ALL.id) } // 전체 카테고리가 기본값

    val categories = remember { DefaultCategories.getDefaultList() }
    val categoryWordCounts = remember(words) {
        categories.associate { category ->
            val count = if (category.id == DefaultCategories.ALL.id) {
                words.size
            } else {
                words.count { it.categoryId == category.id }
            }
            category.id to count
        }
    }

    var selectedCardIndex by rememberSaveable { mutableStateOf(0) }
    var isMeaningFirst by rememberSaveable { mutableStateOf(false) }
    var isCardFlipped by rememberSaveable { mutableStateOf(false) }

    var quizMode by rememberSaveable { mutableStateOf(QuizMode.WordFirst) }
    var shuffleEnabled by rememberSaveable { mutableStateOf(true) }
    var quizPosition by rememberSaveable { mutableStateOf(0) }
    var quizSeed by rememberSaveable { mutableStateOf(0) }
    var quizRevealed by rememberSaveable { mutableStateOf(false) }

    val today = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    LaunchedEffect(Unit) {
        wordStorage.ensureSeeded(ToeicWords.defaultWords)
    }

    LaunchedEffect(words) {
        if (words.isNotEmpty() && words.any { it.categoryId == DefaultCategories.UNCATEGORIZED.id }) {
            val updated = words.map { word ->
                if (word.categoryId == DefaultCategories.UNCATEGORIZED.id) {
                    ToeicCategoryClassifier.apply(word)
                } else {
                    word
                }
            }
            if (updated != words) {
                wordStorage.setWords(updated)
            }
        }
    }

    // 카테고리 필터링된 단어 목록
    val filteredWords = remember(words, selectedCategoryId) {
        if (selectedCategoryId == "all") {
            words
        } else {
            words.filter { it.categoryId == selectedCategoryId }
        }
    }

    val quizOrder = remember(quizSeed, filteredWords.size, shuffleEnabled) {
        val indices = filteredWords.indices.toList()
        if (shuffleEnabled) {
            if (indices.isEmpty()) emptyList() else indices.shuffled(Random(quizSeed))
        } else {
            indices
        }
    }

    val currentQuizWord = quizOrder.getOrNull(quizPosition)?.let { filteredWords[it] }
    val currentQuizWordForUi = currentQuizWord?.let { target ->
        // 최신 상태(정답/오답 횟수)를 words에서 다시 찾아서 전달
        words.find { it.id == target.id } ?: target
    }

    LaunchedEffect(filteredWords.size) {
        if (selectedCardIndex >= filteredWords.size) {
            selectedCardIndex = 0
            isCardFlipped = false
        }
        if (quizPosition >= quizOrder.size) {
            quizPosition = 0
            quizRevealed = false
        }
    }

    LaunchedEffect(shuffleEnabled) {
        quizPosition = 0
        quizRevealed = false
        quizSeed++
    }

    val onCategorySelected: (String) -> Unit = { categoryId ->
        val safeId = categories.find { it.id == categoryId }?.id ?: DefaultCategories.ALL.id
        selectedCategoryId = safeId
        selectedCardIndex = 0
        isCardFlipped = false
        quizPosition = 0
        quizRevealed = false
        quizSeed++
    }

    fun goToNextQuizQuestion() {
        if (quizOrder.isEmpty()) return
        if (quizPosition + 1 >= quizOrder.size) {
            quizPosition = 0
            if (shuffleEnabled) quizSeed++
        } else {
            quizPosition += 1
        }
        quizRevealed = false
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                allWords = words,
                filteredWords = filteredWords,
                userProgress = userProgress,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = onCategorySelected,
                navController = navController,
                onNavigateToManage = {
                    if (selectedCategoryId != DefaultCategories.ALL.id) {
                        newCategoryId = selectedCategoryId
                    }
                    navController.navigate(Screen.Manage.route)
                },
                onNavigateToFlashcards = { navController.navigate(Screen.Flashcards.route) },
                onNavigateToQuiz = { navController.navigate(Screen.Quiz.route) }
            )
        }
        composable(Screen.Manage.route) {
            WordManagerScreen(
                words = words,
                categories = categories,
                newTerm = newTerm,
                newMeaning = newMeaning,
                newCategoryId = newCategoryId,
                editingId = editingId,
                editingTerm = editingTerm,
                editingMeaning = editingMeaning,
                editingCategoryId = editingCategoryId,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onNewTermChange = { newTerm = it },
                onNewMeaningChange = { newMeaning = it },
                onNewCategoryChange = { newCategoryId = it },
                onAddWord = {
                    val trimmedTerm = newTerm.trim()
                    val trimmedMeaning = newMeaning.trim()
                    if (trimmedTerm.isNotEmpty() && trimmedMeaning.isNotEmpty()) {
                        val nextId = (words.maxOfOrNull { it.id } ?: 0L) + 1L
                        val categoryForNew = when (newCategoryId) {
                            DefaultCategories.ALL.id -> DefaultCategories.UNCATEGORIZED.id
                            DefaultCategories.UNCATEGORIZED.id -> ToeicCategoryClassifier.classify(trimmedTerm, trimmedMeaning)
                            else -> newCategoryId
                        }
                        val updatedList =
                            words + WordEntry(
                                id = nextId,
                                term = trimmedTerm,
                                meaning = trimmedMeaning,
                                categoryId = categoryForNew
                            )
                        scope.launch { wordStorage.setWords(updatedList) }
                        newTerm = ""
                        newMeaning = ""
                    }
                },
                onEditStart = { word ->
                    editingId = word.id
                    editingTerm = word.term
                    editingMeaning = word.meaning
                    editingCategoryId = if (word.categoryId == DefaultCategories.ALL.id) {
                        DefaultCategories.UNCATEGORIZED.id
                    } else {
                        word.categoryId
                    }
                },
                onEditTermChange = { editingTerm = it },
                onEditMeaningChange = { editingMeaning = it },
                onEditCategoryChange = { editingCategoryId = it },
                onEditConfirm = {
                    val id = editingId
                    if (id != null) {
                        val updatedTerm = editingTerm.trim()
                        val updatedMeaning = editingMeaning.trim()
                        if (updatedTerm.isNotEmpty() && updatedMeaning.isNotEmpty()) {
                            val categoryForEdit = when (editingCategoryId) {
                                DefaultCategories.ALL.id -> DefaultCategories.UNCATEGORIZED.id
                                else -> editingCategoryId
                            }
                            val updatedList = words.map { word ->
                                if (word.id == id) {
                                    word.copy(
                                        term = updatedTerm,
                                        meaning = updatedMeaning,
                                        categoryId = categoryForEdit
                                    )
                                } else {
                                    word
                                }
                            }
                            scope.launch { wordStorage.setWords(updatedList) }
                            editingId = null
                            editingTerm = ""
                            editingMeaning = ""
                            editingCategoryId = DefaultCategories.UNCATEGORIZED.id
                        }
                    }
                },
                onEditCancel = {
                    editingId = null
                    editingTerm = ""
                    editingMeaning = ""
                    editingCategoryId = DefaultCategories.UNCATEGORIZED.id
                },
                onDeleteWord = { word ->
                    if (words.any { it.id == word.id }) {
                        val updatedList = words.filterNot { it.id == word.id }
                        scope.launch { wordStorage.setWords(updatedList) }
                        if (editingId == word.id) {
                            editingId = null
                            editingTerm = ""
                            editingMeaning = ""
                            editingCategoryId = DefaultCategories.UNCATEGORIZED.id
                        }
                    }
                },
                onBack = {
                    editingId = null
                    editingTerm = ""
                    editingMeaning = ""
                    searchQuery = ""
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Flashcards.route) {
            FlashcardScreen(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                categoryWordCounts = categoryWordCounts,
                onCategorySelected = onCategorySelected,
                words = filteredWords,
                selectedIndex = selectedCardIndex,
                isMeaningFirst = isMeaningFirst,
                isFlipped = isCardFlipped,
                onSelectPrevious = {
                    if (filteredWords.isNotEmpty()) {
                        selectedCardIndex =
                            if (selectedCardIndex == 0) filteredWords.lastIndex else selectedCardIndex - 1
                        isCardFlipped = false
                    }
                },
                onSelectNext = {
                    if (filteredWords.isNotEmpty()) {
                        selectedCardIndex =
                            if (selectedCardIndex == filteredWords.lastIndex) 0 else selectedCardIndex + 1
                        isCardFlipped = false
                    }
                },
                onToggleMeaningFirst = {
                    isMeaningFirst = it
                    isCardFlipped = false
                },
                onFlipCard = { isCardFlipped = !isCardFlipped },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Quiz.route) {
            QuizScreen(
                words = filteredWords,
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                categoryWordCounts = categoryWordCounts,
                currentWord = currentQuizWordForUi,
                quizMode = quizMode,
                shuffleEnabled = shuffleEnabled,
                quizPosition = quizPosition,
                totalQuestions = quizOrder.size,
                quizRevealed = quizRevealed,
                onCategorySelected = onCategorySelected,
                onModeChange = {
                    quizMode = it
                    quizRevealed = false
                },
                onToggleShuffle = { shuffleEnabled = it },
                onRevealAnswer = { quizRevealed = true },
                onMarkCorrect = {
                    currentQuizWord?.let { word ->
                        val updatedList = words.map {
                            if (it.id == word.id) it.copy(correctCount = it.correctCount + 1)
                            else it
                        }
                        scope.launch {
                            wordStorage.setWords(updatedList)

                            val updatedProgress = userProgress
                                .addXP(10)
                                .updateStreakForToday(today)
                                .let { autoUnlockBadges(it, updatedList) }
                            progressStorage.saveProgress(updatedProgress)
                        }
                    }
                    goToNextQuizQuestion()
                },
                onMarkIncorrect = {
                    currentQuizWord?.let { word ->
                        val updatedList = words.map {
                            if (it.id == word.id) it.copy(incorrectCount = it.incorrectCount + 1)
                            else it
                        }
                        scope.launch {
                            wordStorage.setWords(updatedList)

                            val updatedProgress = userProgress
                                .updateStreakForToday(today)
                                .let { autoUnlockBadges(it, updatedList) }
                            progressStorage.saveProgress(updatedProgress)
                        }
                    }
                    goToNextQuizQuestion()
                },
                onNextQuestion = {
                    if (quizOrder.isEmpty()) {
                        // no-op
                    } else if (quizPosition + 1 >= quizOrder.size) {
                        quizPosition = 0
                        quizRevealed = false
                        if (shuffleEnabled) quizSeed++
                    } else {
                        quizPosition += 1
                        quizRevealed = false
                    }
                },
                onReset = {
                    quizPosition = 0
                    quizRevealed = false
                    quizSeed++
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Badges.route) {
            BadgesScreen(
                userProgress = userProgress,
                words = words,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.TypingGame.route) {
            TypingGameScreen(
                words = filteredWords,
                userProgress = userProgress,
                onGameComplete = { score, correctCount ->
                    scope.launch {
                        val xpEarned = correctCount * 15
                        val updatedProgress = userProgress
                            .addXP(xpEarned)
                            .updateStreakForToday(today)
                            .let { autoUnlockBadges(it, words) }
                        progressStorage.saveProgress(updatedProgress)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun WordQuizAppPreview() {
    MyApplicationTheme {
        WordQuizApp()
    }
}
