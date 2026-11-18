package com.example.data.model

import android.graphics.drawable.Drawable

enum class SearchCategory(val label: String, val icon: String) {
    ALL("All", "⚡"),
    APPS("Apps", "📱"),
    CONTACTS("Contacts", "👤"),
    FILES("Files", "📁"),
    SETTINGS("Settings", "⚙️")
}

sealed class SearchItem {
    abstract val id: String
    abstract val title: String
    abstract val subtitle: String
    abstract val category: SearchCategory
    abstract val matchedRanges: List<IntRange>
    abstract val score: Int

    data class AppItem(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        val packageName: String,
        val activityName: String?,
        val iconDrawable: Drawable? = null,
        val isSystem: Boolean = false,
        override val matchedRanges: List<IntRange> = emptyList(),
        override val score: Int = 0
    ) : SearchItem() {
        override val category: SearchCategory = SearchCategory.APPS
    }

    data class ContactItem(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        val phoneNumber: String,
        val email: String? = null,
        val photoUri: String? = null,
        val lookupKey: String? = null,
        override val matchedRanges: List<IntRange> = emptyList(),
        override val score: Int = 0
    ) : SearchItem() {
        override val category: SearchCategory = SearchCategory.CONTACTS
    }

    data class FileItem(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        val uriString: String,
        val mimeType: String,
        val sizeBytes: Long,
        val extension: String,
        override val matchedRanges: List<IntRange> = emptyList(),
        override val score: Int = 0
    ) : SearchItem() {
        override val category: SearchCategory = SearchCategory.FILES
    }

    data class SettingItem(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        val intentAction: String,
        val keywords: List<String> = emptyList(),
        val systemCategory: String = "System",
        override val matchedRanges: List<IntRange> = emptyList(),
        override val score: Int = 0
    ) : SearchItem() {
        override val category: SearchCategory = SearchCategory.SETTINGS
    }

    data class ActionItem(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        val actionType: ActionType,
        val rawQuery: String,
        override val matchedRanges: List<IntRange> = emptyList(),
        override val score: Int = 0
    ) : SearchItem() {
        override val category: SearchCategory = SearchCategory.ALL
    }
}

enum class ActionType {
    DIAL,
    WEB_SEARCH,
    CALCULATOR
}
