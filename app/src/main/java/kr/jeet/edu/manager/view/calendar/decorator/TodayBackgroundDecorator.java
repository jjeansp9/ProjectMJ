package kr.jeet.edu.manager.view.calendar.decorator;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import kr.jeet.edu.manager.R;

public class TodayBackgroundDecorator implements DayViewDecorator {

    private Context context;
    private Drawable drawable, transRes;
    private CalendarDay today;

    public TodayBackgroundDecorator(Context mContext) {
        this.context = mContext;
        this.drawable = ContextCompat.getDrawable(context, R.drawable.bg_calendar_today);
        this.transRes = new ColorDrawable(Color.TRANSPARENT);
    }

    public void setSelectedDay(CalendarDay day) {this.today = day;}

    @Override
    public boolean shouldDecorate(CalendarDay day) {return today.equals(day);}

    @Override
    public void decorate(DayViewFacade view) {
        if (view != null && context != null) {
            view.setSelectionDrawable(drawable);
        }
    }
}
