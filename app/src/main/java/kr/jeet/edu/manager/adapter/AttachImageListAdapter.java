package kr.jeet.edu.manager.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.model.data.AttachFileData;
import kr.jeet.edu.manager.view.DrawableAlwaysCrossFadeFactory;

public class AttachImageListAdapter extends RecyclerView.Adapter<AttachImageListAdapter.ViewHolder> {

    public interface onItemClickListener{
        public void onItemClick(AttachFileData attachFile);
        public void onDeleteClick(int position);
    }
    private Context mContext;
    private List<AttachFileData> mList;
    private onItemClickListener _listener;

    public AttachImageListAdapter(Context mContext, List<AttachFileData> mList, onItemClickListener listener) {
        this.mContext = mContext;
        this.mList = mList;
        this._listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_image_pallet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position != NO_POSITION) {
            AttachFileData item = mList.get(position);
            Glide.with(mContext)
                    .load(item.uri)
                    .thumbnail(0.2f)
                    .centerCrop()
//                    .error(R.drawable.ic_vector_image_error)
                    .transition(DrawableTransitionOptions.with(new DrawableAlwaysCrossFadeFactory()))
                    .into(holder.ivAttachImage);
        }
    }

    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView ivAttachImage, ivAction;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            ivAttachImage = itemView.findViewById(R.id.iv_attach_image);
            ivAction = itemView.findViewById(R.id.iv_action);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                _listener.onItemClick(mList.get(position));
            });
            if(ivAction != null) {
                ivAction.setOnClickListener(v -> {
                    int position = getBindingAdapterPosition();
                    _listener.onDeleteClick(position);
                });
            }
        }
    }
}