package com.robertgasparian.routinehelper.ui.settings

import androidx.annotation.StringRes
import androidx.core.os.LocaleListCompat
import com.robertgasparian.routinehelper.features.settings.R

enum class AppLanguage(
    val languageTag: String?,
    @param:StringRes val displayNameRes: Int,
) {
    SystemDefault(
        languageTag = null,
        displayNameRes = R.string.settings_use_system_language,
    ),
    English(
        languageTag = "en",
        displayNameRes = R.string.settings_language_english,
    ),
    Russian(
        languageTag = "ru",
        displayNameRes = R.string.settings_language_russian,
    ),
    Armenian(
        languageTag = "hy",
        displayNameRes = R.string.settings_language_armenian,
    ),
    ;

    fun toLocaleList(): LocaleListCompat =
        languageTag?.let(LocaleListCompat::forLanguageTags)
            ?: LocaleListCompat.getEmptyLocaleList()

    companion object {
        fun fromLocaleList(localeList: LocaleListCompat): AppLanguage {
            val language = localeList.get(0)?.language ?: return SystemDefault
            return entries.firstOrNull { appLanguage -> appLanguage.languageTag == language }
                ?: SystemDefault
        }
    }
}
