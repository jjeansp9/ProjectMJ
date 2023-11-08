package kr.jeet.edu.manager.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.model.data.AttachFileData;
import kr.jeet.edu.manager.model.data.FileData;
import kr.jeet.edu.manager.utils.FileUtils;
import kr.jeet.edu.manager.utils.LogMgr;

//public class BoardDetailFileListAdapter {
//}
public class BoardDetailFileListAdapter extends RecyclerView.Adapter<BoardDetailFileListAdapter.ViewHolder> {
    private static final String TAG = "fileAdapter";
    public enum Action{Delete, Download};
    public interface onItemClickListener{
        void onItemClick(int position, FileData file);
        void onActionBtnClick(int position, FileData file, Action action);
    }
    private Context mContext;
    private List<FileData> mList;
    private onItemClickListener _listener;
    private Action _action = Action.Delete;
    public BoardDetailFileListAdapter(Context mContext, List<FileData> mList, Action action, onItemClickListener listener) {
        this.mContext = mContext;
        this.mList = mList;
        this._listener = listener;
        this._action = action;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_file_pallet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position != NO_POSITION) {
            FileData item = mList.get(position);
//            holder.tvFileName.setText(Utils.getFileName(item.filePath));
            String type = item.path.replaceAll("/", "");
            boolean isExistDownloadTempFile = FileUtils.isExistBoardTempFile(mContext, item);
            LogMgr.w(TAG, "path = " + mContext.getExternalFilesDir(type).getPath() + "/" + item.tempFileName + "isExist = " + isExistDownloadTempFile);
            if(isExistDownloadTempFile) {
                holder.ivFileIcon.setImageResource(R.drawable.ic_vector_file_open);
            }else{
                String mimeType = FileUtils.getMimeTypeFromExtension(item.extension);
                if(mimeType.startsWith("application")){
                    holder.ivFileIcon.setImageResource(R.drawable.ic_vector_application_file);
                }else if(mimeType.startsWith("text")) {
                    holder.ivFileIcon.setImageResource(R.drawable.ic_vector_text_file);
                }else{
                    holder.ivFileIcon.setImageResource(R.drawable.ic_vector_application_file);
                }
            }

            holder.tvFileName.setText(item.orgName);
        }
    }

    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ConstraintLayout rootView;
        private ImageView ivFileIcon;
        private TextView tvFileName;
        private LinearLayoutCompat layoutAction;
        private ImageView ivAction;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            rootView = itemView.findViewById(R.id.root);
            ivFileIcon = itemView.findViewById(R.id.iv_icon);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            layoutAction = itemView.findViewById(R.id.layout_action);
            ivAction = itemView.findViewById(R.id.iv_action);

            rootView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if(position != NO_POSITION) {
                    _listener.onItemClick(position, mList.get(position));
                }
            });
            switch(_action) {
                case Delete:
                    rootView.setClickable(false);
                    ivAction.setImageResource(R.drawable.ic_vector_close);
                    break;
                case Download:
                    rootView.setClickable(true);
                    ivAction.setImageResource(R.drawable.ic_vector_download);
                    break;
            }
            layoutAction.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if(position != NO_POSITION) {
                    FileData data = mList.get(position);
                    _listener.onActionBtnClick(position, data, _action);
                }
            });
        }
    }
}
