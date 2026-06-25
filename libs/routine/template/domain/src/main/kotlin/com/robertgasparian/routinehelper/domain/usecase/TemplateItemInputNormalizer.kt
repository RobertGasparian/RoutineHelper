package com.robertgasparian.routinehelper.domain.usecase

internal fun normalizeTemplateTitle(title: String): String? =
    title.trim().takeIf(String::isNotEmpty)

internal fun normalizeTemplateDescription(description: String?): String? =
    description?.trim()?.takeIf(String::isNotEmpty)

internal fun normalizeTemplateRepeatTargetCount(repeatTargetCount: Int?): Int? =
    repeatTargetCount?.takeIf { it > 1 }
