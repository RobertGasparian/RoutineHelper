package com.robertgasparian.routinehelper.test

import com.robertgasparian.routinehelper.ui.daily.NoteDateTimeTextProvider

class FakeNoteDateTimeTextProvider : NoteDateTimeTextProvider {
    var timeText = "10:30 AM"
    var dateText = "May 29"
    var weekdayText = "Friday"

    override fun currentTimeText(): String = timeText

    override fun currentDateText(): String = dateText

    override fun currentWeekdayText(): String = weekdayText
}
