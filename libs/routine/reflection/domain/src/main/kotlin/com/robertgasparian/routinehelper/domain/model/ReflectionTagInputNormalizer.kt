package com.robertgasparian.routinehelper.domain.model

import java.util.Locale
import javax.inject.Inject

class ReflectionTagInputNormalizer @Inject constructor() {
    fun normalizeLabel(label: String): String {
        val normalized = label.trim().replace(Whitespace, " ")
        require(normalized.isNotEmpty()) { "Reflection tag label cannot be blank" }
        require(normalized.length <= MAX_LABEL_LENGTH) {
            "Reflection tag label cannot exceed $MAX_LABEL_LENGTH characters"
        }
        return normalized
    }

    fun normalizedKey(label: String): String = normalizeLabel(label).lowercase(Locale.ROOT)

    companion object {
        const val MAX_LABEL_LENGTH = 40
        private val Whitespace = Regex("\\s+")
    }
}
