package kr.jeet.edu.manager.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.model.data.RecipientData;

public class RecipientChipListAdapter extends RecyclerView.Adapter<RecipientChipListAdapter.ViewHolder> {
    public interface onItemClickListener{
//        void onItemClick(RecipientData item);
        void onDeleteClick(int position, RecipientData item);
    }
//    public interface longClickListener{
//        void onLongClick(int position, RecipientData item, Chip chip, ConstraintLayout root);
//    }
    private Context mContext;
    private List<RecipientData> _recipientList;
    private onItemClickListener _listener;
    //private longClickListener _listenerLong;
    Constants.BoardEditMode _editMode = Constants.BoardEditMode.New;
//    private boolean _isMemberLoggedIn = false;

    public RecipientChipListAdapter(Context mContext, List<RecipientData> mList, onItemClickListener listener){
        this.mContext = mContext;
        this._recipientList = mList;
        this._listener = listener;
    }
//    public RecipientChipListAdapter(Context mContext, List<RecipientData> mList, longClickListener listenerLong){
//        this.mContext = mContext;
//        this._recipientList = mList;
//        this._listenerLong = listenerLong;
//    }
    public void setEditMode(Constants.BoardEditMode editMode) {
        this._editMode = editMode;
    }

    @NonNull
    @Override
    public RecipientChipListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_chip_item, parent, false);
        return new RecipientChipListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipientChipListAdapter.ViewHolder holder, int position) {
        RecipientData item = _recipientList.get(position);
//        if(!_isMemberLoggedIn && item.isMemberOnly()) { // 비회원으로 로그인하고 회원용 메뉴인 경우는 표시하지 않음
//            holder.rootLayout.setVisibility(View.GONE);
//        }else {
//            holder.rootLayout.setVisibility(View.VISIBLE);
        if(_editMode.equals(Constants.BoardEditMode.New)) {
            if(TextUtils.isEmpty(item.isApp)) {
                //do nothing;;
            }else if ("Y".equals(item.isApp)) {
                holder.chip.setChipIcon(mContext.getDrawable(R.drawable.icon_app_have));
            } else {
                holder.chip.setChipIcon(mContext.getDrawable(R.drawable.icon_app_haveno));
            }
        }else{
            if(TextUtils.isEmpty(item.isRead)) {
                //do nothing;;
            }
            else if (item.isRead.equals("Y")) {
                Drawable drawable = mContext.getDrawable(R.drawable.ic_vector_show);
                drawable.setTint(mContext.getColor(R.color.blue_sub));
                holder.chip.setChipIcon(drawable);

            } else if (item.isRead.equals("N")){
                Drawable drawable = mContext.getDrawable(R.drawable.ic_vector_show);
                drawable.setTint(mContext.getColor(R.color.bg_gray));
                holder.chip.setChipIcon(drawable);
            }
        }
        if (item.userGubun == Constants.USER_TYPE_STUDENT) {
            holder.chip.setText(item.stName);
        } else {
            holder.chip.setText(String.format("%s 학부모", item.stName));
        }
//        }
    }

    @Override
    public int getItemCount() {
        if (_recipientList == null) return 0;
        return _recipientList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ConstraintLayout rootLayout;
        private Chip chip;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            rootLayout = itemView.findViewById(R.id.root);
            chip = itemView.findViewById(R.id.chip);
            chip.setCheckable(true);
//            chip.setChecked(true);
//            chip.setCheckedIconVisible(true);
            chip.setTextStartPadding(4f);
            chip.setTextEndPadding(4f);
            chip.setClickable(false);
            chip.setFocusable(false);
            if(_editMode.equals(Constants.BoardEditMode.New)){
                chip.setCloseIconVisible(true);
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = getBindingAdapterPosition();
                        _listener.onDeleteClick(position, _recipientList.get(position));
                    }
                });
            }else{
                chip.setCloseIconVisible(false);
            }

//            if(_editMode.equals(Constants.BoardEditMode.Show)) {
//                chip.setOnLongClickListener(v -> {
//                    int position = getBindingAdapterPosition();
//                    if (_listenerLong != null) {
//                        _listenerLong.onLongClick(position, _recipientList.get(position), chip, rootLayout);
//                    }
//                    return true;
//                });
//            }

//            itemView.setOnClickListener(v -> {
//                int position = getBindingAdapterPosition();
//                _listener.onItemClick(_recipientList.get(position));
//            });
        }
    }
}
