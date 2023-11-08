package kr.jeet.edu.manager.view.calendar.decorator;

import android.content.Context;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Collection;
import java.util.HashSet;

import kr.jeet.edu.manager.R;

public class HolidayDecorator implements DayViewDecorator {

    private int color;
    private Context context;
    private HashSet<CalendarDay> dates;
    private CalendarDay selDay;

    public HolidayDecorator(Context mContext, HashSet<CalendarDay> dates) {
        this.context = mContext;
        color = ContextCompat.getColor(this.context, R.color.sunday);
        this.dates = new HashSet<>(dates);
    }

    public void setSelectedDay(CalendarDay day) {
        this.selDay = day;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day) && selDay.getMonth() == day.getMonth();
    }

    @Override
    public void decorate(DayViewFacade view) {
        if (view != null) view.addSpan(new ForegroundColorSpan(color));
    }

    public void setDates(Collection<CalendarDay> collection){
        this.dates = new HashSet<>(collection);
    }
}
