package kr.jeet.edu.manager.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.model.data.AttendanceData;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.Utils;

public class StudentInfoListAdapter extends RecyclerView.Adapter<StudentInfoListAdapter.ViewHolder>{
    private static final String TAG = "studendlistadapter";
    private static final int NO_SELECTED_VALUE = -1;
    public interface onItemClickListener{
        void onItemClick(int position);
        void onCheckBoxChanged(int position, boolean isChecked);
        void onEditNote(int position);
    }
    private Context _context;
    private List<AttendanceData> _list;
    private onItemClickListener _listener;
    private boolean _isEditMode = false;
    SimpleDateFormat _inputDateFormat;
    SimpleDateFormat _outputDateFormat;

    public StudentInfoListAdapter(Context _context, List<AttendanceData> _list, onItemClickListener _listener) {
        this._context = _context;
        this._list = _list;
        this._listener = _listener;
        _inputDateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYYY_MM_DD);
        _outputDateFormat = new SimpleDateFormat(Constants.DATE_FORMATTER_YYMMDD);

    }
    public void setEditMode(boolean isEditMode) {
        _isEditMode = isEditMode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(_context).inflate(R.layout.layout_attendance_info, parent, false);
        ViewHolder viewHolder = new ViewHolder(view,new CustomItemClickListener(), new CustomCheckedChangeListener(), new CustomSpinnerItemSelectedListener(), new CustomOnClickListener());
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        LogMgr.e(TAG, "onBindViewHolder at pos : " + position);
        AttendanceData item = _list.get(position);
        if(position != NO_POSITION) {
            if(position %2 == 0) {
                holder.rootView.setBackgroundColor(_context.getColor(R.color.bg_white));
            }else{
                holder.rootView.setBackgroundColor(_context.getColor(R.color.white));
            }
            holder.onItemClickListener.updatePosition(position);
            if(_isEditMode) {
                // Get the selectableItemBackground attribute's resource ID
                holder.rootView.setClickable(false);
                holder.viewGender.setVisibility(View.GONE);
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.checkedChangeListener.updatePosition(position);
                holder.tvStatus.setVisibility(View.GONE);
                holder.spinnerStatus.setVisibility(View.VISIBLE);
                holder.etNote.setVisibility(View.VISIBLE);
//                holder.etNote.setEnabled(true);
//                holder.etNote.setBackground(_context.getDrawable(R.drawable.bg_edittext));
                if(TextUtils.isEmpty(item.attendBigoWrite)) {
                    holder.etNote.setVisibility(View.GONE);
                }else{
                    holder.etNote.setVisibility(View.VISIBLE);
                    if(!TextUtils.isEmpty(item.attendBigoWrite)) {
                        holder.etNote.setText(item.attendBigoWrite);
                    }
                }
                holder.etNote.setEnabled(false);
                Drawable drawable = _context.getDrawable(R.drawable.bg_edittext);
                drawable.setTint(_context.getColor(android.R.color.transparent));
                holder.etNote.setBackground(drawable);
                holder.btnEditBigo.setVisibility(View.VISIBLE);
                holder.onEditNoteClickListener.updatePosition(position);
//                holder.btnEditBigo.setOnClickListener(holder.onEditNoteClickListener);
//                holder.editTextListener.updatePosition(position);

                //set spinner status
//                holder.spinnerStatus.setItems(Constants.AttendanceStatus.getNameList());
                AttendanceSpinnerAdapter adapter = new AttendanceSpinnerAdapter(_context, Constants.AttendanceStatus.getColoredNameList(_context), holder.spinnerStatus);
                adapter.setOnSpinnerItemSelectedListener(holder.spinnerSelectListener);
                holder.spinnerStatus.setSpinnerAdapter(adapter);

                Constants.AttendanceStatus status = Constants.AttendanceStatus.UNSET;
                if(item.attendGubunWrite != NO_SELECTED_VALUE) {
                    status = Constants.AttendanceStatus.getByCode(item.attendGubunWrite);
                }else{
                    status = Constants.AttendanceStatus.getByCode(item.attendGubun);
                }
                holder.spinnerSelectListener.updatePosition(position);
                int index = Constants.AttendanceStatus.getNameList().indexOf(status.getName());
                holder.spinnerStatus.selectItemByIndex(index);
                holder.checkBox.setChecked(item.isChecked);

            }else{
                holder.rootView.setClickable(true);
//                int selectableItemBackgroundResource = android.R.attr.selectableItemBackground;
//                TypedValue typedValue = new TypedValue();
//                _context.getTheme().resolveAttribute(selectableItemBackgroundResource, typedValue, true);
//                int selectableItemBackgroundDrawableId = typedValue.resourceId;
//                holder.rootView.setForeground(_context.getDrawable(selectableItemBackgroundDrawableId));

                holder.viewGender.setVisibility(View.VISIBLE);
                holder.checkBox.setVisibility(View.GONE);
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.spinnerStatus.setVisibility(View.GONE);
                if(TextUtils.isEmpty(item.attendBigo)) {
                    holder.etNote.setVisibility(View.GONE);
                }else{
                    holder.etNote.setVisibility(View.VISIBLE);
                    holder.etNote.setText(item.attendBigo);
                }
                holder.etNote.setEnabled(false);
                Drawable drawable = _context.getDrawable(R.drawable.bg_edittext);
                drawable.setTint(_context.getColor(android.R.color.transparent));
                holder.etNote.setBackground(drawable);
                holder.btnEditBigo.setVisibility(View.GONE);
                //set gender
                if("M".equals(item.stSex)){
                    holder.viewGender.setBackground(_context.getDrawable(R.drawable.bg_circle_male));
                }else{
                    holder.viewGender.setBackground(_context.getDrawable(R.drawable.bg_circle_female));
                }
                //set tv status
                Constants.AttendanceStatus status = Constants.AttendanceStatus.getByCode(item.attendGubun);
                if(status != null) {
                    if(status.getCode() == 9)   //초기값 {
                    {
                        holder.tvStatus.setVisibility(View.GONE);
                    }else {
                        holder.tvStatus.setVisibility(View.VISIBLE);
                        holder.tvStatus.setText(status.getName());
                        Drawable stateDrawable = _context.getResources().getDrawable(R.drawable.bg_attendance_state_attendance, null);

                        stateDrawable.setTint(_context.getResources().getColor(status.getColorRes(), null));
                        holder.tvStatus.setBackground(stateDrawable);
                    }
                }

            }
            //set tv name
            holder.tvName.setText(item.stName);
            //set tv birthday
            try {
                Date birthday = _inputDateFormat.parse(item.stBirth);
                holder.tvBirthday.setText(_outputDateFormat.format(birthday));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public int getItemCount() {
        if(_list == null) return 0;
        return _list.size();
    }

//    @Override
//    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
//        holder.enableTextWatcher();
//        super.onViewAttachedToWindow(holder);
//    }
//
//    @Override
//    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
//        holder.disableTextWatcher();
//        super.onViewDetachedFromWindow(holder);
//    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        private ConstraintLayout rootView;
        private View viewGender;
        private MaterialCheckBox checkBox;
        private TextView tvName, tvBirthday, tvStatus;
        private PowerSpinnerView spinnerStatus;
        private EditText etNote;
        private ImageButton btnEditBigo;
//        public CustomEditTextListener editTextListener;
        public CustomItemClickListener onItemClickListener;
        public CustomCheckedChangeListener checkedChangeListener;
        public CustomSpinnerItemSelectedListener spinnerSelectListener;
        public CustomOnClickListener onEditNoteClickListener;

        public ViewHolder(@NonNull View itemView, CustomItemClickListener itemClickListener, CustomCheckedChangeListener checkListener, CustomSpinnerItemSelectedListener spinnerListener, CustomOnClickListener onNoteClickListener ) {
            super(itemView);
//            editTextListener = editTextlistener;
            rootView = itemView.findViewById(R.id.attendance_item_root);

            onItemClickListener = itemClickListener;
            checkedChangeListener = checkListener;
            spinnerSelectListener = spinnerListener;
            onEditNoteClickListener = onNoteClickListener;
            rootView.setOnClickListener(onItemClickListener);
            viewGender = itemView.findViewById(R.id.view_gender);
            checkBox = itemView.findViewById(R.id.checkbox);
            checkBox.setOnCheckedChangeListener(checkedChangeListener);
            tvName = itemView.findViewById(R.id.tv_student_name);
            tvBirthday = itemView.findViewById(R.id.tv_birthday);
            btnEditBigo = itemView.findViewById(R.id.btn_edit_bigo);
            tvStatus = itemView.findViewById(R.id.tv_attendance_status);
            spinnerStatus = itemView.findViewById(R.id.spinner_attendance_status);
            spinnerStatus.setSpinnerPopupHeight(ConstraintLayout.LayoutParams.WRAP_CONTENT);
            spinnerStatus.setOnSpinnerItemSelectedListener(spinnerSelectListener);
            spinnerStatus.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
                @Override
                public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                    spinnerStatus.dismiss();
                }
            });
            spinnerStatus.setLifecycleOwner((LifecycleOwner) _context);
            etNote = itemView.findViewById(R.id.edit_attendance_note);
            btnEditBigo.setOnClickListener(onEditNoteClickListener);
        }
