package com.example.myapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapp.models.WordEntry
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

    /** Stored word list. */
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

    /** Replace all words. */
    suspend fun setWords(words: List<WordEntry>) {
        context.wordDataStore.edit { preferences ->
            preferences[WORDS_KEY] = if (words.isEmpty()) EMPTY_JSON else encodeWords(words)
        }
    }

    /**
     * Seed default words on first launch.
     * - Only when the stored payload is blank or "[]".
     */
    suspend fun ensureSeeded(defaultWords: List<WordEntry>) {
        if (defaultWords.isEmpty()) return

        val currentJson = runCatching {
            context.wordDataStore.data.first()[WORDS_KEY]
        }.getOrNull()

        val shouldSeed = currentJson.isNullOrBlank() || currentJson == EMPTY_JSON
        if (!shouldSeed) return

        context.wordDataStore.edit { prefs ->
            val existing = prefs[WORDS_KEY]
            if (existing.isNullOrBlank() || existing == EMPTY_JSON) {
                prefs[WORDS_KEY] = encodeWords(defaultWords)
            }
        }
    }

    /** JSON encode. */
    private fun encodeWords(words: List<WordEntry>): String {
        val deduped = words
            .associateBy { it.id } // keep the first appearance of each id
            .values
            .sortedBy { it.id }

        val jsonArray = JSONArray()
        deduped.forEach { word ->
            val obj = JSONObject().apply {
                put("id", word.id)
                put("term", word.term)
                put("meaning", word.meaning)
                put("correctCount", word.correctCount)
                put("incorrectCount", word.incorrectCount)
                put("categoryId", word.categoryId)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    /** JSON decode. */
    private fun decodeWords(json: String): List<WordEntry> {
        return runCatching {
            val array = JSONArray(json)
            val temp = ArrayList<WordEntry>(array.length())
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val term = item.optString("term", "").trim()
                val meaning = item.optString("meaning", "").trim()
                val id = item.optLong("id", -1L)
                val correctCount = item.optInt("correctCount", 0)
                val incorrectCount = item.optInt("incorrectCount", 0)
                val categoryId = item.optString("categoryId", "uncategorized")
                if (id >= 0 && term.isNotEmpty() && meaning.isNotEmpty()) {
                    temp.add(
                        WordEntry(
                            id = id,
                            term = term,
                            meaning = meaning,
                            correctCount = correctCount,
                            incorrectCount = incorrectCount,
                            categoryId = categoryId
                        )
                    )
                }
            }
            // Remove duplicate ids (latest wins) and keep ascending order for stability.
            temp.associateBy { it.id }.values.sortedBy { it.id }
        }.getOrElse { emptyList() }
    }

    /** Utility: clear everything. */
    suspend fun clear() {
        context.wordDataStore.edit { it[WORDS_KEY] = EMPTY_JSON }
    }
}
