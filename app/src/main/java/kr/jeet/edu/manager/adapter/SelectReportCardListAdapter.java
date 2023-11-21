package kr.jeet.edu.manager.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.model.data.ReportCardData;
import kr.jeet.edu.manager.utils.LogMgr;

public class SelectReportCardListAdapter extends RecyclerView.Adapter<SelectReportCardListAdapter.ViewHolder>{
    private static final String TAG = "reportcardListAdapter";
    public interface ItemClickListener {
        void onItemClick(int position, ReportCardData item);
    }
    SimpleDateFormat millisecFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm_ss_SSS);
    SimpleDateFormat minuteFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD_HH_mm);
    Constants.ReportCardListType _listType = Constants.ReportCardListType.CHECK;
    private Context _context;
    private List<ReportCardData> _list;
    private ItemClickListener _listener;

    public SelectReportCardListAdapter(Context context, List<ReportCardData> list, Constants.ReportCardListType type,  ItemClickListener listener) {
        this._context = context;
        this._list = list;
        this._listType = type;
        this._listener = listener;
    }

    @NonNull
    @Override
    public SelectReportCardListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(_context).inflate(R.layout.layout_student_report_card_list_item, parent, false);
        return new SelectReportCardListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectReportCardListAdapter.ViewHolder holder, int position) {
        if(position == NO_POSITION) return;
        ReportCardData item = _list.get(position);
        holder.tvTitle.setText(item.etName);
        holder.tvGubun.setText(item.etTitleGubunName);
        Drawable gubunDrawable = _context.getResources().getDrawable(R.drawable.bg_layout_manager, null);
        Constants.ReportCardType type = Constants.ReportCardType.getByName(item.etTitleGubunName);
        gubunDrawable.setTint(_context.getResources().getColor(type.getColorRes(), null));
        holder.tvGubun.setBackground(gubunDrawable);
        holder.tvGrade.setText(item.etGubun);
        String dateString = item.regDate;
        try {
            Date regDate = millisecFormat.parse(item.regDate);
            dateString = minuteFormat.format(regDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.tvDate.setText(dateString);
        switch(_listType) {
            case VIEW:
                holder.layoutRoot.setBackgroundColor(_context.getColor(R.color.white));
                break;
            case CHECK:
                holder.cbSelect.setChecked(item.isSelected);
                if("Y".equals(item.msgYn)) {
                    holder.layoutRoot.setBackgroundColor(_context.getColor(R.color.bg_gray));
                }else{
                    holder.layoutRoot.setBackgroundColor(_context.getColor(R.color.white));
                }
                break;
            case SELECT_FORM_TYPE:
                holder.layoutRoot.setBackgroundColor(_context.getColor(R.color.white));
                if(type != null) {
                    Constants.ReportCardType spinnerType = Constants.ReportCardType.getByCode(item.etTitleGubun);
                    int index = Arrays.binarySearch(Constants.ReportCardType.values(), spinnerType);
                    LogMgr.w(TAG, "spinnertype = " + type.getNameKor() + " /index = " + index);
                    holder.spinnerType.selectItemByIndex(index);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        if(_list == null) return 0;
        return _list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        private ConstraintLayout layoutRoot;
        private TextView tvGubun, tvTitle, tvGrade, tvDate;
        private MaterialCheckBox cbSelect;
        private PowerSpinnerView spinnerType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutRoot = itemView.findViewById(R.id.root);
            tvGubun = itemView.findViewById(R.id.tv_report_card_state);
            tvGrade = itemView.findViewById(R.id.tv_grade);
            tvTitle = itemView.findViewById(R.id.tv_report_card_title);
            tvDate = itemView.findViewById(R.id.tv_report_card_date);
            cbSelect = itemView.findViewById(R.id.cb_select);
            spinnerType = itemView.findViewById(R.id.spinner_type);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                ReportCardData item = _list.get(position);
                if (_list.size() > 0) _listener.onItemClick(position, item);
            });
            switch(_listType){
                case VIEW:
                    layoutRoot.setClickable(true);
                    cbSelect.setVisibility(View.GONE);
                    spinnerType.setVisibility(View.GONE);
                    break;
                case CHECK:
                    layoutRoot.setClickable(false);
//                    tvGubun.setVisibility(View.VISIBLE);
//                    tvGrade.setVisibility(View.VISIBLE);
                    cbSelect.setVisibility(View.VISIBLE);
                    spinnerType.setVisibility(View.GONE);
                    cbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            int position = getBindingAdapterPosition();
                            ReportCardData item = _list.get(position);
                            item.isSelected = isChecked;
                        }
                    });
                    break;
                case SELECT_FORM_TYPE:
                    layoutRoot.setClickable(false);
//                    tvGubun.setVisibility(View.GONE);
//                    tvGrade.setVisibility(View.GONE);
                    cbSelect.setVisibility(View.GONE);
                    spinnerType.setVisibility(View.VISIBLE);
                    spinnerType.setIsFocusable(true);
//        _spinnerReportCardMultiple.setItems(Constants.ReportCardType.getNameList());
                    ColoredSpinnerAdapter adapter = new ColoredSpinnerAdapter(_context, Constants.ReportCardType.getColoredNameList(_context), spinnerType);
                    spinnerType.setSpinnerAdapter(adapter);
//                    WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(_context, Constants.ReportCardType.getNameList(), spinnerType);
//                    spinnerType.setSpinnerAdapter(adapter);
                    spinnerType.setSpinnerPopupHeight(ConstraintLayout.LayoutParams.WRAP_CONTENT);
                    spinnerType.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<SpannableString>() {
                        @Override
                        public void onItemSelected(int oldIndex, @Nullable SpannableString oldItem, int newIndex, SpannableString newItem) {
                            LogMgr.w(TAG, "newIndex = " + newIndex + " / newItem = " + newItem);
                            if(newItem.equals(oldItem)) return;
                            Constants.ReportCardType selectedType = Constants.ReportCardType.values()[newIndex];
                            int position = getBindingAdapterPosition();
                            ReportCardData item = _list.get(position);
                            item.etTitleGubun = selectedType.getCode();
//                            notifyItemChanged(position);
                        }
                    });
                    spinnerType.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
                        @Override
                        public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                            spinnerType.dismiss();
                        }
                    });
                    break;
            }

        }
    }
}
