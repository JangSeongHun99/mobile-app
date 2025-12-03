package com.example.myapp.models

data class WordEntry(
    val id: Long,
    val term: String,
    val meaning: String,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val categoryId: String = "uncategorized" // 기본값: 미분류
) {
    val totalAttempts: Int get() = correctCount + incorrectCount
    val accuracyRate: Float get() = if (totalAttempts == 0) 0f else (correctCount.toFloat() / totalAttempts * 100)
}
