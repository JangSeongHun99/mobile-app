package com.example.myapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

private const val USER_PROGRESS_STORE_NAME = "user_progress"
private val Context.userProgressDataStore by preferencesDataStore(name = USER_PROGRESS_STORE_NAME)

class UserProgressStorage(private val context: Context) {

    companion object {
        private val PROGRESS_KEY = stringPreferencesKey("user_progress_json")
    }

    /** 사용자 진행도 스트림 */
    val progressFlow: Flow<UserProgress> = context.userProgressDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val json = preferences[PROGRESS_KEY]
            if (json != null) {
                decodeProgress(json)
            } else {
                UserProgress() // 기본값
            }
        }

    /** 사용자 진행도 저장 */
    suspend fun saveProgress(progress: UserProgress) {
        context.userProgressDataStore.edit { preferences ->
            preferences[PROGRESS_KEY] = encodeProgress(progress)
        }
    }

    /** JSON 인코딩 */
    private fun encodeProgress(progress: UserProgress): String {
        val obj = JSONObject().apply {
            put("level", progress.level)
            put("currentXP", progress.currentXP)
            put("currentStreak", progress.currentStreak)
            put("longestStreak", progress.longestStreak)
            put("lastStudyDate", progress.lastStudyDate ?: "")
            put("totalQuizzesTaken", progress.totalQuizzesTaken)
            put("totalWordsLearned", progress.totalWordsLearned)

            val badgesArray = JSONArray()
            progress.unlockedBadges.forEach { badgesArray.put(it) }
            put("unlockedBadges", badgesArray)
        }
        return obj.toString()
    }

    /** JSON 디코딩 */
    private fun decodeProgress(json: String): UserProgress {
        return runCatching {
            val obj = JSONObject(json)
            val badgesList = mutableListOf<String>()
            val badgesArray = obj.optJSONArray("unlockedBadges")
            if (badgesArray != null) {
                for (i in 0 until badgesArray.length()) {
                    badgesArray.optString(i)?.let { badgesList.add(it) }
                }
            }

            UserProgress(
                level = obj.optInt("level", 1),
                currentXP = obj.optInt("currentXP", 0),
                currentStreak = obj.optInt("currentStreak", 0),
                longestStreak = obj.optInt("longestStreak", 0),
                lastStudyDate = obj.optString("lastStudyDate").takeIf { it.isNotBlank() },
                totalQuizzesTaken = obj.optInt("totalQuizzesTaken", 0),
                totalWordsLearned = obj.optInt("totalWordsLearned", 0),
                unlockedBadges = badgesList
            )
        }.getOrElse { UserProgress() }
    }

    /** 초기화 */
    suspend fun clear() {
        context.userProgressDataStore.edit { it.clear() }
    }
}
