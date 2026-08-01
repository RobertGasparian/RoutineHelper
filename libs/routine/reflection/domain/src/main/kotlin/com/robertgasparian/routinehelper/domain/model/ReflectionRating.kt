package com.robertgasparian.routinehelper.domain.model

@JvmInline
value class ReflectionRating(
    val value: Int,
) {
    init {
        require(value in options) {
            "Reflection rating must be between $MINIMUM and $MAXIMUM"
        }
    }

    companion object {
        const val MINIMUM = 1
        const val MAXIMUM = 5
        val options: IntRange = MINIMUM..MAXIMUM
    }
}
