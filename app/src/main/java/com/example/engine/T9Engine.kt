package com.example.engine

object T9Engine {

    private val CHAR_TO_DIGIT: Map<Char, Char> = buildMap {
        put('1', '1')
        ".!?,@#$-_/()".forEach { put(it, '1') }

        "abcABCàáâãäåçÀÁÂÃÄÅÇ".forEach { put(it, '2') }
        put('2', '2')

        "defDEFèéêëÈÉÊË".forEach { put(it, '3') }
        put('3', '3')

        "ghiGHIìíîïÌÍÎÏ".forEach { put(it, '4') }
        put('4', '4')

        "jklJKL".forEach { put(it, '5') }
        put('5', '5')

        "mnoMNOòóôõöøñÒÓÔÕÖØÑ".forEach { put(it, '6') }
        put('6', '6')

        "pqrsPQRSß".forEach { put(it, '7') }
        put('7', '7')

        "tuvTUVùúûüÙÚÛÜ".forEach { put(it, '8') }
        put('8', '8')

        "wxyzWXYZýÝ".forEach { put(it, '9') }
        put('9', '9')

        put('0', '0')
        put('+', '0')
        put(' ', '0')
    }

    val DIGIT_TO_LETTERS: Map<Char, String> = mapOf(
        '1' to ". , !",
        '2' to "ABC",
        '3' to "DEF",
        '4' to "GHI",
        '5' to "JKL",
        '6' to "MNO",
        '7' to "PQRS",
        '8' to "TUV",
        '9' to "WXYZ",
        '0' to "+ _",
        '*' to "KEY",
        '#' to "THEME"
    )

    // Pre-indexed mobile dictionary for predictive text suggestions
    private val PREDICTIVE_DICTIONARY = listOf(
        "GOOGLE", "CHROME", "CAMERA", "PHOTOS", "SETTINGS", "YOUTUBE", "WHATSAPP",
        "SPOTIFY", "MAPS", "GMAIL", "FILES", "DRIVE", "CONTACTS", "MESSAGES",
        "PHONE", "CALCULATOR", "CALENDAR", "CLOCK", "ALARM", "WIFI", "BLUETOOTH",
        "BATTERY", "STORAGE", "DISPLAY", "SOUND", "SECURITY", "DOWNLOAD", "PDF",
        "AUDIO", "VIDEO", "DOCUMENT", "NOTES", "VOICE", "BACKUP", "UPDATE",
        "ACCOUNT", "PRIVACY", "AIRPLANE", "FLIGHT", "HOTSPOT", "SEARCH", "CALL",
        "BROWSER", "WEATHER", "GALLERY", "RECORDER", "DEVICE", "NETWORK", "THEME",
        "WIDGET", "SCREEN", "WALLPAPER", "NOTIFICATIONS", "APPS", "INSTALL",
        "PLAY", "STORE", "SYNC", "WALLET", "PASSWORDS", "HELP", "FEEDBACK"
    )

    // Map each dictionary word to its T9 digits sequence
    private val DICTIONARY_T9_MAP: List<Pair<String, String>> by lazy {
        PREDICTIVE_DICTIONARY.map { word ->
            word to textToT9(word)
        }
    }

    /**
     * Converts a string of text into its T9 numeric sequence.
     * e.g., "Google" -> "466453"
     */
    fun textToT9(text: String): String {
        val sb = StringBuilder(text.length)
        for (ch in text) {
            val digit = CHAR_TO_DIGIT[ch]
            if (digit != null) {
                sb.append(digit)
            }
        }
        return sb.toString()
    }

    data class MatchResult(
        val isMatch: Boolean,
        val score: Int,
        val matchedRanges: List<IntRange>
    )

