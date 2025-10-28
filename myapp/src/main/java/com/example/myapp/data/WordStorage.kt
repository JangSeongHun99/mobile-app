package com.example.myapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapp.WordEntry
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private const val WORD_DATA_STORE_NAME = "word_storage"
private val Context.wordDataStore by preferencesDataStore(name = WORD_DATA_STORE_NAME)

class WordStorage(private val context: Context) {

    companion object {
        private val WORDS_KEY = stringPreferencesKey("words_json")
        private const val EMPTY_JSON = "[]"
    }

    /** 단어 스트림 */
    val wordsFlow: Flow<List<WordEntry>> = context.wordDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val json = preferences[WORDS_KEY] ?: EMPTY_JSON
            decodeWords(json)
        }

    /** 전체 덮어쓰기 */
    suspend fun setWords(words: List<WordEntry>) {
        context.wordDataStore.edit { preferences ->
            preferences[WORDS_KEY] = if (words.isEmpty()) EMPTY_JSON else encodeWords(words)
        }
    }

    /**
     * 최초 실행 시 기본 단어를 채워넣기.
     * - 키가 없거나 값이 "[]" 인 경우에만 시드 주입
     */
    suspend fun ensureSeeded(defaultWords: List<WordEntry>) {
        if (defaultWords.isEmpty()) return

        val currentJson = runCatching {
            context.wordDataStore.data.first()[WORDS_KEY]
        }.getOrNull()

        val shouldSeed = currentJson.isNullOrBlank() || currentJson == EMPTY_JSON
        if (!shouldSeed) return

        context.wordDataStore.edit { prefs ->
            // 여전히 다른 쓰레드가 썼을 수 있으니 한 번 더 체크
            val existing = prefs[WORDS_KEY]
            if (existing.isNullOrBlank() || existing == EMPTY_JSON) {
                prefs[WORDS_KEY] = encodeWords(defaultWords)
            }
        }
    }

    /** JSON 인코딩 */
    private fun encodeWords(words: List<WordEntry>): String {
        // id 기준 정렬 + 중복 id 제거(마지막 값 우선)
        val deduped = words
            .associateBy { it.id } // 동일 id 마지막 것이 남음
            .values
            .sortedBy { it.id }

        val jsonArray = JSONArray()
        deduped.forEach { word ->
            val obj = JSONObject().apply {
                put("id", word.id)
                put("term", word.term)
                put("meaning", word.meaning)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    /** JSON 디코딩 */
    private fun decodeWords(json: String): List<WordEntry> {
        return runCatching {
            val array = JSONArray(json)
            val temp = ArrayList<WordEntry>(array.length())
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val term = item.optString("term", "").trim()
                val meaning = item.optString("meaning", "").trim()
                val id = item.optLong("id", -1L)
                if (id >= 0 && term.isNotEmpty() && meaning.isNotEmpty()) {
                    temp.add(WordEntry(id = id, term = term, meaning = meaning))
                }
            }
            // 중복 id 제거(마지막 값 우선), id 오름차순 정렬로 안정화
            temp.associateBy { it.id }.values.sortedBy { it.id }
        }.getOrElse { emptyList() }
    }

    /** 선택: 전부 비우기 유틸  **/
    suspend fun clear() {
        context.wordDataStore.edit { it[WORDS_KEY] = EMPTY_JSON }
    }
}
