package com.example.myapp.data

import kotlinx.serialization.Serializable

/**
 * 사용자 학습 진행도 및 게임화 데이터
 */
@Serializable
data class UserProgress(
    // 레벨 및 경험치
    val level: Int = 1,
    val currentXP: Int = 0,

    // 스트릭 (연속 학습일)
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastStudyDate: String? = null, // "2024-12-02" 형식

    // 통계
    val totalQuizzesTaken: Int = 0,
    val totalWordsLearned: Int = 0,

    // 획득한 뱃지
    val unlockedBadges: List<String> = emptyList()
) {
    /**
     * 다음 레벨까지 필요한 XP
     * 레벨 * 100 (예: 레벨 2 = 200 XP, 레벨 3 = 300 XP)
     */
    val xpForNextLevel: Int get() = level * 100

    /**
     * 현재 레벨 진행률 (0.0 ~ 1.0)
     */
    val levelProgress: Float get() = currentXP.toFloat() / xpForNextLevel

    /**
     * XP 추가 및 레벨업 처리
     */
    fun addXP(amount: Int): UserProgress {
        var newXP = currentXP + amount
        var newLevel = level

        // 레벨업 체크
        while (newXP >= newLevel * 100) {
            newXP -= newLevel * 100
            newLevel++
        }

        return copy(
            level = newLevel,
            currentXP = newXP
        )
    }

    /**
     * 오늘 학습 완료 처리 (스트릭 업데이트)
     */
    fun updateStreakForToday(today: String): UserProgress {
        if (lastStudyDate == today) {
            // 오늘 이미 학습함
            return this
        }

        val newStreak = if (isYesterday(lastStudyDate, today)) {
            // 연속 학습
            currentStreak + 1
        } else {
            // 스트릭 끊김, 새로 시작
            1
        }

        return copy(
            currentStreak = newStreak,
            longestStreak = maxOf(longestStreak, newStreak),
            lastStudyDate = today
        )
    }

    /**
     * 뱃지 획득
     */
    fun unlockBadge(badgeId: String): UserProgress {
        return if (!unlockedBadges.contains(badgeId)) {
            copy(unlockedBadges = unlockedBadges + badgeId)
        } else {
            this
        }
    }

    private fun isYesterday(lastDate: String?, today: String): Boolean {
        if (lastDate == null) return false

        // 간단한 날짜 비교 (YYYY-MM-DD 형식)
        // 실제로는 kotlinx-datetime 사용 권장
        val lastParts = lastDate.split("-").map { it.toIntOrNull() ?: 0 }
        val todayParts = today.split("-").map { it.toIntOrNull() ?: 0 }

        if (lastParts.size != 3 || todayParts.size != 3) return false

        val lastYear = lastParts[0]
        val lastMonth = lastParts[1]
        val lastDay = lastParts[2]

        val todayYear = todayParts[0]
        val todayMonth = todayParts[1]
        val todayDay = todayParts[2]

        // 같은 달의 하루 차이
        if (todayYear == lastYear && todayMonth == lastMonth && todayDay == lastDay + 1) {
            return true
        }

        // 월이 넘어간 경우 (간단 체크)
        if (todayYear == lastYear && todayMonth == lastMonth + 1 && todayDay == 1) {
            return true
        }

        // 연도가 넘어간 경우
        if (todayYear == lastYear + 1 && todayMonth == 1 && lastMonth == 12 && todayDay == 1) {
            return true
        }

        return false
    }
}

/**
 * 뱃지 정의
 */
enum class Badge(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String
) {
    FIRST_WORD("first_word", "첫 걸음", "첫 단어 추가", "🌱"),
    TEN_WORDS("ten_words", "단어 수집가", "10개 단어 추가", "📚"),
    FIFTY_WORDS("fifty_words", "단어 마스터", "50개 단어 추가", "🎓"),

    FIRST_QUIZ("first_quiz", "퀴즈 도전자", "첫 퀴즈 완료", "🎯"),
    TEN_QUIZZES("ten_quizzes", "퀴즈 마니아", "10회 퀴즈 완료", "🏆"),

    STREAK_3("streak_3", "꾸준함", "3일 연속 학습", "🔥"),
    STREAK_7("streak_7", "일주일의 힘", "7일 연속 학습", "⚡"),
    STREAK_30("streak_30", "한 달의 여정", "30일 연속 학습", "💪"),

    LEVEL_5("level_5", "학습자", "레벨 5 달성", "⭐"),
    LEVEL_10("level_10", "전문가", "레벨 10 달성", "🌟"),
    LEVEL_20("level_20", "마스터", "레벨 20 달성", "✨"),

    PERFECT_QUIZ("perfect_quiz", "완벽주의", "퀴즈 100% 정답", "💯");

    companion object {
        fun fromId(id: String): Badge? = values().find { it.id == id }
    }
}
