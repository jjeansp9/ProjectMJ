package kr.jeet.edu.manager.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerInterface;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.manager.R;

public class WrapContentSpinnerAdapter extends RecyclerView.Adapter<WrapContentSpinnerAdapter.CustomSpinnerViewHolder> implements PowerSpinnerInterface<String> {
    private static final int NO_SELECTED_INDEX = -1;
    private int index = NO_SELECTED_INDEX;
    private PowerSpinnerView spinnerView;
    private OnSpinnerItemSelectedListener<String> onSpinnerItemSelectedListener;
    private List<String> spinnerItems = new ArrayList<>();
    private String selectedItem = "";
    private Context _context;
    public WrapContentSpinnerAdapter(Context mContext, List<String> mList, PowerSpinnerView spinnerView){
        this._context = mContext;
        this.spinnerItems = mList;
        this.spinnerView = spinnerView;

    }
    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public PowerSpinnerView getSpinnerView() {
        return spinnerView;
    }

    public void setSpinnerView(PowerSpinnerView powerSpinnerView) {
        this.spinnerView = powerSpinnerView;
    }

    @Override
    public OnSpinnerItemSelectedListener<String> getOnSpinnerItemSelectedListener() {
        return onSpinnerItemSelectedListener;
    }

    @Override
    public void setOnSpinnerItemSelectedListener(OnSpinnerItemSelectedListener<String> onSpinnerItemSelectedListener) {
        this.onSpinnerItemSelectedListener = onSpinnerItemSelectedListener;
    }

    @NonNull
    @Override
    public CustomSpinnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        PowerspinnerItemDefaultPowerBinding binding = PowerspinnerItemDefaultPowerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
//        return new DefaultSpinnerViewHolder(binding);
        View view = LayoutInflater.from(_context).inflate(R.layout.layout_custom_spinner_dropdown_item, parent, false);
        return new CustomSpinnerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomSpinnerViewHolder holder, @SuppressLint("RecyclerView") int position) {
//        holder.bind(spinnerView, spinnerItems.get(position), index == position);
        String string = spinnerItems.get(position);
        holder.tvItem.setText(string);
        if(index == position) { //selected
            holder.rootView.setBackgroundColor(_context.getColor(R.color.color_spinner_selected_item));
        }else{
            holder.rootView.setBackgroundColor(_context.getColor(R.color.white));
        }
    }

    @Override
    public void notifyItemSelected(int index) {
        if (index == NO_SELECTED_INDEX) return;
        int oldIndex = this.index;
        this.index = index;
        this.spinnerView.notifyItemSelected(index, spinnerItems.get(index));
        notifyDataSetChanged();
        if (onSpinnerItemSelectedListener != null) {
            String oldItem = oldIndex != NO_SELECTED_INDEX ? spinnerItems.get(oldIndex) : null;
            String newItem = spinnerItems.get(index);
            onSpinnerItemSelectedListener.onItemSelected(oldIndex, oldItem, index, newItem);
        }
    }

    @Override
    public int getItemCount() {
        return spinnerItems.size();
    }

    @Override
    public void setItems(@NonNull List<? extends String> list) {
        this.spinnerItems.clear();
        this.spinnerItems.addAll(list);
        this.index = NO_SELECTED_INDEX;
        notifyDataSetChanged();
    }

    public class CustomSpinnerViewHolder extends RecyclerView.ViewHolder {
        //        private final PowerspinnerItemDefaultPowerBinding binding;
        private LinearLayout rootView;
        private TextView tvItem;
        public CustomSpinnerViewHolder(View itemView) {
            super(itemView);
            this.rootView = itemView.findViewById(R.id.root_view);
            this.tvItem = itemView.findViewById(R.id.tv_item);
            this.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getBindingAdapterPosition();
                    notifyItemSelected(position);
                }
            });
        }
    }
}
