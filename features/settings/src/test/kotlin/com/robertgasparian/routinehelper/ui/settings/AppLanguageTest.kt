package com.robertgasparian.routinehelper.ui.settings

import androidx.core.os.LocaleListCompat
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {
    @Test
    fun `given empty locale list when mapped then system default is selected`() {
        assertEquals(
            AppLanguage.SystemDefault,
            AppLanguage.fromLocaleList(LocaleListCompat.getEmptyLocaleList()),
        )
    }

    @Test
    fun `given supported locale tags when mapped then matching languages are selected`() {
        assertEquals(
            AppLanguage.English,
            AppLanguage.fromLocaleList(LocaleListCompat.forLanguageTags("en-US")),
        )
        assertEquals(
            AppLanguage.Russian,
            AppLanguage.fromLocaleList(LocaleListCompat.forLanguageTags("ru")),
        )
        assertEquals(
            AppLanguage.Armenian,
            AppLanguage.fromLocaleList(LocaleListCompat.forLanguageTags("hy-AM")),
        )
    }
}
