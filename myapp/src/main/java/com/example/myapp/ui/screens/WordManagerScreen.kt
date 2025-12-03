package com.example.myapp.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapp.models.Category
import com.example.myapp.models.DefaultCategories
import com.example.myapp.models.WordEntry
import com.example.myapp.ui.components.QuizScreenScaffold

@Composable
fun WordManagerScreen(
    words: List<WordEntry>,
    categories: List<Category>,
    newTerm: String,
    newMeaning: String,
    newCategoryId: String,
    editingId: Long?,
    editingTerm: String,
    editingMeaning: String,
    editingCategoryId: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNewTermChange: (String) -> Unit,
    onNewMeaningChange: (String) -> Unit,
    onNewCategoryChange: (String) -> Unit,
    onAddWord: () -> Unit,
    onEditStart: (WordEntry) -> Unit,
    onEditTermChange: (String) -> Unit,
    onEditMeaningChange: (String) -> Unit,
    onEditCategoryChange: (String) -> Unit,
    onEditConfirm: () -> Unit,
    onEditCancel: () -> Unit,
    onDeleteWord: (WordEntry) -> Unit,
    onBack: () -> Unit
) {
    val filteredWords = remember(words, searchQuery) {
        if (searchQuery.isBlank()) {
            words
        } else {
            words.filter {
                it.term.contains(searchQuery, ignoreCase = true) ||
                    it.meaning.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val selectableCategories = remember(categories) {
        categories.filter { it.id != DefaultCategories.ALL.id }
    }

    QuizScreenScaffold(title = "단어 관리", onBack = onBack) { innerPadding ->
        val listState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("단어 검색") },
                    placeholder = { Text("단어 혹은 뜻으로 검색") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (searchQuery.isNotBlank()) {
                    Text(
                        text = "검색 결과: ${filteredWords.size}개 (전체 ${words.size}개)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
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
                Text(
                    text = "카테고리",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectableCategories.forEach { category ->
                        FilterChip(
                            selected = newCategoryId == category.id,
                            onClick = { onNewCategoryChange(category.id) },
                            label = { Text("${category.icon} ${category.name}") }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onAddWord,
                        enabled = newTerm.isNotBlank() && newMeaning.isNotBlank()
                    ) {
                        Text("단어 추가")
                    }
                }
            }

            if (filteredWords.isEmpty()) {
                item {
                    Text(
                        text = "등록된 단어가 없습니다. 새로운 단어를 추가해 보세요.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(filteredWords, key = { it.id }) { word ->
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
                                Text(
                                    text = "카테고리",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    selectableCategories.forEach { category ->
                                        FilterChip(
                                            selected = editingCategoryId == category.id,
                                            onClick = { onEditCategoryChange(category.id) },
                                            label = { Text("${category.icon} ${category.name}") }
                                        )
                                    }
                                }
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
                                val categoryLabel = selectableCategories.find { it.id == word.categoryId }
                                Text(
                                    text = "카테고리: ${categoryLabel?.let { "${it.icon} ${it.name}" } ?: "미분류"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
