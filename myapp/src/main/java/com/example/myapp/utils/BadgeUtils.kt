package com.example.myapp.utils

import com.example.myapp.data.Badge
import com.example.myapp.data.UserProgress
import com.example.myapp.models.WordEntry

/**
 * 뱃지 획득 조건 체크
 */
fun checkBadgeRequirement(
    badge: Badge,
    userProgress: UserProgress,
    words: List<WordEntry>
): Boolean {
    return when (badge) {
        Badge.FIRST_WORD -> words.isNotEmpty()
        Badge.TEN_WORDS -> words.size >= 10
        Badge.FIFTY_WORDS -> words.size >= 50

        Badge.FIRST_QUIZ -> userProgress.totalQuizzesTaken >= 1
        Badge.TEN_QUIZZES -> userProgress.totalQuizzesTaken >= 10

        Badge.STREAK_3 -> userProgress.currentStreak >= 3
        Badge.STREAK_7 -> userProgress.currentStreak >= 7
        Badge.STREAK_30 -> userProgress.currentStreak >= 30

        Badge.LEVEL_5 -> userProgress.level >= 5
        Badge.LEVEL_10 -> userProgress.level >= 10
        Badge.LEVEL_20 -> userProgress.level >= 20

        Badge.PERFECT_QUIZ -> false // 별도 로직 필요
    }
}

/**
 * 자동으로 획득 가능한 뱃지를 체크하고 획득 처리
 */
fun autoUnlockBadges(
    userProgress: UserProgress,
    words: List<WordEntry>
): UserProgress {
    var updated = userProgress

    Badge.values().forEach { badge ->
        if (!updated.unlockedBadges.contains(badge.id)) {
            if (checkBadgeRequirement(badge, updated, words)) {
                updated = updated.unlockBadge(badge.id)
            }
        }
    }

    return updated
}
