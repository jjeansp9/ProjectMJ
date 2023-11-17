package kr.jeet.edu.manager.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.model.data.ReportCardSummaryData;

public class ReportCardListAdapter extends RecyclerView.Adapter<ReportCardListAdapter.ViewHolder> implements Filterable {

    public interface ItemClickListener {
        void onItemClick(int position, ReportCardSummaryData item);
        void onFilteringCompleted();
    }
    private Context _context;
    private List<ReportCardSummaryData> _list;
    private List<ReportCardSummaryData> _filteredList;
    private ItemClickListener _listener;

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

        private TextView tvTitle, tvEtTitleGubun, tvEtGubun, tvDate;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (_list.size() > 0) _listener.onItemClick(position, _list.get(position));
            });
        }
    }
}
