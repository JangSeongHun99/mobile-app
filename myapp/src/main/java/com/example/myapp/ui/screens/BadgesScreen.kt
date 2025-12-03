package com.example.myapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapp.data.Badge
import com.example.myapp.data.UserProgress
import com.example.myapp.models.WordEntry
import com.example.myapp.ui.components.QuizScreenScaffold
import com.example.myapp.utils.checkBadgeRequirement

@Composable
fun BadgesScreen(
    userProgress: UserProgress,
    words: List<WordEntry>,
    onBack: () -> Unit
) {
    QuizScreenScaffold(title = "업적 & 뱃지", onBack = onBack) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "획득한 뱃지: ${userProgress.unlockedBadges.size} / ${Badge.values().size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // 모든 뱃지 표시
            Badge.values().forEach { badge ->
                val isUnlocked = userProgress.unlockedBadges.contains(badge.id)
                val canUnlock = checkBadgeRequirement(badge, userProgress, words)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else if (canUnlock) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isUnlocked) badge.emoji else "🔒",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = badge.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = badge.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (canUnlock && !isUnlocked) {
                                Text(
                                    text = "달성 가능!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
