package kr.jeet.edu.manager.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.model.data.ScheduleData;
import kr.jeet.edu.manager.utils.LogMgr;

public class ScheduleListAdapter extends RecyclerView.Adapter<ScheduleListAdapter.ViewHolder> implements Filterable {

    private final static String TAG = "ScheduleListAdapter";

    public interface ItemClickListener{
        void onItemClick(ScheduleData item);
        void onFilteringCompleted();
    }

    private Context mContext;
    private List<ScheduleData> mList;
    private List<ScheduleData> _filteredList;
    private ItemClickListener _listener;
//    private String _acaCode = "";

    public ScheduleListAdapter(Context mContext, List<ScheduleData> mList, ItemClickListener listener) {
        this.mContext = mContext;
        this._listener = listener;
        this.mList = mList;
        this._filteredList = mList;
    }

//    public void setACACode(String acaCode) {
//        this._acaCode = acaCode;
//    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_schedule_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position == NO_POSITION) return;
        ScheduleData item = _filteredList.get(position);

        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyyMd", Locale.KOREA);
        SimpleDateFormat outputFormat = new SimpleDateFormat("M.d (E)", Locale.KOREA);
        Date date = null;

        String formattedDate = "";

        try {

            String getDate = String.format(Locale.KOREA, "%d%d%d", item.year,item.month, item.day);
            date = inputDateFormat.parse(getDate);

            if (date != null) {
                formattedDate = outputFormat.format(date);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.tvDate.setText(formattedDate);
        holder.tvTitle.setText(TextUtils.isEmpty(item.title) ? "" : item.title);

        String str = TextUtils.isEmpty(item.acaGubunName) ? "" : "["+item.acaGubunName+"]";

        if (!TextUtils.isEmpty(item.acaGubunName)){
            holder.tvTarget.setText(str);
            holder.tvTarget.setVisibility(View.VISIBLE);
        }else{
//            if (TextUtils.isEmpty(_acaCode))
//            holder.tvTarget.setVisibility(View.INVISIBLE);
//            else
                holder.tvTarget.setVisibility(View.GONE);
        }


//        if (TextUtils.isEmpty(_acaCode)){
//            holder.tvCampus.setText(TextUtils.isEmpty(item.acaName) ? "" : item.acaName);
        if(!TextUtils.isEmpty(item.acaCode)) {
//            if(TextUtils.isEmpty(item.acaGubunName)) {
//                holder.tvCampus.setText(item.acaName);
//            }else{
                holder.tvCampus.setText(item.acaName);
//            }
        }
//            holder.tvCampus.setVisibility(View.VISIBLE);
//        }else{
//            holder.tvCampus.setVisibility(View.GONE);
//        }

//        try{
//
//            String date = item.month+"."+item.day;
//
//            holder.tvDate.setText(date);
//            holder.tvTarget.setText(TextUtils.isEmpty(item.target) ? "" : item.target);
//            holder.tvTitle.setText(TextUtils.isEmpty(item.title) ? "" : item.title);
//
//        }catch (Exception e){
//            LogMgr.e("ListAdapter Exception : " + e.getMessage());
//        }
    }

    @Override
    public int getItemCount() {
        if (_filteredList == null) return 0;
        return _filteredList.size();
    }
    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String trigger = charSequence.toString();
                if(trigger.isEmpty()) {
                    _filteredList = mList;
                }else{
                    int selectedDate = 1;
                    try{
                        selectedDate = Integer.parseInt(trigger);
                    }catch(NumberFormatException e){}
                    List<ScheduleData> filteringList = new ArrayList<>();
                    int finalSelectedDate = selectedDate;
                    filteringList = mList.stream().filter(t->t.day == finalSelectedDate).collect(Collectors.toList());
                    _filteredList = filteringList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = _filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                _filteredList = (List<ScheduleData>) filterResults.values;
                LogMgr.e( "publishResult " + _filteredList.size());
                notifyDataSetChanged();
                _listener.onFilteringCompleted();
            }
        };
    }
    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvDate, tvTarget, tvTitle, tvCampus;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            tvDate = itemView.findViewById(R.id.tv_schedule_date);
            tvTarget = itemView.findViewById(R.id.tv_schedule_target);
            tvTitle = itemView.findViewById(R.id.tv_schedule_title);
            tvCampus = itemView.findViewById(R.id.tv_schedule_campus);

            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (_filteredList.size() > 0) _listener.onItemClick(_filteredList.get(position));
            });
        }
    }
}
