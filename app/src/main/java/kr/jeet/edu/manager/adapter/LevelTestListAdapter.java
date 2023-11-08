package kr.jeet.edu.manager.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Optional;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.model.data.LTCData;
import kr.jeet.edu.manager.model.data.LevelTestData;

public class LevelTestListAdapter extends RecyclerView.Adapter<LevelTestListAdapter.ViewHolder> {
    public interface ItemClickListener{ public void onItemClick(LevelTestData item); }

    private Context mContext;
    private ArrayList<LevelTestData> mList;
    private ItemClickListener _listener;

    public LevelTestListAdapter(Context mContext, ArrayList<LevelTestData> mList, ItemClickListener _listener) {
        this.mContext = mContext;
        this.mList = mList;
        this._listener = _listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_leveltest_item, parent, false);
        return new LevelTestListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LevelTestData item = mList.get(position);

        if (item != null){
            holder.tvName.setText(item.name);
            LTCData currentData = DataManager.getInstance().getLTCData(item.bigo);
            if(currentData != null) {
                holder.tvCampus.setText(currentData.ltcName);
            }
            holder.tvDate.setText(item.reservationDate);
        }
    }

    @Override
    public int getItemCount() {
        if(mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvName, tvCampus, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_test_reserve_name);
            tvCampus = itemView.findViewById(R.id.tv_test_reserve_campus);
            tvDate = itemView.findViewById(R.id.tv_test_reserve_date);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList.get(position));
            });
        }
    }
}
