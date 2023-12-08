package kr.jeet.edu.manager.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

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
import kr.jeet.edu.manager.model.data.AnswererData;

public class AnswererChipListAdapter extends RecyclerView.Adapter<AnswererChipListAdapter.ViewHolder> {
    private Context _context;
    List<AnswererData> _list;
    public AnswererChipListAdapter(Context context, List<AnswererData> list) {
        this._context = context;
        this._list = list;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(_context).inflate(R.layout.layout_chip_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position == NO_POSITION) return;
        AnswererData item = _list.get(position);
        if(item.isWrited) {
            holder.chip.setChipIconVisible(true);
            Drawable drawable = _context.getDrawable(R.drawable.ic_vector_chat);
            drawable.setTint(_context.getColor(R.color.blue_sub));
            holder.chip.setChipIcon(drawable);
        }else{
            holder.chip.setChipIconVisible(true);
            Drawable drawable = _context.getDrawable(R.drawable.ic_vector_chat);
            drawable.setTint(_context.getColor(R.color.bg_gray));
            holder.chip.setChipIcon(drawable);
        }
        holder.chip.setText(item.sfName);
    }

    @Override
    public int getItemCount() {
        if(_list == null) return 0;
        return _list.size();
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
            chip.setCloseIconVisible(false);


        }
    }
}
