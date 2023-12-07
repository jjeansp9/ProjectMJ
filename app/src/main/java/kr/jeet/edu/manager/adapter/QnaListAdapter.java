package kr.jeet.edu.manager.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.model.data.QnaData;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;

public class QnaListAdapter extends RecyclerView.Adapter<QnaListAdapter.ViewHolder>{

    private final String TAG = "QnaListAdapter";

    public interface ItemClickListener { public void onItemClick(QnaData item, int position); }

    private Context mContext;
    private List<QnaData> mList;
    private ItemClickListener _listener;

    public QnaListAdapter(Context mContext, List<QnaData> mList, ItemClickListener _listener) {
        this.mContext = mContext;
        this.mList = mList;
        this._listener = _listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_qna_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == NO_POSITION) return;
        QnaData item =  mList.get(position);

        String str = "";

        //setChipGroup(holder, item);

        holder.layoutStatus.removeAllViews();

        if (!TextUtils.isEmpty(item.isMain)) {
            if (item.isMain.equals("Y")) { // 공지 글
                holder.qnaRoot.setBackgroundColor(mContext.getColor(R.color.color_qna_main_background));
                addTag(holder.layoutStatus, R.color.color_notice, "공지");

                if (item.userGubun <= Constants.USER_TYPE_TEACHER) { // 강사, 관리자의 경우
                    if (!item.isOpen.equals("Y")) {
                        addTag(holder.layoutStatus, R.color.color_private, "비공개");
                    }
                    if (item.writerSeq == PreferenceUtil.getUserSeq(mContext)) {
                        addTag(holder.layoutStatus, R.color.color_me, "본인");
                    }
                }

            } else { // 사용자 질문 글
                holder.qnaRoot.setBackgroundColor(mContext.getColor(R.color.white));
                if (item.userGubun >= Constants.USER_TYPE_STUDENT) { // 부모, 원생의 경우
                    if (!TextUtils.isEmpty(item.state)) {
                        if (item.state.equals(Constants.QNA_STATE_SUBSCRIPTION)) {
                            addTag(holder.layoutStatus, R.color.color_subscription, "신청");

                        } else if(item.state.equals(Constants.QNA_STATE_RECEPTION)) {
                            addTag(holder.layoutStatus, R.color.color_receiption, "접수");

                        } else if(item.state.equals(Constants.QNA_STATE_COMPLETE)) {
                            addTag(holder.layoutStatus, R.color.color_complete, "완료");
                        }
                    }
                }

                if (!TextUtils.isEmpty(item.isOpen)) {
                    if (!item.isOpen.equals("Y")) {
                        addTag(holder.layoutStatus, R.color.color_private, "비공개");
                    }
                }

                if (item.writerSeq == PreferenceUtil.getUserSeq(mContext)) {
                    addTag(holder.layoutStatus, R.color.color_me, "본인");
                }
            }
        }else{
            holder.qnaRoot.setBackgroundColor(mContext.getColor(R.color.white));
        }

        holder.tvTitle.setText(Utils.getStr(item.title));

        if (!TextUtils.isEmpty(item.acaName)) str = item.acaName;
        if (!TextUtils.isEmpty(item.acaGubunName)) {
            if (!TextUtils.isEmpty(item.acaName)) str += " / " + item.acaGubunName;
            else str += item.acaGubunName;
        }
        holder.tvCampus.setText(str);
        if(item.userGubun >= Constants.USER_TYPE_STUDENT) {
            holder.tvWriter.setVisibility(View.VISIBLE);
            holder.tvWriter.setText(item.writerNm);
        }else{
            holder.tvWriter.setVisibility(View.GONE);
        }
        holder.tvDate.setText(Utils.getStr(item.insertDate));

        try { holder.tvRdCnt.setText(Utils.decimalFormat(item.rdcnt)); }
        catch (Exception e) {}

    }

    private void addTag(LinearLayout layout, int bgColor, String state) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        int marginEnd = Utils.fromDpToPx(4);
        int paddingTop = Utils.fromDpToPx(1);
        int paddingBottom = Utils.fromDpToPx(2);
        int paddingHorizontal = Utils.fromDpToPx(8);

        TextView tvStatus = new TextView(mContext);
        tvStatus.setText(state);
        //tvStatus.setHint("비공개");
        tvStatus.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_badge_default));
        ViewCompat.setBackgroundTintList(tvStatus, ColorStateList.valueOf(ContextCompat.getColor(mContext, bgColor)));
        tvStatus.setTextAppearance(R.style.QnaTagTextAppearance);
        tvStatus.setGravity(Gravity.CENTER);

        tvStatus.setPadding(paddingHorizontal, paddingTop, paddingHorizontal, paddingBottom);

        layoutParams.rightMargin = marginEnd;
        tvStatus.setLayoutParams(layoutParams);

        layout.addView(tvStatus);
    }


    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ConstraintLayout qnaRoot;
        private LinearLayout layoutStatus;
        private ImageView imgRdCnt;
        private TextView tvTitle, tvCampus, tvWriter, tvDate, tvRdCnt;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            qnaRoot = itemView.findViewById(R.id.qna_root);
            layoutStatus = itemView.findViewById(R.id.layout_status);
            tvTitle = itemView.findViewById(R.id.tv_qna_title);
            tvCampus = itemView.findViewById(R.id.tv_qna_campus);
            tvWriter = itemView.findViewById(R.id.tv_qna_writer);
            tvDate = itemView.findViewById(R.id.tv_qna_date);
            tvRdCnt = itemView.findViewById(R.id.tv_qna_rd_cnt);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick(mList.get(position), position);
            });
        }
    }
}
