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
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.model.data.AnswererData;

public class AnswererListAdapter extends RecyclerView.Adapter<AnswererListAdapter.ViewHolder> implements Filterable {
    public interface ItemClickListener {
//        void onItemClick(int position, AnswererData item);
        void onCheckChanged(int position, AnswererData item, boolean isChecked);
        void onFilteringCompleted();
    }
    private Context _context;
    private List<AnswererData> _list;
    private List<AnswererData> _filteredList;
    private ItemClickListener _listener;
    public AnswererListAdapter(Context context, List<AnswererData> list, ItemClickListener listener) {
        this._context = context;
        this._list = this._filteredList = list;
        this._listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(_context).inflate(R.layout.layout_answerer_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position == NO_POSITION) return;
        AnswererData item = _filteredList.get(position);
        holder.tvName.setText(item.sfName);
        holder.cbSelect.setChecked(item.isSelected);
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
                if(TextUtils.isEmpty(trigger)) {
                    _filteredList = _list;
                }else{
                    List<AnswererData> filteringList = new ArrayList<>();
                    filteringList = _list.stream().filter(t->t.sfName.contains(trigger)).collect(Collectors.toList());
                    _filteredList = filteringList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = _filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                _filteredList = (List<AnswererData>) results.values;
                notifyDataSetChanged();
                _listener.onFilteringCompleted();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvName;
        private LinearLayoutCompat layoutCheckBox;
        private MaterialCheckBox cbSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            layoutCheckBox = itemView.findViewById(R.id.layout_checkbox);
            cbSelect = itemView.findViewById(R.id.cb_select);
            layoutCheckBox.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if(position == NO_POSITION) return;
                if(_filteredList.size() > 0) {
                    AnswererData item = _filteredList.get(position);
                    item.isSelected = !item.isSelected;
                    cbSelect.setChecked(item.isSelected);
//                    notifyItemChanged(position);
                    _listener.onCheckChanged(position, _filteredList.get(position), item.isSelected);
                }
            });
        }
    }
}
