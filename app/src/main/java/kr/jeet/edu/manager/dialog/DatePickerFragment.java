package kr.jeet.edu.manager.dialog;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Date;

import kr.jeet.edu.manager.common.Constants;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private OnDateSetListener listener;
    private int year = 0;
    private int month = 0;
    private int dateOfMonth = 0;
    private Date selectedDate;
    private boolean minDateSet = false;

    public DatePickerFragment(OnDateSetListener listener, Date selectedDate, boolean minDateSet) {
        this.listener = listener;
        this.selectedDate = selectedDate;
        this.minDateSet = minDateSet;
    }

    public DatePickerFragment(OnDateSetListener listener) {
        this.listener = listener;
    }

    public interface OnDateSetListener {
        void onDateSet(int year, int month, int day);
    }

    public void setDate(int year, int month, int date) {
        this.year = year;
        this.month = month;
        this.dateOfMonth = date;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();

        int year = (this.year == 0) ? calendar.get(Calendar.YEAR) : this.year;
        int month = (this.month == 0) ? calendar.get(Calendar.MONTH) : this.month;
        int day = (this.dateOfMonth == 0) ? calendar.get(Calendar.DAY_OF_MONTH) : this.dateOfMonth;

        DatePickerDialog dialog = new DatePickerDialog(requireContext(), this, year, month, day);
        //최소날짜
        Calendar minDate = Calendar.getInstance();
        if (minDateSet) {
            Calendar setCalendar = Calendar.getInstance();
            setCalendar.setTime(selectedDate);

            int setYear = setCalendar.get(Calendar.YEAR);
            int setMonth = setCalendar.get(Calendar.MONTH);
            int setDay = setCalendar.get(Calendar.DAY_OF_MONTH);

            minDate.set(setYear, setMonth, setDay);
        } else {
            minDate.set(Constants.PICKER_MIN_YEAR, 1, 1);
        }
        dialog.getDatePicker().setMinDate(minDate.getTime().getTime());
        //최대날짜
        Calendar maxDate = Calendar.getInstance();
        maxDate.set(Constants.PICKER_MAX_YEAR, 12, 31);
        dialog.getDatePicker().setMaxDate(maxDate.getTime().getTime());
        return dialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        if (listener != null) {
            listener.onDateSet(year, month, dayOfMonth);
        }
    }
}
