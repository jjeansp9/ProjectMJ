package kr.jeet.edu.manager.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.model.data.AttachFileData;

//public class AttachFileListAdapter {
//}
public class AttachFileListAdapter extends RecyclerView.Adapter<AttachFileListAdapter.ViewHolder> {
    public enum Action{Delete, Download};
    public interface onItemClickListener{
        public void onItemClick(AttachFileData attachFile);
        public void onActionBtnClick(int position, Action action);
    }
    private Context mContext;
    private List<AttachFileData> mList;
    private onItemClickListener _listener;
    private Action _action = Action.Delete;
    public AttachFileListAdapter(Context mContext, List<AttachFileData> mList, Action action, onItemClickListener listener) {
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
            AttachFileData item = mList.get(position);
//            holder.tvFileName.setText(Utils.getFileName(item.filePath));
            if(item.mimeType.startsWith("application")){
                holder.ivFileIcon.setImageResource(R.drawable.ic_vector_application_file);
            }else if(item.mimeType.startsWith("text")) {
                holder.ivFileIcon.setImageResource(R.drawable.ic_vector_text_file);
            }
            holder.tvFileName.setText(item.fileName);
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
        private ImageView ivAction;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            rootView = itemView.findViewById(R.id.root);
            ivFileIcon = itemView.findViewById(R.id.iv_icon);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
            ivAction = itemView.findViewById(R.id.iv_action);

            rootView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                _listener.onItemClick(mList.get(position));
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
            ivAction.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                _listener.onActionBtnClick(position, _action);
            });
        }
    }
}