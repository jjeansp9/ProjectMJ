package kr.jeet.edu.manager.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.RelativeLayout;
import android.app.TimePickerDialog;
import android.widget.TimePicker;

import androidx.annotation.NonNull;

import java.util.Objects;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.view.CustomTimePicker;

public class TimePickerFragment extends Dialog implements TimePickerDialog.OnTimeSetListener {

    private Context context;
    private CustomTimePicker timePicker;
    private RelativeLayout cancelBtn, okBtn;
    private OnTimeSetListener listener;

    public TimePickerFragment(@NonNull Context context, OnTimeSetListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        initView();
    }

    public interface OnTimeSetListener {
        void onTimeSet(int hourOfDay, int minute);
    }

    public void setTime(int hour, int minute) {
        timePicker.setHour(hour);
        timePicker.setMinute(minute);
    }

    private void initView() {
        this.setCanceledOnTouchOutside(false);
        this.setCancelable(false);

        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.custom_time_picker);

        timePicker = (CustomTimePicker) findViewById(R.id.time_picker);
        cancelBtn = (RelativeLayout) findViewById(R.id.cancelBtn);
        okBtn = (RelativeLayout) findViewById(R.id.okBtn);
    }

    public void setOnOkButtonClickListener(View.OnClickListener listener) {
        okBtn.setOnClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            onTimeSet(timePicker, hour, minute);
            listener.onClick(v);
        });
    }

    public void setOnCancelButtonClickListener(View.OnClickListener listener) {
        cancelBtn.setVisibility(View.VISIBLE);
        cancelBtn.setOnClickListener(listener);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (listener != null) {
            listener.onTimeSet(hourOfDay, minute);
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
