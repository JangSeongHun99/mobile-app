package com.example.myapp.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.myapp.data.UserProgress
import com.example.myapp.models.Category
import com.example.myapp.models.DefaultCategories
import com.example.myapp.models.WordEntry
import com.example.myapp.navigation.Screen
import com.example.myapp.ui.components.HomeMenuCard
import com.example.myapp.ui.components.QuizScreenScaffold

@Composable
fun HomeScreen(
    allWords: List<WordEntry>,
    filteredWords: List<WordEntry>,
    userProgress: UserProgress,
    selectedCategoryId: String,
    onCategorySelected: (String) -> Unit,
    navController: NavHostController,
    onNavigateToManage: () -> Unit,
    onNavigateToFlashcards: () -> Unit,
    onNavigateToQuiz: () -> Unit
) {
    val totalCorrect = filteredWords.sumOf { it.correctCount }
    val totalIncorrect = filteredWords.sumOf { it.incorrectCount }
    val totalAttempts = totalCorrect + totalIncorrect
    val overallAccuracy = if (totalAttempts > 0) (totalCorrect.toFloat() / totalAttempts * 100) else 0f

    QuizScreenScaffold(title = "영단어 학습") { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "오늘의 학습",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            // 카테고리 필터 칩
            val categories = DefaultCategories.getDefaultList()
            val categoryWordCounts = categories.associate { category ->
                category.id to if (category.id == "all") {
                    allWords.size
                } else {
                    allWords.count { it.categoryId == category.id }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val wordCount = categoryWordCounts[category.id] ?: 0
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { onCategorySelected(category.id) },
                        label = {
                            Text("${category.icon} ${category.name} ($wordCount)")
                        }
                    )
                }
            }

            // 스트릭 및 레벨 카드
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 스트릭
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔥",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Column {
                                Text(
                                    text = "${userProgress.currentStreak}일 연속",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "최장 기록: ${userProgress.longestStreak}일",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Divider()

                    // 레벨 및 XP
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "레벨 ${userProgress.level}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${userProgress.currentXP} / ${userProgress.xpForNextLevel} XP",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // XP 프로그레스 바
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                androidx.compose.material3.LinearProgressIndicator(
                                    progress = { userProgress.levelProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                )
                            }
                        }
                    }
                }
            }

            // 학습 통계 카드
            if (totalAttempts > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "학습 통계",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$totalCorrect",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text("정답", style = MaterialTheme.typography.bodyMedium)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$totalIncorrect",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text("오답", style = MaterialTheme.typography.bodyMedium)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "%.1f%%".format(overallAccuracy),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("정답률", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            Text(
                text = if (selectedCategoryId == "all") {
                    "등록된 단어 ${allWords.size}개"
                } else {
                    val categoryName = DefaultCategories.findById(selectedCategoryId)?.name ?: "선택한 카테고리"
                    "$categoryName ${filteredWords.size}개 / 전체 ${allWords.size}개"
                },
                style = MaterialTheme.typography.bodyLarge
            )
            if (filteredWords.isEmpty()) {
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
            HomeMenuCard(
                title = "타이핑 게임",
                description = "뜻을 보고 영단어를 빠르게 타이핑하는 게임입니다.",
                onClick = { navController.navigate(Screen.TypingGame.route) }
            )
            HomeMenuCard(
                title = "업적 & 뱃지",
                description = "획득한 뱃지를 확인하고 새로운 업적에 도전하세요.",
                onClick = { navController.navigate(Screen.Badges.route) }
            )
        }
    }
}
