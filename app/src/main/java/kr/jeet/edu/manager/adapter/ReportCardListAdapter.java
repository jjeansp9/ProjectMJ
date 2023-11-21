package kr.jeet.edu.manager.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.model.data.ReportCardSummaryData;
import kr.jeet.edu.manager.utils.Utils;

public class ReportCardListAdapter extends RecyclerView.Adapter<ReportCardListAdapter.ViewHolder> implements Filterable {

    public interface ItemClickListener {
        void onItemClick(int position, ReportCardSummaryData item);
        void onFilteringCompleted();
    }
    private Context _context;
    private List<ReportCardSummaryData> _list;
    private List<ReportCardSummaryData> _filteredList;
    private ItemClickListener _listener;

    SimpleDateFormat millisecFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm_ss_SSS);
    SimpleDateFormat minuteFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm);

    public ReportCardListAdapter(Context context, List<ReportCardSummaryData> list, ItemClickListener listener) {
        this._context = context;
        this._list = this._filteredList = list;
        this._listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(_context).inflate(R.layout.layout_report_card_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position == NO_POSITION) return;
        ReportCardSummaryData item = _list.get(position);
        holder.tvTitle.setText(Utils.getStr(item.content));
        holder.tvWriter.setText(Utils.getStr(item.writerName));
        holder.tvStudent.setText(Utils.getStr(item.stName));
        if(item.reportList != null && !item.reportList.isEmpty()) {
            holder.ivCount.setVisibility(View.VISIBLE);
            holder.tvCount.setVisibility(View.VISIBLE);
            holder.tvCount.setText(_context.getString(R.string.content_item_count, item.reportList.size()));
        }else{
            holder.ivCount.setVisibility(View.GONE);
            holder.tvCount.setVisibility(View.GONE);
        }
        holder.tvCampus.setText(item.acaName);
        String dateString = item.insertDate;
        try {
            Date regDate = millisecFormat.parse(item.insertDate);
            dateString = minuteFormat.format(regDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.tvDate.setText(dateString);
    }

    @Override
    public int getItemCount() {
        if(_filteredList == null) return 0;
        return _filteredList.size();
    }

    @Override
    public Filter getFilter() {
        return null;
        /*
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String trigger = charSequence.toString();
                if(trigger.isEmpty() || trigger.equals(mContext.getString(R.string.item_total))) {
                    _filteredList = _list;
                }else{
                    List<BusInfoData> filteringList = new ArrayList<>();
                    filteringList = _list.stream().filter(t->t.busName.equals(trigger)).collect(Collectors.toList());
                    _filteredList = filteringList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = _filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                _filteredList = (List<BusInfoData>) filterResults.values;
                notifyDataSetChanged();
                _listener.onFilteringCompleted();
            }
        };
         */
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvTitle, tvWriter, tvStudent, tvCount, tvCampus, tvDate;
        private ImageView ivArrow, ivCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvWriter = itemView.findViewById(R.id.tv_writer_name);
            tvStudent = itemView.findViewById(R.id.tv_student_name);
            tvCount = itemView.findViewById(R.id.tv_count);
            tvCampus = itemView.findViewById(R.id.tv_campus);
            tvDate = itemView.findViewById(R.id.tv_date);
            ivArrow = itemView.findViewById(R.id.img_icon_to);
            ivCount = itemView.findViewById(R.id.img_icon_count);
            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (_list.size() > 0) _listener.onItemClick(position, _list.get(position));
            });
        }
    }
}
