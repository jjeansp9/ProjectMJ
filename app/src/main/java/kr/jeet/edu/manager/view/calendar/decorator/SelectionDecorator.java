package kr.jeet.edu.manager.view.calendar.decorator;

import android.content.Context;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import kr.jeet.edu.manager.R;

public class SelectionDecorator implements DayViewDecorator {

    private int color;
    private Context context;
    private CalendarDay selDay;

    public SelectionDecorator(Context mContext) {
        this.context = mContext;
        this.color = ContextCompat.getColor(this.context, R.color.white);
    }

    public void setSelectedDay(CalendarDay day) {
        this.selDay = day;

    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return day != null && day.equals(selDay);
    }

    @Override
    public void decorate(DayViewFacade view) {
        if (view != null && context != null) view.addSpan(new ForegroundColorSpan(color));
    }
}
