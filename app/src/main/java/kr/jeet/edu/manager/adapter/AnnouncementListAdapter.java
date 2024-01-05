package kr.jeet.edu.manager.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.model.data.ACAData;
import kr.jeet.edu.manager.model.data.AnnouncementData;
import kr.jeet.edu.manager.model.data.FileData;
import kr.jeet.edu.manager.model.data.ReadData;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.utils.FileUtils;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.DrawableAlwaysCrossFadeFactory;


public class AnnouncementListAdapter extends RecyclerView.Adapter<AnnouncementListAdapter.ViewHolder> {
    private static final String TAG = "announceListAdapter";
    public enum ViewMode{
        SUMMARY,
        NORMAL
    }

    public interface onItemClickListener{
        public void onItemClick(AnnouncementData item, int position);
    }
    private Context mContext;
    private List<ReadData> mList;
    private onItemClickListener _listener;
//    private String selectedACACode = "";
    private ArrayMap<String, ACAData> ACAArrayMap;
    ViewMode _currentViewMode = ViewMode.NORMAL;
    int _userGubun = 0;
    public AnnouncementListAdapter(Context mContext, List<ReadData> mList, onItemClickListener listener) {
        this.mContext = mContext;
        this.mList = mList;
        this._listener = listener;
        ACAArrayMap = DataManager.getInstance().getACAListMap();
        _userGubun = PreferenceUtil.getUserGubun(mContext);
    }
    public void setViewMode(ViewMode mode) {
        _currentViewMode = mode;
    }
//    public void setSelectedACACode(String code) {
//        this.selectedACACode = code;
//    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_announcement_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position == NO_POSITION) return;
        AnnouncementData item = (AnnouncementData) mList.get(position);

        if (item.fileList != null && item.fileList.size() > 0){

            //String url = RetrofitApi.SERVER_BASE_URL + item.fileList.get(0).path + "/" + item.fileList.get(0).saveName;
            boolean isContainImage = false;
            for(FileData data : item.fileList) {
                if(FileUtils.getMimeTypeFromExtension(data.extension).startsWith("image")) {
                    isContainImage = true;
                    String url = RetrofitApi.FILE_SUFFIX_URL + data.path + "/" + data.saveName;
                    url = FileUtils.replaceMultipleSlashes(url);
                    Glide.with(mContext)
                            .load(url)
                            .thumbnail(0.2f)
                            .centerCrop()
//                            .error(R.drawable.ic_vector_image_error)
//                    .placeholder(R.drawable.ic_vector_image_placeholder)
                            .transition(DrawableTransitionOptions.with(new DrawableAlwaysCrossFadeFactory()))
                            .into(holder.imgAnnouncement);
                    LogMgr.e("UrlTest", url);
                    break;
                }else{
                    continue;
                }
            }
            if(!isContainImage) {
                Glide.with(mContext).clear(holder.imgAnnouncement);
                holder.imgAnnouncement.setVisibility(View.GONE);
            }else{
                holder.imgAnnouncement.setVisibility(View.VISIBLE);
            }

        }
        else {
            Glide.with(mContext).clear(holder.imgAnnouncement);
            holder.imgAnnouncement.setVisibility(View.GONE);
        }
//        holder.tvAnnouncementDate.setVisibility(View.VISIBLE);

        holder.tvTitle.setText(TextUtils.isEmpty(item.title) ? "" : item.title);
        if(_currentViewMode == ViewMode.NORMAL){
            LogMgr.e(TAG, "isRead: " + item.isRead);
            if (!item.isRead) { // 읽지 않은 게시글
                holder.layoutRoot.setBackgroundColor(mContext.getColor(R.color.bg_is_read));
            } else {
                holder.layoutRoot.setBackgroundColor(Color.TRANSPARENT);
            }
            holder.tvName.setVisibility(View.VISIBLE);
            holder.tvName.setText(TextUtils.isEmpty(item.memberResponseVO.name) ? "" : item.memberResponseVO.name);
            holder.tvCampus.setVisibility(View.VISIBLE);
            if(!TextUtils.isEmpty(item.acaCode)) {
                if(TextUtils.isEmpty(item.acaGubunName)) {
                    holder.tvCampus.setText(item.acaName);
                }else{
                    holder.tvCampus.setText(item.acaName + " / " + item.acaGubunName);
                }
            }
        }else{
            holder.layoutRoot.setBackgroundColor(Color.TRANSPARENT);
            holder.tvName.setVisibility(View.GONE);
            if(_userGubun <= Constants.USER_TYPE_ADMIN) {
                holder.tvCampus.setVisibility(View.VISIBLE);
            }else{
                //holder.tvCampus.setVisibility(View.GONE);
                holder.tvCampus.setVisibility(View.VISIBLE);
            }
            if(!TextUtils.isEmpty(item.acaCode)) {
                if(TextUtils.isEmpty(item.acaGubunName)) {
                    holder.tvCampus.setText(item.acaName);
                }else{
                    holder.tvCampus.setText(item.acaName + " / " + item.acaGubunName);
                }
            }
        }
        LogMgr.w("test", "insertDate=" + item.insertDate);
        holder.tvAnnouncementDate.setText(TextUtils.isEmpty(item.insertDate) ? "" : item.insertDate);
//        holder.imgRead.setVisibility(View.VISIBLE);
//        holder.tvReadCount.setVisibility(View.VISIBLE);
        holder.tvReadCount.setText(Utils.decimalFormat(item.rdcnt));

        //컨셉변경 : 전체일때만 캠퍼스 정보 보여주기-> 캠퍼스 정보 무조건 보여주기
//        if(TextUtils.isEmpty(selectedACACode)){
//            if(DataManager.getInstance().getACAListMap() != null) {
//                holder.tvCampus.setText(DataManager.getInstance().getACAListMap().get(item.acaCode).acaName);
//            }
//        }else{
//            holder.tvCampus.setText("");
//        }

    }

    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ConstraintLayout layoutRoot;
        private Guideline guideline;
        private ImageView imgAnnouncement, imgRead;
        private TextView tvDate, tvTitle, tvName, tvState, tvCampus, tvAnnouncementDate, tvReadCount;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            layoutRoot = itemView.findViewById(R.id.brf_root);
            tvDate = itemView.findViewById(R.id.tv_brf_date);
            tvTitle = itemView.findViewById(R.id.tv_brf_title);
            tvName = itemView.findViewById(R.id.tv_writer_name);
//            tvState = itemView.findViewById(R.id.tv_brf_state);
            tvCampus = itemView.findViewById(R.id.tv_brf_campus);
            imgAnnouncement = itemView.findViewById(R.id.img_brf);
            guideline = itemView.findViewById(R.id.guideline);
            tvAnnouncementDate = itemView.findViewById(R.id.tv_announcement_date);
            imgRead = itemView.findViewById(R.id.img_rd_cnt);
            tvReadCount = itemView.findViewById(R.id.tv_rd_cnt);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != NO_POSITION) if (mList.size() > 0) _listener.onItemClick((AnnouncementData) mList.get(position), position);
            });
        }
    }
}
