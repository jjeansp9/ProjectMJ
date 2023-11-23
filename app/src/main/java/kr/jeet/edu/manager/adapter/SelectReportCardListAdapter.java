package kr.jeet.edu.manager.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
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
import kr.jeet.edu.manager.model.data.RecipientStudentData;
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
    private Drawable disableDrawable;

    public SelectReportCardListAdapter(Context context, List<ReportCardData> list, Constants.ReportCardListType type,  ItemClickListener listener) {
        this._context = context;
        this._list = list;
        this._listType = type;
        this._listener = listener;
        disableDrawable = _context.getDrawable(R.drawable.ic_vector_checkbox_disabled);
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
        if(type != null) {
            gubunDrawable.setTint(_context.getResources().getColor(type.getColorRes(), null));
        }
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
                holder.cbSelect.setBackground(null);
                holder.cbSelect.setEnabled(true);
                break;
            case CHECK:
                holder.cbSelect.setChecked(item.isSelected);
                if("Y".equals(item.msgYn)) {
                    holder.layoutRoot.setBackgroundColor(_context.getColor(R.color.bg_gray));
                }else{
                    holder.layoutRoot.setBackgroundColor(_context.getColor(R.color.white));
                }
                if(item.etTitleGubun == Constants.ReportCardType.MIDDLE.getCode()) {
                    holder.cbSelect.setEnabled(false);
                    holder.cbSelect.setBackground(disableDrawable);
                }else{
                    holder.cbSelect.setEnabled(true);
                    holder.cbSelect.setBackground(null);
                }
                break;
            case SELECT_FORM_TYPE:
                holder.layoutRoot.setBackgroundColor(_context.getColor(R.color.white));
                holder.cbSelect.setBackground(null);
                holder.cbSelect.setEnabled(true);
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
        private LinearLayoutCompat layoutCheckbox;
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
            layoutCheckbox = itemView.findViewById(R.id.layout_checkbox);
            cbSelect = itemView.findViewById(R.id.cb_select);
            spinnerType = itemView.findViewById(R.id.spinner_type);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                ReportCardData item = _list.get(position);
                if (_list.size() > 0) _listener.onItemClick(position, item);
            });
            switch(_listType){
                case VIEW:
//                    layoutRoot.setClickable(true);
                    int selectableItemBackgroundResource = android.R.attr.selectableItemBackground;
                    TypedValue typedValue = new TypedValue();
                    _context.getTheme().resolveAttribute(selectableItemBackgroundResource, typedValue, true);
                    int selectableItemBackgroundDrawableId = typedValue.resourceId;
                    layoutRoot.setForeground(_context.getDrawable(selectableItemBackgroundDrawableId));
                    layoutCheckbox.setVisibility(View.GONE);
                    layoutCheckbox.setOnClickListener(null);
                    spinnerType.setVisibility(View.GONE);
                    break;
                case CHECK:
//                    layoutRoot.setClickable(false);
                    layoutRoot.setForeground(null);
//                    tvGubun.setVisibility(View.VISIBLE);
//                    tvGrade.setVisibility(View.VISIBLE);
                    layoutCheckbox.setVisibility(View.VISIBLE);
                    layoutCheckbox.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view) {
                            int position = getBindingAdapterPosition();
                            if(_list != null && position != NO_POSITION && position < _list.size()) {
                                ReportCardData item = _list.get(position);
                                if (cbSelect.isEnabled()) {
                                    cbSelect.setChecked(!cbSelect.isChecked());
                                } else {
                                    if((item.etTitleGubun == Constants.ReportCardType.MIDDLE.getCode())) {
                                        cbSelect.setChecked(false);
                                        Toast.makeText(_context, R.string.err_msg_middle_not_supported, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    });
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
//                    layoutRoot.setClickable(false);
                    layoutRoot.setForeground(null);
//                    tvGubun.setVisibility(View.GONE);
//                    tvGrade.setVisibility(View.GONE);
                    layoutCheckbox.setVisibility(View.GONE);
                    layoutCheckbox.setOnClickListener(null);
                    spinnerType.setVisibility(View.VISIBLE);
//                    spinnerType.setIsFocusable(true);
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
