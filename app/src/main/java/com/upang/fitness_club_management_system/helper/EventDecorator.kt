package com.upang.fitness_club_management_system.helper

import android.graphics.Color
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan

class EventDecorator(private val dates: HashSet<CalendarDay>) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day) // Check if this day should be decorated
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(DotSpan(8f, Color.BLUE)) // Adds a small BLUE dot below the date
    }
}
