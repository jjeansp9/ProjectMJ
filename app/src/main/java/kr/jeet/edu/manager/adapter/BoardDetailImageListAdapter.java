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

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.model.data.FileData;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.utils.FileUtils;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.view.DrawableAlwaysCrossFadeFactory;

public class BoardDetailImageListAdapter extends RecyclerView.Adapter<BoardDetailImageListAdapter.ViewHolder> {

    public interface ItemClickListener{
        public void onItemClick(List<FileData> item, int position);
    }

    private Context mContext;
    private List<FileData> mList;
    private ItemClickListener _listener;

    public BoardDetailImageListAdapter(Context mContext, List<FileData> mList, ItemClickListener _listener) {
        this.mContext = mContext;
        this.mList = mList;
        this._listener = _listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_board_detail_img_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position != NO_POSITION) {
            FileData item = mList.get(position);

            if (item != null && mList.size() > 0) {
                //String url = RetrofitApi.SERVER_BASE_URL + item.fileList.get(0).path + "/" + item.fileList.get(0).saveName;
//                String url = RetrofitApi.FILE_SUFFIX_URL + "notice/" + item.get(position).saveName;
                String url = RetrofitApi.FILE_SUFFIX_URL + item.path + "/" + item.saveName;
                url = FileUtils.replaceMultipleSlashes(url);
                LogMgr.i("urlTest ", url);

                Glide.with(mContext)
                        .load(url)
                        .thumbnail(0.2f)
//                        .error(R.drawable.ic_vector_image_error)
                        .transition(DrawableTransitionOptions.with(new DrawableAlwaysCrossFadeFactory()))
                        .into(holder.imgBoardDetail);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mList == null) return 0;
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imgBoardDetail;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            imgBoardDetail = itemView.findViewById(R.id.img_board_detail);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                _listener.onItemClick(mList, position);
            });
        }
    }
}
