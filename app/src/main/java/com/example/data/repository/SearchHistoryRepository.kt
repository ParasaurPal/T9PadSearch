package com.example.data.repository

import com.example.data.local.SearchHistoryDao
import com.example.data.local.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

class SearchHistoryRepository(private val searchHistoryDao: SearchHistoryDao) {

    fun getRecentSearches(limit: Int = 15): Flow<List<SearchHistoryEntity>> {
        return searchHistoryDao.getRecentSearches(limit)
    }

    suspend fun recordSearch(
        query: String,
        displayTitle: String = "",
        category: String = "ALL"
    ) {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) return

        val existing = searchHistoryDao.getByQuery(cleanQuery)
        val entry = SearchHistoryEntity(
            query = cleanQuery,
            displayTitle = displayTitle.ifBlank { existing?.displayTitle ?: "" },
            category = category,
            searchCount = (existing?.searchCount ?: 0) + 1,
            timestamp = System.currentTimeMillis()
        )
        searchHistoryDao.upsertSearch(entry)
    }

    suspend fun deleteSearch(query: String) {
        searchHistoryDao.deleteByQuery(query)
    }

    suspend fun clearHistory() {
        searchHistoryDao.clearAll()
    }
}
