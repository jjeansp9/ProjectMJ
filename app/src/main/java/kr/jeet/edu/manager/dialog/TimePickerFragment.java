package kr.jeet.edu.manager.dialog;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private OnTimeSetListener listener;
    private int hour = -1;
    private int minute = -1;

    public TimePickerFragment(OnTimeSetListener listener) {
        this.listener = listener;
    }

    public interface OnTimeSetListener {
        void onTimeSet(int hourOfDay, int minute);
    }

    public void setTime(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();

        int hour = (this.hour == -1) ? calendar.get(Calendar.HOUR_OF_DAY) : this.hour;
        int minute = (this.minute == -1) ? calendar.get(Calendar.MINUTE) : this.minute;

        return new TimePickerDialog(requireContext(), this, hour, minute, false);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (listener != null) {
            listener.onTimeSet(hourOfDay, minute);
        }
    }
}