//        void enableTextWatcher() {
//            etNote.addTextChangedListener(editTextListener);
//        }
//
//        void disableTextWatcher() {
//            etNote.removeTextChangedListener(editTextListener);
//        }
    }
//    private class CustomEditTextListener implements TextWatcher {
//        private int position;
//        public void updatePosition(int position) {
//            this.position = position;
//        }
//        @Override
//        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//        }
//
//        @Override
//        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//        }
//
//        @Override
//        public void afterTextChanged(Editable editable) {
//            if(editable != null) {
//                AttendanceData data = _list.get(position);
//                data.attendBigoWrite = editable.toString();
////                notifyItemChanged(position);
//            }
//        }
//    }
private class CustomItemClickListener implements View.OnClickListener {
    private int position;
    public void updatePosition(int position) {
        this.position = position;
    }

    @Override
    public void onClick(View view) {
        if(!_isEditMode) {
            _listener.onItemClick(position);
        }
    }
}
    private class CustomCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        private int position;
        public void updatePosition(int position) {
            this.position = position;
        }
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            AttendanceData data = _list.get(position);
            data.isChecked = b;
            _listener.onCheckBoxChanged(position, b);
        }
    }
    private class CustomSpinnerItemSelectedListener implements OnSpinnerItemSelectedListener<SpannableString> {
        private int position;
        public void updatePosition(int position) {
            this.position = position;
        }
        @Override
        public void onItemSelected(int oldIndex, @Nullable SpannableString oldItem, int newIndex, SpannableString newItem) {
            LogMgr.e(newItem + " selected");
            Constants.AttendanceStatus selectedStatus = Constants.AttendanceStatus.ATTENDANCE;
            AttendanceData data = _list.get(position);
//            selectedStatus = Constants.AttendanceStatus.getByName(newItem);
            selectedStatus = Constants.AttendanceStatus.values()[newIndex];
            if (data != null) {
                if(_isEditMode) {
                    if (data.attendGubunWrite != -1 && data.attendGubunWrite != selectedStatus.getCode()) {
                        data.isChecked = true;
                        notifyItemChanged(position);
                        LogMgr.w(data.stName + " selected Attendance Code = " + selectedStatus.getCode());
                    }
                    data.attendGubunWrite = selectedStatus.getCode();
                }
            }
        }
    }
    private class CustomOnClickListener implements View.OnClickListener {
        private int position;
        public void updatePosition(int position) {
            this.position = position;
        }
        @Override
        public void onClick(View view) {
            AttendanceData data = _list.get(position);
            _listener.onEditNote(position);
        }
    }

}
