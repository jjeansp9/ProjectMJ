package kr.jeet.edu.manager.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import kr.jeet.edu.manager.R;

public class CustomTimePicker extends TimePicker {

    private final int minuteInterval;

    public CustomTimePicker(Context context) {
        this(context, null);
    }

    public CustomTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IntervalTimePicker);
        minuteInterval = a.getInt(R.styleable.IntervalTimePicker_interval, DEFAULT_MINUTE_INTERVAL);
        a.recycle();

        setupMinutePicker();
    }

    @SuppressLint("DiscouragedApi")
    private void setupMinutePicker() {
        int minuteResourceId = getResources().getIdentifier("minute", "id", "android");
        NumberPicker minutePicker = findViewById(minuteResourceId);
        if (minutePicker != null) {
            minutePicker.setValue((int) Math.round((float) minutePicker.getValue() / minuteInterval));
            minutePicker.setMinValue(MIN_MINUTE);
            minutePicker.setMaxValue(MAX_MINUTE / minuteInterval - 1);
            String[] displayedValues = new String[MAX_MINUTE / minuteInterval];
            for (int minute = MIN_MINUTE; minute < MAX_MINUTE; minute += minuteInterval) {
                displayedValues[minute / minuteInterval] = String.format("%02d", minute);
            }
            minutePicker.setDisplayedValues(displayedValues);
        }
    }

    @Override
    public void setMinute(int minute) {
        super.setMinute(minute / minuteInterval);
    }

    @Override
    public int getMinute() {
        return super.getMinute() * minuteInterval;
    }

    @Override
    public void setOnTimeChangedListener(final OnTimeChangedListener onTimeChangedListener) {
        super.setOnTimeChangedListener((view, hourOfDay, minute) -> onTimeChangedListener.onTimeChanged(view, hourOfDay, getMinute()));
    }

    private static final int DEFAULT_MINUTE_INTERVAL = 1;
    private static final int MIN_MINUTE = 0;
    private static final int MAX_MINUTE = 60;
}
