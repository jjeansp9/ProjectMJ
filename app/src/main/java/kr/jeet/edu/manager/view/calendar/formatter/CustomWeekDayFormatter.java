package kr.jeet.edu.manager.view.calendar.formatter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.format.WeekDayFormatter;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import kr.jeet.edu.manager.R;

public class CustomWeekDayFormatter implements WeekDayFormatter {
    private final Context context;
    private final int defaultColor;
    private final int sundayColor;
    private final int saturdayColor;

    public CustomWeekDayFormatter(Context context) {
        this.context = context;
        this.defaultColor = ContextCompat.getColor(context, R.color.font_color_default);
        this.sundayColor = ContextCompat.getColor(context, R.color.sunday);
        this.saturdayColor = ContextCompat.getColor(context, R.color.saturday);
    }

    @Override
    public CharSequence format(int weekDay) {
        switch (weekDay) {
            case Calendar.SUNDAY:
                return getColoredSpannable("일", sundayColor);
            case Calendar.MONDAY:
                return getColoredSpannable("월", defaultColor);
            case Calendar.TUESDAY:
                return getColoredSpannable("화", defaultColor);
            case Calendar.WEDNESDAY:
                return getColoredSpannable("수", defaultColor);
            case Calendar.THURSDAY:
                return getColoredSpannable("목", defaultColor);
            case Calendar.FRIDAY:
                return getColoredSpannable("금", defaultColor);
            case Calendar.SATURDAY:
                return getColoredSpannable("토", saturdayColor);


            default:
                return new DateFormatSymbols().getWeekdays()[weekDay];
        }
    }

    private SpannableString getColoredSpannable(String text, int color){
        SpannableString spannable = new SpannableString(text);
        spannable.setSpan(new ForegroundColorSpan(color), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }
}
