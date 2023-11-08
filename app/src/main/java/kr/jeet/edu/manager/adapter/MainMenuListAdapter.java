package kr.jeet.edu.manager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.model.data.MainMenuItemData;
import kr.jeet.edu.manager.view.DrawableAlwaysCrossFadeFactory;

public class MainMenuListAdapter extends RecyclerView.Adapter<MainMenuListAdapter.ViewHolder> {

    public interface onItemClickListener{
        public void onItemClick(MainMenuItemData item);
    }
    private Context mContext;
    private List<MainMenuItemData> _menuList;
    private onItemClickListener _listener;
//    private boolean _isMemberLoggedIn = false;

    public MainMenuListAdapter(Context mContext, List<MainMenuItemData> mList, onItemClickListener listener){
        this.mContext = mContext;
        this._menuList = mList;
        this._listener = listener;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_main_menu_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainMenuItemData item = _menuList.get(position);
//        if(!_isMemberLoggedIn && item.isMemberOnly()) { // 비회원으로 로그인하고 회원용 메뉴인 경우는 표시하지 않음
//            holder.rootLayout.setVisibility(View.GONE);
//        }else {
//            holder.rootLayout.setVisibility(View.VISIBLE);
            Glide.with(mContext)
                    .load(item.getImgRes())
                    .into(holder.imgMenu);
            holder.tvMenu.setText(item.getTitleRes());
//        }
    }

    @Override
    public int getItemCount() {
        if (_menuList == null) return 0;
        return _menuList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ConstraintLayout rootLayout;
        private ImageView imgMenu;
        private TextView tvMenu;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            rootLayout = itemView.findViewById(R.id.root);
            imgMenu = itemView.findViewById(R.id.img_menu);
            tvMenu = itemView.findViewById(R.id.tv_menu);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                _listener.onItemClick(_menuList.get(position));
            });
        }
    }
}
