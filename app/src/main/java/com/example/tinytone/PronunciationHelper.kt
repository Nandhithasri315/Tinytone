package com.example.tinytone

object PronunciationHelper {

    private val fillerWords = setOf("the", "a", "an", "um", "uh", "like", "so", "i", "said")

    fun normalize(text: String): String =
        text.lowercase()
            .replace(Regex("[^a-z\\s]"), "")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() && it !in fillerWords }
            .joinToString(" ")

    fun similarityScore(target: String, spoken: String): Float {
        val targetClean = normalize(target)
        val spokenClean = normalize(spoken)

        if (targetClean.isEmpty() || spokenClean.isEmpty()) return 0f
        if (targetClean == spokenClean) return 1f

        val spokenWords = spokenClean.split(Regex("\\s+"))
        if (spokenWords.contains(targetClean)) return 1f

        val bestWordFuzzy = spokenWords.maxOf { fuzzy(targetClean, it) }
        val phraseFuzzy = fuzzy(targetClean, spokenClean)
        val bestPhonetic = spokenWords.maxOf { phoneticScore(targetClean, it) }

        return maxOf(bestWordFuzzy, phraseFuzzy, bestPhonetic).coerceIn(0f, 1f)
    }

    private fun fuzzy(a: String, b: String): Float {
        if (a == b) return 1f
        val dist = levenshtein(a, b)
        val maxLen = maxOf(a.length, b.length)
        return (1f - dist.toFloat() / maxLen).coerceAtLeast(0f)
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        return dp[a.length][b.length]
    }

    private fun phonetic(word: String): String {
        var value = word.lowercase()
        value = value.replace(Regex("ph"), "f")
        value = value.replace(Regex("ck|k"), "k")
        value = value.replace(Regex("gh"), "")
        value = value.replace(Regex("wh"), "w")
        value = value.replace(Regex("tion|sion"), "shun")
        value = value.replace(Regex("ee|ea|ie"), "e")
        value = value.replace(Regex("oo|ou|ow"), "o")
        value = value.replace(Regex("ai|ay|ei"), "a")
        value = value.replace(Regex("([a-z])\\1"), "$1")
        return value
    }

    private fun phoneticScore(a: String, b: String): Float = fuzzy(phonetic(a), phonetic(b))

    fun hint(target: String, spoken: String): String {
        val targetClean = normalize(target)
        val spokenClean = normalize(spoken)

        if (spokenClean.isEmpty()) return "Say the word out loud so we can hear it."
        if (targetClean == spokenClean) return "Perfect. That sounded clear and strong."

        val score = similarityScore(target, spoken)
        return when {
            score >= 0.85f -> "So close. Say it one more time, a little more clearly."
            score >= 0.60f -> "Good try. Listen once, then copy the word again."
            targetClean.isNotEmpty() && spokenClean.isNotEmpty() && targetClean[0] != spokenClean[0] ->
                "Start with the first sound: ${targetClean[0].uppercaseChar()}."
            targetClean.isNotEmpty() && spokenClean.isNotEmpty() && targetClean.last() != spokenClean.last() ->
                "Check the ending sound of the word."
            else -> "Listen carefully and try the word again."
        }
    }
}
