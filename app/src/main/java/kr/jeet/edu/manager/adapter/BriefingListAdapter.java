package kr.jeet.edu.manager.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.model.data.BriefingData;
import kr.jeet.edu.manager.model.data.FileData;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.utils.FileUtils;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.Utils;

public class BriefingListAdapter extends RecyclerView.Adapter<BriefingListAdapter.ViewHolder> {

    private final static String TAG = "BrfListAdapter";

    public interface ItemClickListener{ public void onItemClick(BriefingData item, int position); }

    private Context mContext;
    private List<BriefingData> mList;
    private ItemClickListener _listener;
    /**
     * 전체 선택 시에는 캠퍼스 정보를 숨김 -> 모두 보임
     */
//    private static boolean isWholeCampusMode = false;
    private final boolean IMG_IS_EMPTY = true;
    private final boolean IMG_IS_NOT_EMPTY = false;

    public BriefingListAdapter(Context mContext, List<BriefingData> mList, ItemClickListener listener) {
        this.mContext = mContext;
        this._listener = listener;
        this.mList = mList;
    }
//    public void setWholeCampusMode(boolean flag) {
//        this.isWholeCampusMode = flag;
//    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_briefing_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BriefingData item = mList.get(position);
        try{
            holder.tvDate.setText(TextUtils.isEmpty(item.date) || TextUtils.isEmpty(item.ptTime) ? "" : Utils.formatDate(item.date, item.ptTime, false));
            holder.tvTitle.setText(TextUtils.isEmpty(item.title) ? "" : item.title);
            holder.tvLocation.setText(TextUtils.isEmpty(item.place) ? "" : item.place);

//            if (isWholeCampusMode) {
//                holder.tvCampus.setVisibility(View.VISIBLE);
            if(!TextUtils.isEmpty(item.acaCode)) {
                if(TextUtils.isEmpty(item.acaGubunName)) {
                    holder.tvCampus.setText(item.acaName);
                }else{
                    holder.tvCampus.setText(item.acaName + " / " + item.acaGubunName);
                }
            }
//            } else {
//                holder.tvCampus.setVisibility(View.GONE);
//            }
            String dateStr = item.date+item.ptTime;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm", Locale.KOREA);

            Date date = sdf.parse(dateStr);
            Calendar calBrf = Calendar.getInstance();
            if (date != null) calBrf.setTime(date);
            Calendar calCurrent = Calendar.getInstance();

            String cnt;
            if (calCurrent.after(calBrf) || calCurrent.equals(calBrf)) {
                cnt = "종료";
                holder.tvState.setBackground(mContext.getDrawable(R.drawable.bg_layout_parent));
            }
            else if (item.reservationCnt >= item.participantsCnt) {
                holder.tvState.setBackground(mContext.getDrawable(R.drawable.bg_layout_darkgray));
                cnt = "마감";
            }
            else {
                holder.tvState.setBackground(mContext.getDrawable(R.drawable.bg_layout_manager));
                cnt = "예약";
            }

            holder.tvState.setText(cnt);
            holder.tvReadCount.setText(Utils.decimalFormat(item.rdcnt));

            if (item.fileList != null && item.fileList.size() > 0){

                boolean isContainImage = false;

                for(FileData data : item.fileList) {

                    String mimeType = FileUtils.getMimeTypeFromExtension(data.extension);

                    if (mimeType != null && mimeType.startsWith("image")){
                        isContainImage = true;
                        String url = RetrofitApi.FILE_SUFFIX_URL + data.path + "/" + data.saveName;
                        url = FileUtils.replaceMultipleSlashes(url);

                        FileUtils.loadImage(mContext, url, holder.imgBrf);
                        LogMgr.e(TAG+" UrlTest", url);
                        break;
                    }else{
                        continue;
                    }
                }
                if(!isContainImage) {
                    Glide.with(mContext).clear(holder.imgBrf);
                    holder.imgBrf.setVisibility(View.INVISIBLE);
                    setView(holder.tvTitle, IMG_IS_EMPTY);

                }else{
                    holder.imgBrf.setVisibility(View.VISIBLE);
                    setView(holder.tvTitle, IMG_IS_NOT_EMPTY);
                }

            }
            else {
                Glide.with(mContext).clear(holder.imgBrf);
                holder.imgBrf.setVisibility(View.INVISIBLE);
                setView(holder.tvTitle, IMG_IS_EMPTY);
            }

        }catch (Exception e){
            LogMgr.e("ListAdapter Exception : " + e.getMessage());
        }
    }

    private void setView(TextView tvTitle, boolean set){
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) tvTitle.getLayoutParams();

        if (set){
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            layoutParams.endToStart = ConstraintLayout.LayoutParams.UNSET;
            //tvSub.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

        }else{
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET;
            layoutParams.endToStart = R.id.img_brf;
            //tvSub.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        }
        tvTitle.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ConstraintLayout brfRoot;
        private ImageView imgBrf, imgRead;
        private TextView tvDate, tvTitle, tvLocation, tvState, tvCampus, tvReadCount;;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            brfRoot = itemView.findViewById(R.id.brf_root);
            tvDate = itemView.findViewById(R.id.tv_brf_date);
            tvTitle = itemView.findViewById(R.id.tv_brf_title);
            tvLocation = itemView.findViewById(R.id.tv_brf_location);
            tvState = itemView.findViewById(R.id.tv_brf_state);
            tvCampus = itemView.findViewById(R.id.tv_brf_campus);
            imgBrf = itemView.findViewById(R.id.img_brf);
            imgRead = itemView.findViewById(R.id.img_rd_cnt);
            tvReadCount = itemView.findViewById(R.id.tv_rd_cnt);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                _listener.onItemClick(mList.get(position), position);
            });
        }
    }
}

