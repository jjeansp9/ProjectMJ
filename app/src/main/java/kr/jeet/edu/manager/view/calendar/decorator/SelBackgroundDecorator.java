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

public class SelBackgroundDecorator implements DayViewDecorator {

    private Context context;
    private Drawable drawable, transRes;
    private CalendarDay selDay;

    public SelBackgroundDecorator(Context mContext) {
        this.context = mContext;
        this.drawable = ContextCompat.getDrawable(context, R.drawable.selector_bg_calender);
        this.transRes = new ColorDrawable(Color.TRANSPARENT);
    }

    public void setSelectedDay(CalendarDay day) {this.selDay = day;}

    @Override
    public boolean shouldDecorate(CalendarDay day) {return !selDay.equals(day);}

    @Override
    public void decorate(DayViewFacade view) {
        if (view != null && context != null) {
            view.setSelectionDrawable(drawable);
        }
    }
}
