package com.example.myapp.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapp.models.Category
import com.example.myapp.models.WordEntry
import com.example.myapp.ui.components.FilterButton
import com.example.myapp.ui.components.QuizScreenScaffold

@Composable
fun FlashcardScreen(
    categories: List<Category>,
    selectedCategoryId: String,
    categoryWordCounts: Map<String, Int>,
    onCategorySelected: (String) -> Unit,
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
                    androidx.compose.material3.FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { onCategorySelected(category.id) },
                        label = { Text("${category.icon} ${category.name} ($count)") }
                    )
                }
            }

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
                Text(
                    "??? ????? ??? ??? ????. ?? ????? ????? ??? ??? ???.",
                    style = MaterialTheme.typography.bodyMedium
                )
                return@Column
            }

            val safeIndex = selectedIndex.coerceIn(0, words.lastIndex)
            val currentWord = words[safeIndex]
            val showWordSide = if (isMeaningFirst) isFlipped else !isFlipped
            val cardLabel = if (showWordSide) "단어" else "뜻"
            val cardText = if (showWordSide) currentWord.term else currentWord.meaning

            // 3D 뒤집기 애니메이션
            val rotation by animateFloatAsState(
                targetValue = if (isFlipped) 180f else 0f,
                animationSpec = tween(
                    durationMillis = 600,
                    easing = FastOutSlowInEasing
                ),
                label = "cardRotation"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable { onFlipCard() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 40.dp)
                        .graphicsLayer {
                            rotationY = if (rotation > 90f) 180f else 0f
                        },
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
}
