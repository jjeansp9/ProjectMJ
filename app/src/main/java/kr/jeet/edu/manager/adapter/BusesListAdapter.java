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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.model.data.BusInfoData;
import kr.jeet.edu.manager.utils.LogMgr;

public class BusesListAdapter extends RecyclerView.Adapter<BusesListAdapter.ViewHolder> implements Filterable {
    public interface ItemClickListener{
        void onItemClick(BusInfoData item);
//        void onCallPhone(BusInfoData item);
        void onFilteringCompleted();
    }
    private Context mContext;
    private List<BusInfoData> _list;
    private List<BusInfoData> _filteredList;
    private ItemClickListener _listener;

    public BusesListAdapter(Context mContext, List<BusInfoData> list, ItemClickListener _listener) {
        this.mContext = mContext;
        this._list = list;
        this._filteredList = list;
        this._listener = _listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_buses_list_item, parent, false);
        return new BusesListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position == NO_POSITION) return;
        BusInfoData busData = _filteredList.get(position);
        holder.tvBus.setText(busData.busName);
//        holder.tvPhoneNo.setText(busData.busPhoneNumber);
        if(!TextUtils.isEmpty(busData.isDrive)) {
            LogMgr.w("busAdapter", "name = " + busData.busName + "isDrive = " + busData.isDrive);
            if(busData.isDrive.equals("Y")) {
                holder.viewBusState.setBackground(mContext.getDrawable(R.drawable.icon_bus_on));
            }else{
                holder.viewBusState.setBackground(mContext.getDrawable(R.drawable.icon_bus_off));
            }
        }
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
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvBus, tvDrive, tvPhoneNo;
        private View viewBusState;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBus = itemView.findViewById(R.id.tv_bus);
            viewBusState = itemView.findViewById(R.id.view_bus_state);


            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (_list.size() > 0) _listener.onItemClick(_list.get(position));
            });
        }
    }
}
