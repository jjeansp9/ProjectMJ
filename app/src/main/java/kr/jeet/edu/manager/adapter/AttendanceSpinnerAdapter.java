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

//public class AttendanceSpinnerAdapter extends RecyclerView.Adapter<AttendanceSpinnerAdapter.ViewHolder> implements PowerSpinnerInterface<SpannableString> {
//    private static final int NO_SELECTED_INDEX = -1;
//    private int index = NO_SELECTED_INDEX;
//    private PowerSpinnerView spinnerView;
//    private OnSpinnerItemSelectedListener<SpannableString> onSpinnerItemSelectedListener;
//    private List<SpannableString> _itemList = new ArrayList<>();
//    Context _context;
//    public AttendanceSpinnerAdapter(Context context, List<SpannableString> list){
//       this._context = context;
//       this._itemList = list;
//    }
//    @Override
//    public int getIndex() {
//        return index;
//    }
//
//    @Override
//    public void setIndex(int index) {
//        this.index = index;
//    }
//
//    @Override
//    public PowerSpinnerView getSpinnerView() {
//        return spinnerView;
//    }
//
//    @Override
//    public OnSpinnerItemSelectedListener<SpannableString> getOnSpinnerItemSelectedListener() {
//        return onSpinnerItemSelectedListener;
//    }
//
//    @Override
//    public void setOnSpinnerItemSelectedListener(OnSpinnerItemSelectedListener<SpannableString> onSpinnerItemSelectedListener) {
//        this.onSpinnerItemSelectedListener = onSpinnerItemSelectedListener;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_custom_spinner_dropdown_item, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                notifyItemSelected(position);
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        if(_itemList == null) return 0;
//        return _itemList.size(); // Implement the method to return the number of items in the spinner
//    }
//
//    @Override
//    public void setItems(@NonNull List<? extends SpannableString> list) {
//        _itemList = (List<SpannableString>) list;
//    }
//
//    // Your implementation of MySpinnerViewHolder
//    class ViewHolder extends RecyclerView.ViewHolder {
//        // ViewHolder code here...
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            // Initialize views here...
//        }
//    }
//
//    // Your implementation of notifyItemSelected method
//    @Override
//    public void notifyItemSelected(int index) {
//        if (index == NO_SELECTED_INDEX) return;
//        int oldIndex = this.index;
//        this.index = index;
//        this.spinnerView.notifyItemSelected(index, _itemList.get(index));
//        if (onSpinnerItemSelectedListener != null) {
//            SpannableString oldItem = oldIndex != NO_SELECTED_INDEX ? _itemList.get(oldIndex) : null;
//            SpannableString newItem = _itemList.get(index);
//            onSpinnerItemSelectedListener.onItemSelected(oldIndex, oldItem, index, newItem);
//        }
//    }
//}


public class AttendanceSpinnerAdapter extends RecyclerView.Adapter<AttendanceSpinnerAdapter.CustomSpinnerViewHolder> implements PowerSpinnerInterface<SpannableString> {
    private static final int NO_SELECTED_INDEX = -1;
    private int index = NO_SELECTED_INDEX;
    private PowerSpinnerView spinnerView;
    private OnSpinnerItemSelectedListener<SpannableString> onSpinnerItemSelectedListener;
    private List<SpannableString> spinnerItems = new ArrayList<>();
    private Context _context;
    public AttendanceSpinnerAdapter(Context mContext, List<SpannableString> mList, PowerSpinnerView spinnerView){
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
    public OnSpinnerItemSelectedListener<SpannableString> getOnSpinnerItemSelectedListener() {
        return onSpinnerItemSelectedListener;
    }

    @Override
    public void setOnSpinnerItemSelectedListener(OnSpinnerItemSelectedListener<SpannableString> onSpinnerItemSelectedListener) {
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
        SpannableString spanString = spinnerItems.get(position);
        holder.tvItem.setText(spanString);

    }

    @Override
    public void setItems(List<? extends SpannableString> itemList) {
        this.spinnerItems.clear();
        this.spinnerItems.addAll(itemList);
        this.index = NO_SELECTED_INDEX;
        notifyDataSetChanged();
    }

    @Override
    public void notifyItemSelected(int index) {
        if (index == NO_SELECTED_INDEX) return;
        int oldIndex = this.index;
        this.index = index;
        this.spinnerView.notifyItemSelected(index, spinnerItems.get(index));
        notifyDataSetChanged();
        if (onSpinnerItemSelectedListener != null) {
            SpannableString oldItem = oldIndex != NO_SELECTED_INDEX ? spinnerItems.get(oldIndex) : null;
            SpannableString newItem = spinnerItems.get(index);
            onSpinnerItemSelectedListener.onItemSelected(oldIndex, oldItem, index, newItem);
        }
    }

    @Override
    public int getItemCount() {
        return spinnerItems.size();
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