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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.model.data.SchoolData;

public class SchoolListAdapter extends RecyclerView.Adapter<SchoolListAdapter.ViewHolder> implements Filterable {

    public interface ItemClickListener{
        void onItemClick(SchoolData item);
        void onFilteringCompleted();
    }

    private Context mContext;
    private List<SchoolData> mList;
    private List<SchoolData> _filteredList;
    private ItemClickListener _listener;

    public SchoolListAdapter(Context mContext, List<SchoolData> list, ItemClickListener _listener) {
        this.mContext = mContext;
        this.mList = list;
        this._filteredList = list;
        this._listener = _listener;
    }

    @NonNull
    @Override
    public SchoolListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_school_list_item, parent, false);
        return new SchoolListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SchoolListAdapter.ViewHolder holder, int position) {
        if(position == NO_POSITION) return;
        SchoolData schoolData = _filteredList.get(position);
        holder.tvSchoolName.setText(schoolData.scName);
    }

    @Override
    public int getItemCount() {
        if(_filteredList == null) return 0;
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
                    List<SchoolData> filteringList = new ArrayList<>();
                    filteringList = mList.stream().filter(t->t.scName.contains(trigger)).collect(Collectors.toList());
                    _filteredList = filteringList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = _filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                _filteredList = (List<SchoolData>) filterResults.values;
                notifyDataSetChanged();
                _listener.onFilteringCompleted();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvSchoolName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSchoolName = itemView.findViewById(R.id.tv_school_name);

            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (_filteredList.size() > 0) _listener.onItemClick(_filteredList.get(position));
            });
        }
    }
}
