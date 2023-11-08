package kr.jeet.edu.manager.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.model.data.AttendanceData;

public class MonthlyAttendanceListAdapter extends RecyclerView.Adapter<MonthlyAttendanceListAdapter.ViewHolder>{
    private Context _context;
    private List<AttendanceData> _list;
    public MonthlyAttendanceListAdapter(Context context, List<AttendanceData> list) {
        this._context = context;
        this._list = list;
    }
    @NonNull
    @Override
    public MonthlyAttendanceListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(_context).inflate(R.layout.layout_monthly_attendance_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthlyAttendanceListAdapter.ViewHolder holder, int position) {
        if(position == NO_POSITION) return;
        AttendanceData item = _list.get(position);
        if(!TextUtils.isEmpty(item.attendDate)) {
            SimpleDateFormat _dateTransferFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD);
            try {
                Date date = _dateTransferFormat.parse(item.attendDate);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                holder.tvDate.setText(String.format("%d일", cal.get(Calendar.DAY_OF_MONTH)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        Constants.AttendanceStatus status = Constants.AttendanceStatus.getByCode(item.attendGubun);
        String state = status.getName();
        holder.tvState.setText(state);
        Drawable drawable = holder.layoutRoot.getBackground();
        drawable.setTint(_context.getColor(status.getColorRes()));
    }

    @Override
    public int getItemCount() {
        if(_list == null) return 0;
        return _list.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        private ConstraintLayout layoutRoot;
        private TextView tvDate, tvState;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            layoutRoot = itemView.findViewById(R.id.layout_root);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvState = itemView.findViewById(R.id.tv_state);
        }
    }
}
