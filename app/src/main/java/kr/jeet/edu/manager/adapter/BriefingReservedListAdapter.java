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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.model.data.BriefingReservedListData;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.Utils;

public class BriefingReservedListAdapter extends RecyclerView.Adapter<BriefingReservedListAdapter.ViewHolder> {

    private final static String TAG = "BrfListAdapter";

    public interface ItemClickListener{
        public void onDeleteClick(BriefingReservedListData item);
    }

    private Context mContext;
    private List<BriefingReservedListData> mList;
    private ItemClickListener _listener;
    boolean isReservable = true;
    private int _userGubun = 1;
    public BriefingReservedListAdapter(Context mContext, List<BriefingReservedListData> mList, ItemClickListener listener) {
        this.mContext = mContext;
        this._listener = listener;
        this.mList = mList;
    }
    public void setUserGubun(int gubun) {
        this._userGubun = gubun;
    }
    public void setReservable(boolean flag) {this.isReservable = flag; }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_brf_reserve_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position == NO_POSITION) return;
        if (!mList.isEmpty()) {
            BriefingReservedListData item = mList.get(position);
            try {

                String phoneNum = Utils.formatPhoneNumber(item.phoneNumber);
                holder.tvName.setText(item.name);
//                holder.tvSchool.setText(item.schoolNm);
                StringBuilder sb = new StringBuilder();
                if(!TextUtils.isEmpty(item.schoolNm)) {
                    sb.append(item.schoolNm);
                }
                if(!TextUtils.isEmpty(item.grade)) {
                    int grade = 0;
                    Pattern pattern = Pattern.compile("\\d+");
                    Matcher matcher = pattern.matcher(item.grade);
                    if (matcher.find()) {
                        String match1 = matcher.group();
                        grade = Integer.parseInt(match1);
                        if (sb.length() > 0) sb.append("\t");
                        sb.append(String.format("%d학년", grade));
                    }
                }
                if(sb.length() > 0) {
                    holder.tvGrade.setText(sb.toString());
                }
                if(isReservable) {
                    holder.deleteView.setVisibility(View.VISIBLE);
                }else{
                    holder.deleteView.setVisibility(View.GONE);
                }
                if (_userGubun == Constants.USER_TYPE_ADMIN || _userGubun == Constants.USER_TYPE_SUPER_ADMIN) {
                    holder.tvPhoneNum.setText(phoneNum);
                } else {
                    holder.tvPhoneNum.setText(Utils.blindPhoneNumber(phoneNum));
                }

                if (!TextUtils.isEmpty(item.isStudent)) {
                    if (item.isStudent.equals("Y")) holder.tvIsStudent.setText(mContext.getString(R.string.stu));
                    else holder.tvIsStudent.setText(mContext.getString(R.string.stu_non));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvName, tvPhoneNum, tvGrade, tvIsStudent;
        private View deleteView;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_brf_reserved_name);
            tvPhoneNum = itemView.findViewById(R.id.tv_brf_reserved_phone_number);
//            tvSchool = itemView.findViewById(R.id.tv_brf_school);
            tvGrade = itemView.findViewById(R.id.tv_brf_grade);
            tvIsStudent = itemView.findViewById(R.id.tv_brf_is_student);
            deleteView = itemView.findViewById(R.id.view_delete);
            deleteView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                _listener.onDeleteClick(mList.get(position));
            });
        }
    }
}
