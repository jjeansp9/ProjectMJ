package kr.jeet.edu.manager.view.calendar.decorator;

import android.content.Context;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Calendar;

import kr.jeet.edu.manager.R;

public class OtherSundayDecorator implements DayViewDecorator {

    private int color;
    private Context context;
    private CalendarDay selDay;

    public OtherSundayDecorator(Context mContext) {
        this.context = mContext;
        this.color = ContextCompat.getColor(this.context, R.color.sunday_other_month);
    }

    public void setSelectedDay(CalendarDay day) {
        this.selDay = day;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        if (day == null) return false;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day.getDate());

        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        return selDay.getMonth() != day.getMonth() && weekDay == Calendar.SUNDAY;
    }

    @Override
    public void decorate(DayViewFacade view) {
        if (view != null && context != null) view.addSpan(new ForegroundColorSpan(color));
    }
}
