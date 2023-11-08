package kr.jeet.edu.manager.view.calendar.decorator;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.LineBackgroundSpan;
import android.text.style.TextAppearanceSpan;
import android.util.DisplayMetrics;

import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.model.data.AttendanceSummaryData;
import kr.jeet.edu.manager.utils.LogMgr;

public class AttendanceDecorator implements DayViewDecorator {
    private int color;
    private HashSet<AttendanceSummaryData> dates;
    private Context context;
    public Constants.AttendanceStatus type;

    public AttendanceDecorator(Context mContext, HashSet<AttendanceSummaryData> dates, Constants.AttendanceStatus type) {
        this.context = mContext;
        this.type = type;
        this.color = ContextCompat.getColor(this.context, type.getColorRes());
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
//        LogMgr.e("decorator", "day = " + day);
        Set<CalendarDay> decorateDay = dates.stream().map(t->t.attendDate).collect(Collectors.toSet());
        boolean result = decorateDay.contains(day);
//        int attendGubun = -1;
//        Optional optional = dates.stream().filter(t->t.attendDate.equals(day)).findFirst();
//        if(optional.isPresent()){
//            attendGubun = ((AttendanceSummaryData) optional.get()).attendGubun;
//        }
//        Constants.AttendanceStatus attendStatus = Constants.AttendanceStatus.getByCode(attendGubun);
//        LogMgr.e("decorator", "result = " + result);
//        if(attendStatus != null && result) {
//            LogMgr.e("decorator", "status = " + attendStatus.getName() + day.toString());
//            this.color = ContextCompat.getColor(this.context,attendStatus.getColorRes());
//        }
        return result;
    }

    @Override
    public void decorate(DayViewFacade view) {
        if (view != null) {
//            SpannableStringBuilder spanBuilder = new SpannableStringBuilder();
//            spanBuilder.append("출석");
//            spanBuilder.setSpan(new BackgroundColorSpan(context.getColor(R.color.red)), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

//            DayViewSpan span = new DayViewSpan("출석", context.getColor(R.color.blue),  context.getColor(R.color.white));
//            view.addSpan(span);
            if (view != null) {
                view.addSpan(new DotSpan(DataManager.getInstance().DOT_SIZE, color));
            }
        }
    }

    public void setDates(Collection<AttendanceSummaryData> collection){
        this.dates = new HashSet<>(collection);
    }
//    public class DayViewSpan implements LineBackgroundSpan {
//        private String text;
//        private int backgroundColor;
//        private int textColor;
//
//        public DayViewSpan(String text, int backgroundColor, int textColor) {
//            this.text = text;
//            this.backgroundColor = backgroundColor;
//            this.textColor = textColor;
//        }
//
//        @Override
//        public void drawBackground(
//                Canvas canvas, Paint paint, int left, int right, int top, int baseline, int bottom,
//                CharSequence text, int start, int end, int lineNumber
//        ) {
//            // 텍스트 뒷 배경을 그리는 부분
//            Paint.Style originalStyle = paint.getStyle();
//            int originalColor = paint.getColor();
//
//            paint.setStyle(Paint.Style.FILL);
//            paint.setColor(backgroundColor);
//
//            canvas.drawRect(left + 20, top + 50, right - 20, bottom + 40, paint);
//
//            paint.setStyle(originalStyle);
//            paint.setColor(originalColor);
//
//            // 텍스트를 그리는 부분
//            paint.setColor(textColor);
//
//            // 텍스트 위치를 조정하여 가운데 정렬
//            int x = (left + right) / 4;
//            int y = bottom + 30;
//
//            canvas.drawText(this.text, x, y, paint);
//        }
//    }
//    public class DayViewSpan implements DayViewDecorator {
//        private String text;
//        private int backgroundColor;
//        private int textColor;
//
//        public DayViewSpan(String text, int backgroundColor, int textColor) {
//            this.text = text;
//            this.backgroundColor = backgroundColor;
//            this.textColor = textColor;
//        }
//
//        @Override
//        public boolean shouldDecorate(CalendarDay day) {
//            // Customize this logic to decide which days should have this span.
//            return dates.stream().map(t -> t.attendDate).collect(Collectors.toSet()).contains(day);
//        }
//
//        @Override
//        public void decorate(DayViewFacade view) {
//            // Customize this logic to apply your custom span to the day view.
//            view.addSpan(new ForegroundColorSpan(textColor));
//            view.addSpan(new BackgroundColorSpan(backgroundColor));
//            // Add any other spans you want here.
//        }
//    }
}
