package com.example.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.ui.theme.MyApplicationTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapp.data.WordStorage
import kotlin.random.Random
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()
    val words by wordStorage.wordsFlow.collectAsState(initial = emptyList())

    var newTerm by rememberSaveable { mutableStateOf("") }
    var newMeaning by rememberSaveable { mutableStateOf("") }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editingTerm by rememberSaveable { mutableStateOf("") }
    var editingMeaning by rememberSaveable { mutableStateOf("") }

    var selectedCardIndex by rememberSaveable { mutableStateOf(0) }
    var isMeaningFirst by rememberSaveable { mutableStateOf(false) }
    var isCardFlipped by rememberSaveable { mutableStateOf(false) }

    var quizMode by rememberSaveable { mutableStateOf(QuizMode.WordFirst) }
    var shuffleEnabled by rememberSaveable { mutableStateOf(true) }
    var quizPosition by rememberSaveable { mutableStateOf(0) }
    var quizSeed by rememberSaveable { mutableStateOf(0) }
    var quizRevealed by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        wordStorage.ensureSeeded(
            listOf(
                WordEntry(id = 1L, term = "apple", meaning = "사과"),
                WordEntry(id = 2L, term = "compose", meaning = "안드로이드 UI 툴킷")
            )
        )
    }

    val quizOrder = remember(quizSeed, words.size, shuffleEnabled) {
        val indices = words.indices.toList()
        if (shuffleEnabled) {
            if (indices.isEmpty()) emptyList() else indices.shuffled(Random(quizSeed))
        } else {
            indices
        }
    }

    val currentQuizWord = quizOrder.getOrNull(quizPosition)?.let { words[it] }

    LaunchedEffect(words.size) {
        if (selectedCardIndex >= words.size) {
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

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                wordCount = words.size,
                onNavigateToManage = { navController.navigate(Screen.Manage.route) },
                onNavigateToFlashcards = { navController.navigate(Screen.Flashcards.route) },
                onNavigateToQuiz = { navController.navigate(Screen.Quiz.route) }
            )
        }
        composable(Screen.Manage.route) {
            WordManagerScreen(
                words = words,
                newTerm = newTerm,
                newMeaning = newMeaning,
                editingId = editingId,
                editingTerm = editingTerm,
                editingMeaning = editingMeaning,
                onNewTermChange = { newTerm = it },
                onNewMeaningChange = { newMeaning = it },
                onAddWord = {
                    val trimmedTerm = newTerm.trim()
                    val trimmedMeaning = newMeaning.trim()
                    if (trimmedTerm.isNotEmpty() && trimmedMeaning.isNotEmpty()) {
                        val nextId = (words.maxOfOrNull { it.id } ?: 0L) + 1L
                        val updatedList =
                            words + WordEntry(id = nextId, term = trimmedTerm, meaning = trimmedMeaning)
                        scope.launch { wordStorage.setWords(updatedList) }
                        newTerm = ""
                        newMeaning = ""
                    }
                },
                onEditStart = { word ->
                    editingId = word.id
                    editingTerm = word.term
                    editingMeaning = word.meaning
                },
                onEditTermChange = { editingTerm = it },
                onEditMeaningChange = { editingMeaning = it },
                onEditConfirm = {
                    val id = editingId
                    if (id != null) {
                        val updatedTerm = editingTerm.trim()
                        val updatedMeaning = editingMeaning.trim()
                        if (updatedTerm.isNotEmpty() && updatedMeaning.isNotEmpty()) {
                            val updatedList = words.map { word ->
                                if (word.id == id) {
                                    word.copy(term = updatedTerm, meaning = updatedMeaning)
                                } else {
                                    word
                                }
                            }
                            scope.launch { wordStorage.setWords(updatedList) }
                            editingId = null
                            editingTerm = ""
                            editingMeaning = ""
                        }
                    }
                },
                onEditCancel = {
                    editingId = null
                    editingTerm = ""
                    editingMeaning = ""
                },
                onDeleteWord = { word ->
                    if (words.any { it.id == word.id }) {
                        val updatedList = words.filterNot { it.id == word.id }
                        scope.launch { wordStorage.setWords(updatedList) }
                        if (editingId == word.id) {
                            editingId = null
                            editingTerm = ""
                            editingMeaning = ""
                        }
                    }
                },
                onBack = {
                    editingId = null
                    editingTerm = ""
                    editingMeaning = ""
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Flashcards.route) {
            FlashcardScreen(
                words = words,
                selectedIndex = selectedCardIndex,
                isMeaningFirst = isMeaningFirst,
                isFlipped = isCardFlipped,
                onSelectPrevious = {
                    if (words.isNotEmpty()) {
                        selectedCardIndex =
                            if (selectedCardIndex == 0) words.lastIndex else selectedCardIndex - 1
                        isCardFlipped = false
                    }
                },
                onSelectNext = {
                    if (words.isNotEmpty()) {
                        selectedCardIndex =
                            if (selectedCardIndex == words.lastIndex) 0 else selectedCardIndex + 1
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
            QuizPracticeScreen(
                words = words,
                currentWord = currentQuizWord,
                quizMode = quizMode,
                shuffleEnabled = shuffleEnabled,
                quizPosition = quizPosition,
                totalQuestions = quizOrder.size,
                quizRevealed = quizRevealed,
                onModeChange = {
                    quizMode = it
                    quizRevealed = false
                },
                onToggleShuffle = { shuffleEnabled = it },
                onRevealAnswer = { quizRevealed = true },
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
                }
                ,
                onReset = {
                    quizPosition = 0
                    quizRevealed = false
                    quizSeed++
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    wordCount: Int,
    onNavigateToManage: () -> Unit,
    onNavigateToFlashcards: () -> Unit,
    onNavigateToQuiz: () -> Unit
) {
    QuizScreenScaffold(title = "영단어 학습") { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "오늘의 학습",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "등록된 단어 ${wordCount}개",
                style = MaterialTheme.typography.bodyLarge
            )
            if (wordCount == 0) {
                Text(
                    text = "아직 등록된 단어가 없습니다. 단어 관리에서 학습 목록을 만들어 보세요.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            HomeMenuCard(
                title = "단어 관리",
                description = "새 단어를 추가하고 기존 단어를 수정하거나 삭제합니다.",
                onClick = onNavigateToManage
            )
            HomeMenuCard(
                title = "플래시카드",
                description = "단어 먼저 보기 또는 뜻 먼저 보기 모드로 뒤집어 보며 암기하세요.",
                onClick = onNavigateToFlashcards
            )
            HomeMenuCard(
                title = "단어 퀴즈",
                description = "랜덤 순서로 단어/뜻 맞히기 퀴즈를 풀어보세요.",
                onClick = onNavigateToQuiz
            )
        }
    }
}

@Composable
private fun WordManagerScreen(
    words: List<WordEntry>,
    newTerm: String,
    newMeaning: String,
    editingId: Long?,
    editingTerm: String,
    editingMeaning: String,
    onNewTermChange: (String) -> Unit,
    onNewMeaningChange: (String) -> Unit,
    onAddWord: () -> Unit,
    onEditStart: (WordEntry) -> Unit,
    onEditTermChange: (String) -> Unit,
    onEditMeaningChange: (String) -> Unit,
    onEditConfirm: () -> Unit,
    onEditCancel: () -> Unit,
    onDeleteWord: (WordEntry) -> Unit,
    onBack: () -> Unit
) {
    QuizScreenScaffold(title = "단어 관리", onBack = onBack) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WordManagerSection(
                words = words,
                newTerm = newTerm,
                newMeaning = newMeaning,
                editingId = editingId,
                editingTerm = editingTerm,
                editingMeaning = editingMeaning,
                onNewTermChange = onNewTermChange,
                onNewMeaningChange = onNewMeaningChange,
                onAddWord = onAddWord,
                onEditStart = onEditStart,
                onEditTermChange = onEditTermChange,
                onEditMeaningChange = onEditMeaningChange,
                onEditConfirm = onEditConfirm,
                onEditCancel = onEditCancel,
                onDeleteWord = onDeleteWord
            )
        }
    }
}

@Composable
private fun FlashcardScreen(
    words: List<WordEntry>,
    selectedIndex: Int,
    isMeaningFirst: Boolean,
    isFlipped: Boolean,
    onSelectPrevious: () -> Unit,
    onSelectNext: () -> Unit,
    onToggleMeaningFirst: (Boolean) -> Unit,
    onFlipCard: () -> Unit,
    onBack: () -> Unit
) {
    QuizScreenScaffold(title = "플래시카드", onBack = onBack) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WordCardSection(
                words = words,
                selectedIndex = selectedIndex,
                isMeaningFirst = isMeaningFirst,
                isFlipped = isFlipped,
                onSelectPrevious = onSelectPrevious,
                onSelectNext = onSelectNext,
                onToggleMeaningFirst = onToggleMeaningFirst,
                onFlipCard = onFlipCard
            )
        }
    }
}

@Composable
private fun QuizPracticeScreen(
    words: List<WordEntry>,
    currentWord: WordEntry?,
    quizMode: QuizMode,
    shuffleEnabled: Boolean,
    quizPosition: Int,
    totalQuestions: Int,
    quizRevealed: Boolean,
    onModeChange: (QuizMode) -> Unit,
    onToggleShuffle: (Boolean) -> Unit,
    onRevealAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    QuizScreenScaffold(title = "단어 퀴즈", onBack = onBack) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuizSection(
                words = words,
                currentWord = currentWord,
                quizMode = quizMode,
                shuffleEnabled = shuffleEnabled,
                quizPosition = quizPosition,
                totalQuestions = totalQuestions,
                quizRevealed = quizRevealed,
                onModeChange = onModeChange,
                onToggleShuffle = onToggleShuffle,
                onRevealAnswer = onRevealAnswer,
                onNextQuestion = onNextQuestion,
                onReset = onReset
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizScreenScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "뒤로가기"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Composable
private fun HomeMenuCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun WordManagerSection(
    words: List<WordEntry>,
    newTerm: String,
    newMeaning: String,
    editingId: Long?,
    editingTerm: String,
    editingMeaning: String,
    onNewTermChange: (String) -> Unit,
    onNewMeaningChange: (String) -> Unit,
    onAddWord: () -> Unit,
    onEditStart: (WordEntry) -> Unit,
    onEditTermChange: (String) -> Unit,
    onEditMeaningChange: (String) -> Unit,
    onEditConfirm: () -> Unit,
    onEditCancel: () -> Unit,
    onDeleteWord: (WordEntry) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = newTerm,
            onValueChange = onNewTermChange,
            label = { Text("단어") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = newMeaning,
            onValueChange = onNewMeaningChange,
            label = { Text("뜻") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = onAddWord,
            modifier = Modifier.align(Alignment.End),
            enabled = newTerm.isNotBlank() && newMeaning.isNotBlank()
        ) {
            Text("단어 추가")
        }

        if (words.isEmpty()) {
            Text(
                text = "등록된 단어가 없습니다. 새로운 단어를 추가해 보세요.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                words.forEach { word ->
                    val isEditing = editingId == word.id
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isEditing) {
                                OutlinedTextField(
                                    value = editingTerm,
                                    onValueChange = onEditTermChange,
                                    label = { Text("단어") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = editingMeaning,
                                    onValueChange = onEditMeaningChange,
                                    label = { Text("뜻") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = onEditConfirm,
                                        enabled = editingTerm.isNotBlank() && editingMeaning.isNotBlank()
                                    ) {
                                        Text("저장")
                                    }
                                    TextButton(onClick = onEditCancel) {
                                        Text("취소")
                                    }
                                }
                            } else {
                                Text(
                                    text = word.term,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(text = word.meaning, style = MaterialTheme.typography.bodyMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = { onEditStart(word) }) { Text("수정") }
                                    TextButton(onClick = { onDeleteWord(word) }) { Text("삭제") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WordCardSection(
    words: List<WordEntry>,
    selectedIndex: Int,
    isMeaningFirst: Boolean,
    isFlipped: Boolean,
    onSelectPrevious: () -> Unit,
    onSelectNext: () -> Unit,
    onToggleMeaningFirst: (Boolean) -> Unit,
    onFlipCard: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterButton(
                text = "단어 먼저 보기",
                selected = !isMeaningFirst,
                onClick = { onToggleMeaningFirst(false) }
            )
            FilterButton(
                text = "뜻 먼저 보기",
                selected = isMeaningFirst,
                onClick = { onToggleMeaningFirst(true) }
            )
        }
        if (words.isEmpty()) {
            Text("플래시카드 학습을 위해 단어를 추가하세요.", style = MaterialTheme.typography.bodyMedium)
            return@Column
        }

        val safeIndex = selectedIndex.coerceIn(0, words.lastIndex)
        val currentWord = words[safeIndex]
        val showWordSide = if (isMeaningFirst) isFlipped else !isFlipped
        val cardLabel = if (showWordSide) "단어" else "뜻"
        val cardText = if (showWordSide) currentWord.term else currentWord.meaning

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onFlipCard() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(cardLabel, style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = cardText,
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp),
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "(탭하여 뒤집기)", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onSelectPrevious) {
                Text("이전")
            }
            Text(text = "${safeIndex + 1} / ${words.size}", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onSelectNext) {
                Text("다음")
            }
        }
    }
}

@Composable
private fun FilterButton(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = if (selected) {
        ButtonDefaults.textButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        ButtonDefaults.textButtonColors()
    }
    TextButton(
        onClick = onClick,
        colors = colors,
        modifier = Modifier
            .size(width = 140.dp, height = 48.dp)
    ) {
        Text(text)
    }
}

@Composable
private fun QuizSection(
    words: List<WordEntry>,
    currentWord: WordEntry?,
    quizMode: QuizMode,
    shuffleEnabled: Boolean,
    quizPosition: Int,
    totalQuestions: Int,
    quizRevealed: Boolean,
    onModeChange: (QuizMode) -> Unit,
    onToggleShuffle: (Boolean) -> Unit,
    onRevealAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onReset: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterButton(text = "단어 먼저 보기", selected = quizMode == QuizMode.WordFirst) {
                onModeChange(QuizMode.WordFirst)
            }
            FilterButton(text = "뜻 먼저 보기", selected = quizMode == QuizMode.MeaningFirst) {
                onModeChange(QuizMode.MeaningFirst)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("순서 섞기")
            TextButton(onClick = { onToggleShuffle(!shuffleEnabled) }) {
                Text(if (shuffleEnabled) "켜짐" else "꺼짐")
            }
        }

        if (currentWord == null || words.isEmpty()) {
            Text("퀴즈를 시작하려면 단어를 추가하세요.", style = MaterialTheme.typography.bodyMedium)
            return@Column
        }

        val prompt = if (quizMode == QuizMode.WordFirst) currentWord.term else currentWord.meaning
        val answer = if (quizMode == QuizMode.WordFirst) currentWord.meaning else currentWord.term

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "문제 ${quizPosition + 1} / $totalQuestions",
                    style = MaterialTheme.typography.labelLarge
                )
                Text(text = prompt, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (quizRevealed) {
                    Divider()
                    Text(text = answer, style = MaterialTheme.typography.titleMedium)
                } else {
                    Button(onClick = onRevealAnswer) {
                        Text("정답 보기")
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onReset) {
                Text("처음부터")
            }
            Button(onClick = onNextQuestion) {
                Text("다음 문제")
            }
        }
    }
}

private enum class Screen(val route: String) {
    Home("home"),
    Manage("manage"),
    Flashcards("flashcards"),
    Quiz("quiz")
}

data class WordEntry(
    val id: Long,
    val term: String,
    val meaning: String
)

enum class QuizMode {
    WordFirst,
    MeaningFirst
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun WordQuizAppPreview() {
    MyApplicationTheme {
        WordQuizApp()
    }
}
