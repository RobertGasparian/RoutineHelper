package com.robertgasparian.routinehelper.domain.usecase

internal fun normalizeCurrentListTitle(title: String): String? =
    title.trim().takeIf(String::isNotEmpty)

internal fun normalizeCurrentListDescription(description: String?): String? =
    description?.trim()?.takeIf(String::isNotEmpty)