    /**
     * Matches a target text against a T9 query string (e.g. "4664").
     * Supports:
     * 1. Exact start prefix match
     * 2. Word boundary prefix match (e.g. "Google Chrome" matching "247" for Chrome)
     * 3. Acronym match (e.g. "Google Chrome" -> "42" for GC)
     * 4. Substring match
     */
    fun matchT9(targetText: String, queryDigits: String): MatchResult {
        if (queryDigits.isEmpty()) {
            return MatchResult(true, 0, emptyList())
        }

        // Handle case where query is regular alphanumeric instead of digits
        val isAllDigits = queryDigits.all { it.isDigit() }
        val cleanQuery = if (isAllDigits) queryDigits else textToT9(queryDigits)

        if (cleanQuery.isEmpty()) {
            return MatchResult(false, 0, emptyList())
        }

        val targetT9 = textToT9(targetText)

        // 1. Exact start prefix match
        if (targetT9.startsWith(cleanQuery)) {
            val endIdx = findOriginalIndexForT9Length(targetText, cleanQuery.length)
            return MatchResult(
                isMatch = true,
                score = 1000 + (100 - targetText.length.coerceAtMost(100)),
                matchedRanges = listOf(0 until endIdx)
            )
        }

        // 2. Word boundary prefix match
        val words = findWordOffsets(targetText)
        for ((wordStart, word) in words) {
            val wordT9 = textToT9(word)
            if (wordT9.startsWith(cleanQuery)) {
                val endIdx = wordStart + findOriginalIndexForT9Length(word, cleanQuery.length)
                return MatchResult(
                    isMatch = true,
                    score = 800 + (50 - wordStart.coerceAtMost(50)),
                    matchedRanges = listOf(wordStart until endIdx)
                )
            }
        }

        // 3. Acronym / Initialism match (e.g. "Google Maps" -> 'G', 'M' -> "46")
        if (words.size > 1 && cleanQuery.length <= words.size) {
            val acronymDigits = StringBuilder()
            val matchedWordStarts = mutableListOf<Int>()
            for ((wordStart, word) in words) {
                if (word.isNotEmpty()) {
                    val firstCharT9 = CHAR_TO_DIGIT[word[0]]
                    if (firstCharT9 != null) {
                        acronymDigits.append(firstCharT9)
                        matchedWordStarts.add(wordStart)
                    }
                }
            }
            val acronymStr = acronymDigits.toString()
            if (acronymStr.startsWith(cleanQuery)) {
                val ranges = mutableListOf<IntRange>()
                for (i in 0 until cleanQuery.length) {
                    val start = matchedWordStarts[i]
                    ranges.add(start..start)
                }
                return MatchResult(
                    isMatch = true,
                    score = 750,
                    matchedRanges = ranges
                )
            }
        }

        // 4. Substring match
        val subIndex = targetT9.indexOf(cleanQuery)
        if (subIndex >= 0) {
            val startOrig = findOriginalIndexForT9Offset(targetText, subIndex)
            val endOrig = findOriginalIndexForT9Offset(targetText, subIndex + cleanQuery.length)
            return MatchResult(
                isMatch = true,
                score = 500 - subIndex.coerceAtMost(200),
                matchedRanges = listOf(startOrig until endOrig)
            )
        }

        return MatchResult(false, 0, emptyList())
    }

    /**
     * Special match for phone numbers (matches raw numeric digits ignoring formatting like +1, -, spaces)
     */
    fun matchPhoneNumber(phoneNumber: String, queryDigits: String): MatchResult {
        if (queryDigits.isEmpty()) return MatchResult(true, 0, emptyList())
        val digitsOnly = phoneNumber.filter { it.isDigit() }
        val queryOnly = queryDigits.filter { it.isDigit() }
        if (queryOnly.isEmpty()) return MatchResult(false, 0, emptyList())

        val idx = digitsOnly.indexOf(queryOnly)
        if (idx >= 0) {
            val score = if (idx == 0) 950 else 700 - idx
            return MatchResult(true, score, emptyList())
        }
        return MatchResult(false, 0, emptyList())
    }

    /**
     * Returns predictive dictionary suggestions based on the typed T9 digits.
     */
    fun getPredictiveSuggestions(queryDigits: String, limit: Int = 6): List<String> {
        val cleanDigits = queryDigits.filter { it.isDigit() }
        if (cleanDigits.isEmpty()) return emptyList()

        val results = mutableListOf<String>()

        // 1. Exact or prefix matches in predictive dictionary
        for ((word, t9) in DICTIONARY_T9_MAP) {
            if (t9.startsWith(cleanDigits)) {
                results.add(word)
                if (results.size >= limit) return results
            }
        }

        // 2. Substring matches if prefix matches are scarce
        if (results.size < limit && cleanDigits.length >= 2) {
            for ((word, t9) in DICTIONARY_T9_MAP) {
                if (t9.contains(cleanDigits) && !results.contains(word)) {
                    results.add(word)
                    if (results.size >= limit) break
                }
            }
        }

        return results
    }

    private fun findWordOffsets(text: String): List<Pair<Int, String>> {
        val list = mutableListOf<Pair<Int, String>>()
        var inWord = false
        var wordStart = 0

        for (i in text.indices) {
            val c = text[i]
            if (c.isLetterOrDigit()) {
                if (!inWord) {
                    inWord = true
                    wordStart = i
                }
            } else {
                if (inWord) {
                    list.add(wordStart to text.substring(wordStart, i))
                    inWord = false
                }
            }
        }
        if (inWord) {
            list.add(wordStart to text.substring(wordStart, text.length))
        }
        return list
    }

    private fun findOriginalIndexForT9Length(text: String, t9Len: Int): Int {
        var count = 0
        for (i in text.indices) {
            if (CHAR_TO_DIGIT.containsKey(text[i])) {
                count++
                if (count >= t9Len) return i + 1
            }
        }
        return text.length
    }

    private fun findOriginalIndexForT9Offset(text: String, t9Offset: Int): Int {
        var count = 0
        for (i in text.indices) {
            if (count >= t9Offset) return i
            if (CHAR_TO_DIGIT.containsKey(text[i])) {
                count++
            }
        }
        return text.length
    }
}
