package com.robertgasparian.routinehelper.ui.tracking

import android.content.Context
import com.robertgasparian.routinehelper.core.time.TimeProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

interface NoteDateTimeTextProvider {
    fun currentTimeText(): String

    fun currentDateText(): String

    fun currentWeekdayText(): String
}

class AndroidNoteDateTimeTextProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val timeProvider: TimeProvider,
) : NoteDateTimeTextProvider {
    override fun currentTimeText(): String =
        android.text.format.DateFormat
            .getTimeFormat(context)
            .format(currentDate())

    override fun currentDateText(): String {
        val locale = context.currentLocale()
        return currentText(patternSkeleton = "MMMd", locale = locale)
    }

    override fun currentWeekdayText(): String {
        val locale = context.currentLocale()
        return currentText(patternSkeleton = "EEEE", locale = locale)
    }

    private fun currentText(
        patternSkeleton: String,
        locale: Locale,
    ): String {
        val pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, patternSkeleton)
        return SimpleDateFormat(pattern, locale).format(currentDate())
    }

    private fun currentDate(): Date =
        Date.from(timeProvider.now().toInstant())
}

private fun Context.currentLocale(): Locale =
    resources.configuration.locales[0]
