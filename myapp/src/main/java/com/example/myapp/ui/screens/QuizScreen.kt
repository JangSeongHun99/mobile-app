package com.example.myapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapp.models.Category
import com.example.myapp.models.QuizMode
import com.example.myapp.models.WordEntry
import com.example.myapp.ui.components.FilterButton
import com.example.myapp.ui.components.QuizScreenScaffold

@Composable
fun QuizScreen(
    words: List<WordEntry>,
    categories: List<Category>,
    selectedCategoryId: String,
    categoryWordCounts: Map<String, Int>,
    currentWord: WordEntry?,
    quizMode: QuizMode,
    shuffleEnabled: Boolean,
    quizPosition: Int,
    totalQuestions: Int,
    quizRevealed: Boolean,
    onCategorySelected: (String) -> Unit,
    onModeChange: (QuizMode) -> Unit,
    onToggleShuffle: (Boolean) -> Unit,
    onRevealAnswer: () -> Unit,
    onMarkCorrect: () -> Unit,
    onMarkIncorrect: () -> Unit,
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
            Text(
                text = "카테고리 선택",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val count = categoryWordCounts[category.id] ?: 0
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { onCategorySelected(category.id) },
                        label = { Text("${category.icon} ${category.name} ($count)") }
                    )
                }
            }

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
                Text("섞어서 보기")
                TextButton(onClick = { onToggleShuffle(!shuffleEnabled) }) {
                    Text(if (shuffleEnabled) "켜짐" else "꺼짐")
                }
            }

            if (currentWord == null || words.isEmpty()) {
                val categoryName = categories.find { it.id == selectedCategoryId }?.name ?: "선택한 카테고리"
                Text(
                    "${categoryName}에 학습할 단어가 없습니다. 다른 카테고리를 선택하거나 단어를 추가해 보세요.",
                    style = MaterialTheme.typography.bodyMedium
                )
                return@QuizScreenScaffold
            }

            val prompt = if (quizMode == QuizMode.WordFirst) currentWord.term else currentWord.meaning
            val answer = if (quizMode == QuizMode.WordFirst) currentWord.meaning else currentWord.term
            val totalCorrect = words.sumOf { it.correctCount }
            val totalIncorrect = words.sumOf { it.incorrectCount }
            val totalAttempts = totalCorrect + totalIncorrect
            val totalAccuracy = if (totalAttempts > 0) totalCorrect.toFloat() / totalAttempts * 100 else 0f

            // 통계 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalCorrect",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("정답", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalIncorrect",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text("오답", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (totalAttempts > 0) "%.1f%%".format(totalAccuracy) else "-",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text("정답률", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // 퀴즈 카드
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

                    AnimatedVisibility(
                        visible = quizRevealed,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Divider()
                            Text(text = "정답: $answer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                            // 정답/오답 버튼
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onMarkCorrect,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("정답")
                                }
                                Button(
                                    onClick = onMarkIncorrect,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("오답")
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = !quizRevealed,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Button(onClick = onRevealAnswer, modifier = Modifier.fillMaxWidth()) {
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
                if (quizRevealed) {
                    TextButton(onClick = onNextQuestion) {
                        Text("다음 문제")
                    }
                }
            }
        }
    }
}
