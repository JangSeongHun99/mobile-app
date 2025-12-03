package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapp.data.UserProgress
import com.example.myapp.models.WordEntry
import com.example.myapp.ui.components.QuizScreenScaffold
import kotlinx.coroutines.delay

@Composable
fun TypingGameScreen(
    words: List<WordEntry>,
    userProgress: UserProgress,
    onGameComplete: (score: Int, correctCount: Int) -> Unit,
    onBack: () -> Unit
) {
    var gameStarted by remember { mutableStateOf(false) }
    var currentWordIndex by remember { mutableStateOf(0) }
    var userInput by remember { mutableStateOf("") }
    var score by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    var timeLeft by remember { mutableStateOf(60) }
    var gameOrder by remember { mutableStateOf(words.indices.shuffled()) }
    var lastShownAnswer by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(gameStarted, timeLeft) {
        if (gameStarted && timeLeft > 0) {
            delay(1000)
            timeLeft--
        } else if (gameStarted && timeLeft == 0) {
            onGameComplete(score, correctCount)
            gameStarted = false
        }
    }

    val currentWord = if (gameOrder.isNotEmpty() && currentWordIndex < gameOrder.size) {
        words.getOrNull(gameOrder[currentWordIndex])
    } else null

    fun moveNextOrFinish() {
        currentWordIndex++
        if (currentWordIndex >= gameOrder.size) {
            onGameComplete(score, correctCount)
            gameStarted = false
        }
    }

    fun submitAnswer() {
        val target = currentWord ?: return
        if (userInput.trim().equals(target.term, ignoreCase = true)) {
            correctCount++
            score += (timeLeft + 10)
            lastShownAnswer = null
        } else {
            lastShownAnswer = target.term
        }
        userInput = ""
        moveNextOrFinish()
    }

    QuizScreenScaffold(title = "타이핑 게임", onBack = onBack) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!gameStarted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "타이핑 게임",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "60초 동안 뜻을 보고 단어를 빠르게 타이핑하세요.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "정답마다 15 XP 획득\n더 빠르게 맞힐수록 추가 점수",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (words.isEmpty()) {
                            Text(
                                text = "게임을 시작하려면 단어를 추가하세요.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Button(
                                onClick = {
                                    gameStarted = true
                                    currentWordIndex = 0
                                    userInput = ""
                                    score = 0
                                    correctCount = 0
                                    timeLeft = 60
                                    lastShownAnswer = null
                                    gameOrder = words.indices.shuffled()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("게임 시작")
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$timeLeft",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (timeLeft <= 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text("남은 시간", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$correctCount",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("정답", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$score",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text("점수", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (currentWord != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Text(
                                text = "뜻",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = currentWord.meaning,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        label = { Text("단어 입력") },
                        placeholder = { Text("정답을 타이핑하세요") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submitAnswer() })
                    )

                    Button(
                        onClick = { submitAnswer() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = userInput.isNotBlank()
                    ) {
                        Text("제출")
                    }

                    lastShownAnswer?.let { answer ->
                        Text(
                            text = "정답: $answer",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Text(
                        text = "문제 ${currentWordIndex + 1} / ${gameOrder.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}
