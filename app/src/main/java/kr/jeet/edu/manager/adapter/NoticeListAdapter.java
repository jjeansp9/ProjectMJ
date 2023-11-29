package kr.jeet.edu.manager.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.fcm.FCMManager;
import kr.jeet.edu.manager.model.data.NoticeData;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;

public class NoticeListAdapter extends RecyclerView.Adapter<NoticeListAdapter.ViewHolder> {

    public interface ItemClickListener{ public void onItemClick(NoticeData item); }

    private Context mContext;
    private ArrayList<NoticeData> mList;
    private ItemClickListener _listener;
    private int _seq = 0;

    public NoticeListAdapter(Context mContext, ArrayList<NoticeData> mList, ItemClickListener _listener) {
        this.mContext = mContext;
        this.mList = mList;
        this._listener = _listener;
        _seq = PreferenceUtil.getUserSeq(mContext);
    }

    SimpleDateFormat _dateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm_ss, Locale.KOREA);

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_notice_list_item, parent, false);
        return new NoticeListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NoticeData item = mList.get(position);
        boolean isReceivedMsg = true;
        String noticeType = TextUtils.isEmpty(item.searchType) ? "" : item.searchType;
        if (noticeType.equals(FCMManager.MSG_TYPE_SYSTEM)) {
//            holder.tvType.setText(mContext.getString(R.string.settings_notice_system));
            holder.btnNext.setVisibility(View.VISIBLE);
            holder.tvTitle.setText(item.title);
            isReceivedMsg = false;
        }
        else if (noticeType.equals(FCMManager.MSG_TYPE_COUNSEL)) {
//            holder.tvType.setText(mContext.getString(R.string.menu_stu_info_consultation_request_header));
            holder.btnNext.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            if(item.userGubun == Constants.USER_TYPE_STUDENT) {
                if(!TextUtils.isEmpty(item.writerName)) {
                    sb.append(item.writerName);
                }else if(!TextUtils.isEmpty(item.memberName)) {
                    sb.append(item.memberName);
                }
            }else if(item.userGubun == Constants.USER_TYPE_PARENTS) {
                if(!TextUtils.isEmpty(item.writerName)) {
                    sb.append(item.writerName);
                }
                if(!TextUtils.isEmpty(item.memberName) && !item.writerName.equals(item.memberName)) {
                    sb.append(String.format(" (%s 학부모)", item.memberName));
                }
            }
            holder.tvTitle.setText(sb.toString());
        }
        else if (noticeType.equals(FCMManager.MSG_TYPE_LEVEL_TEST)) {
//            holder.tvType.setText(mContext.getString(R.string.main_menu_test_reserve));
            holder.btnNext.setVisibility(View.VISIBLE);
            holder.tvTitle.setText(item.title);
        }
        else {
//            holder.tvType.setText(TextUtils.isEmpty(item.searchType) ? "정보없음" : item.searchType);
            holder.btnNext.setVisibility(View.GONE);
        }


        //holder.tvAttState.setText(TextUtils.isEmpty(item.noticeAttendanceState) ? "" : item.noticeAttendanceState);
        //holder.tvDate.setText(TextUtils.isEmpty(item.date) ? "" : Utils.formatNoticeDate(item.date));
//        holder.tvDate.setText(TextUtils.isEmpty(item.insertDate.toString()) ? "" : _dateFormat.format(item.insertDate));
        String date = "";
//        if(!TextUtils.isEmpty(item.counselDate)) date = item.counselDate;
//        else
            if(!TextUtils.isEmpty(item.insertDate)) date = Utils.formatDate(item.insertDate, Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm_ss, Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm);;
        holder.tvDate.setText(date);
        //holder.tvReceiver.setText(TextUtils.isEmpty(item.noticeReceiver) ? "" : item.noticeReceiver);
        if(isReceivedMsg) {
            Glide.with(mContext).load(R.drawable.img_receive).into(holder.imgSenderAndReceiver);
        }else{
            Glide.with(mContext).load(R.drawable.img_send).into(holder.imgSenderAndReceiver);
        }
//        holder.imgSenderAndReceiver.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        if(mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

//        private TextView tvType,
        private TextView tvTitle, tvDate, tvReceiver;
        private ImageView imgSenderAndReceiver;
        private ImageButton btnNext;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            btnNext = itemView.findViewById(R.id.btn_notice_next);
//            tvType = itemView.findViewById(R.id.tv_notice_type);
            tvTitle = itemView.findViewById(R.id.tv_notice_title);
            tvDate = itemView.findViewById(R.id.tv_notice_date);
            tvReceiver = itemView.findViewById(R.id.tv_notice_receiver);
            imgSenderAndReceiver = itemView.findViewById(R.id.img_notice_send_and_receiver);

            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (mList.size() > 0) _listener.onItemClick(mList.get(position));
            });
        }
    }
}
