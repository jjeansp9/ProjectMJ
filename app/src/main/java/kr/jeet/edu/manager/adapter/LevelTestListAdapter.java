package kr.jeet.edu.manager.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.model.data.LTCData;
import kr.jeet.edu.manager.model.data.LevelTestData;
import kr.jeet.edu.manager.utils.Utils;

public class LevelTestListAdapter extends RecyclerView.Adapter<LevelTestListAdapter.ViewHolder> {
    public interface ItemClickListener{ public void onItemClick(int position, LevelTestData item); }

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
                StringBuilder sb = new StringBuilder();
                if(TextUtils.isEmpty(currentData.ltcName)){
                    if(!TextUtils.isEmpty(item.grade)){
                        sb.append(item.grade);
                    }
                }else{
                    sb.append(currentData.ltcName);
                    if(!TextUtils.isEmpty(item.grade)){
                        sb.append(" / ");
                        sb.append(item.grade);
                    }
                }
                holder.tvCampus.setText(sb.toString());
            }
            holder.tvSubject.setText(Utils.getStr(item.subjectName));
            String date = Utils.formatDate(item.insertDate, Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm_ss, Constants.DATE_FORMATTER_YYYY_MM_DD);
            holder.tvRegisterDate.setText(date);
        }
    }

    @Override
    public int getItemCount() {
        if(mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvName, tvCampus, tvRegisterDate, tvSubject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_test_reserve_name);
            tvCampus = itemView.findViewById(R.id.tv_test_reserve_campus);
            tvRegisterDate = itemView.findViewById(R.id.tv_test_register_date);
            tvSubject = itemView.findViewById(R.id.tv_test_register_subject);
//            tvReserveDate = itemView.findViewById(R.id.tv_test_reserve_date);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(position, mList.get(position));
            });
        }
    }
}
