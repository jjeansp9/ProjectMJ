package kr.jeet.edu.manager.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.model.data.RecipientStudentData;
import kr.jeet.edu.manager.utils.LogMgr;

public class RecipientListAdapter extends RecyclerView.Adapter<RecipientListAdapter.ViewHolder> implements Filterable {

    public interface onItemClickListener {
        void onItemClick(RecipientStudentData item);
        void onCheckedParent(int position, boolean b);
        void onCheckedStudent(int position, boolean b);
        void onFilteringCompleted();
    }
    public onItemClickListener _itemClickListener;
    private Context _context;
    public List<RecipientStudentData> _list;
    public List<RecipientStudentData> _filteredlist;
    private Drawable disableDrawable;
    private Drawable installedDrawable;
    private Drawable uninstalledDrawable;
    /**
     * 보기/설정 모드
     * true : 설정모드
     */
    private boolean _isEditMode = true;
    /**
     * 표시 열 설정
     */
    private Constants.ShowCheckboxColumnType _columnType = Constants.ShowCheckboxColumnType.TYPE_BOTH;
    private boolean _isIgnoreInstalled = false;
    public RecipientListAdapter(Context context, List<RecipientStudentData> list, onItemClickListener listener) {
        this._context = context;
        this._list = list;
        this._filteredlist = list;
        this._itemClickListener = listener;
        disableDrawable = _context.getDrawable(R.drawable.ic_vector_checkbox_disabled);
        installedDrawable = _context.getDrawable(R.drawable.icon_app_have);
        uninstalledDrawable = _context.getDrawable(R.drawable.icon_app_haveno);
    }
    public void setEditMode(boolean isEditMode) {
        _isEditMode = isEditMode;
    }
    public void setIgnoreInstalled(boolean isIgnore) {
        _isIgnoreInstalled = isIgnore;
    }
    public void setShowColumns(Constants.ShowCheckboxColumnType type) {
        _columnType = type;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(_context).inflate(R.layout.layout_recipient_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(position == NO_POSITION) return;
        RecipientStudentData recipientData = _filteredlist.get(position);
        if(position % 2 == 0) {
            holder.rootView.setBackgroundColor(_context.getColor(R.color.bg_white));
        }else{
            holder.rootView.setBackgroundColor(_context.getColor(R.color.white));
        }
        holder.tvName.setText(recipientData.stName);
        if(_isEditMode) {   //수정모드
            holder.rootView.setClickable(false);
            holder.layoutCheckBoxBoth.setClickable(true);
            holder.layoutCheckBoxStudent.setClickable(true);
            holder.layoutCheckBoxParent.setClickable(true);
            if (_columnType != Constants.ShowCheckboxColumnType.TYPE_NONE) {
                holder.cbBoth.setVisibility(View.VISIBLE);
            } else {
                holder.cbBoth.setVisibility(View.INVISIBLE);
            }
            if (_columnType.ordinal() >= Constants.ShowCheckboxColumnType.TYPE_PARENT_ONLY.ordinal()) {
                holder.cbParent.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(recipientData.parentPhoneNumber)) {
                    holder.cbParent.setEnabled(false);
                    holder.cbParent.setBackground(disableDrawable);
                    holder.viewParentInstall.setBackground(null);
                } else {
                    holder.cbParent.setEnabled(true);
                    holder.cbParent.setBackground(null);
                    if (recipientData.parentInstall.equals("Y")) {
                        holder.viewParentInstall.setBackground(installedDrawable);
                    } else {
                        holder.viewParentInstall.setBackground(uninstalledDrawable);
                    }
                }
            } else {
                holder.cbParent.setVisibility(View.INVISIBLE);
            }
            if (_columnType == Constants.ShowCheckboxColumnType.TYPE_STUDENT_ONLY ||
                    _columnType == Constants.ShowCheckboxColumnType.TYPE_BOTH) {
                holder.cbStudent.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(recipientData.stPhoneNumber)) {
                    holder.cbStudent.setEnabled(false);
                    holder.cbStudent.setBackground(disableDrawable);
                    holder.viewStudentInstall.setBackground(null);
                } else {
                    holder.cbStudent.setEnabled(true);
                    holder.cbStudent.setBackground(null);
                    if (recipientData.stInstall.equals("Y")) {
                        holder.viewStudentInstall.setBackground(installedDrawable);
                    } else {
                        holder.viewStudentInstall.setBackground(uninstalledDrawable);
                    }
                }
            } else {
                holder.cbStudent.setVisibility(View.INVISIBLE);
            }
            if (_isIgnoreInstalled) {
                if (holder.cbStudent.getVisibility() == View.VISIBLE && recipientData.stInstall.equals("Y")) {
                    holder.cbStudent.setEnabled(false);
                    holder.cbStudent.setBackground(disableDrawable);
                }
                if (holder.cbParent.getVisibility() == View.VISIBLE && recipientData.parentInstall.equals("Y")) {
                    holder.cbParent.setEnabled(false);
                    holder.cbParent.setBackground(disableDrawable);
                }
            }
            if (holder.cbParent.getVisibility() == View.VISIBLE) {
                if (holder.cbParent.isEnabled()) {
                    holder.cbParent.setChecked(recipientData.isCheckParent);
                } else {
                    holder.cbParent.setChecked(false);
                }
            }
            if (holder.cbStudent.getVisibility() == View.VISIBLE) {
                if (holder.cbStudent.isEnabled()) {
                    holder.cbStudent.setChecked(recipientData.isCheckStudent);
                } else {
                    holder.cbStudent.setChecked(false);
                }
            }
            if (holder.cbBoth.getVisibility() == View.VISIBLE) {
                if (!holder.cbParent.isEnabled() && !holder.cbStudent.isEnabled()) {
                    holder.cbBoth.setEnabled(false);
                    holder.cbBoth.setBackground(disableDrawable);
                } else {
                    holder.cbBoth.setEnabled(true);
                    holder.cbBoth.setBackground(null);
                }
                if (holder.cbBoth.isEnabled()) {
                    holder.cbBoth.setChecked(
                            (recipientData.isCheckParent || TextUtils.isEmpty(recipientData.parentPhoneNumber) || (_isIgnoreInstalled && recipientData.parentInstall.equals("Y")))
                                    && (recipientData.isCheckStudent || TextUtils.isEmpty(recipientData.stPhoneNumber) || (_isIgnoreInstalled && recipientData.stInstall.equals("Y")))
                    );
                }
            }else{
                holder.cbBoth.setBackground(null);
            }
        }else{  //보기 모드
            holder.rootView.setClickable(true);
            holder.layoutCheckBoxBoth.setClickable(false);
            holder.layoutCheckBoxStudent.setClickable(false);
            holder.layoutCheckBoxParent.setClickable(false);
            holder.cbParent.setVisibility(View.INVISIBLE);
            if(_columnType == Constants.ShowCheckboxColumnType.TYPE_BOTH ||
                _columnType == Constants.ShowCheckboxColumnType.TYPE_PARENT_ONLY) {
                if (TextUtils.isEmpty(recipientData.parentPhoneNumber)) {
                    holder.viewParentInstall.setBackground(null);
                } else {
                    if (recipientData.parentInstall.equals("Y")) {
                        holder.viewParentInstall.setBackground(installedDrawable);
                    } else {
                        holder.viewParentInstall.setBackground(uninstalledDrawable);
                    }
                }
            }else{
                holder.viewParentInstall.setBackground(null);
            }
            holder.cbStudent.setVisibility(View.INVISIBLE);
            if(_columnType == Constants.ShowCheckboxColumnType.TYPE_BOTH ||
                    _columnType == Constants.ShowCheckboxColumnType.TYPE_STUDENT_ONLY) {
                if (TextUtils.isEmpty(recipientData.stPhoneNumber)) {
                    holder.viewStudentInstall.setBackground(null);
                } else {
                    if (recipientData.stInstall.equals("Y")) {
                        holder.viewStudentInstall.setBackground(installedDrawable);
                    } else {
                        holder.viewStudentInstall.setBackground(uninstalledDrawable);
                    }
                }
            }else{
                holder.viewStudentInstall.setBackground(null);
            }
            holder.cbBoth.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        if(_filteredlist == null) return 0;
        return _filteredlist.size();
    }
//    public int getCheckedItemCount() {
//        if(_filteredlist == null) return 0;
//        return (int)(_filteredlist.stream().filter(t->t.isCheckParent).count() + _filteredlist.stream().filter(t->t.isCheckStudent).count());
//    }
    //region implementation Filterable
    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence charsequence) {
                String trigger = charsequence.toString();
                if(trigger.isEmpty()) {
                    _filteredlist = _list;
                }else{
                    List<RecipientStudentData> filteringList = new ArrayList<>();
                    filteringList = _list.stream().filter(t->t.stName.contains(trigger)).collect(Collectors.toList());
                    _filteredlist = filteringList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = _filteredlist;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

                _filteredlist = (List<RecipientStudentData>) filterResults.values;
                LogMgr.e( "publishResult " + _filteredlist.size());
                notifyDataSetChanged();
                _itemClickListener.onFilteringCompleted();
            }
        };
    }
    //endregion

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout rootView;
        private LinearLayoutCompat layoutCheckBoxBoth, layoutCheckBoxStudent, layoutCheckBoxParent;
        private MaterialCheckBox cbBoth;
        private MaterialCheckBox cbStudent;
        private MaterialCheckBox cbParent;
        private TextView tvName /*, tvBirthday*/;
        private View viewStudentInstall, viewParentInstall;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.recipient_item_root);
            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getBindingAdapterPosition();
                    if(_filteredlist != null && position != NO_POSITION && position < _filteredlist.size()) {
                        RecipientStudentData recipient = _filteredlist.get(position);
                        _itemClickListener.onItemClick(recipient);
                    }
                }
            });
            viewStudentInstall = itemView.findViewById(R.id.view_student_install);
            viewParentInstall = itemView.findViewById(R.id.view_parent_install);
            tvName = itemView.findViewById(R.id.tv_student_name);
//            tvBirthday = itemView.findViewById(R.id.tv_birthday);
            cbBoth = itemView.findViewById(R.id.checkbox_both);
            cbStudent = itemView.findViewById(R.id.checkbox_student);
            cbParent = itemView.findViewById(R.id.checkbox_parent);
//            cbBoth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                    LogMgr.w("both checkedChange " + b);
//                    int position = getBindingAdapterPosition();
//                    if(_list != null && position != NO_POSITION && position < _list.size()) {
////                        RecipientStudentData recipient = _list.get(position);
////                        recipient.isCheckParent = b;
////                        recipient.isCheckStudent = b;
//                        cbStudent.setChecked(b);
//                        cbParent.setChecked(b);
//                    }
//                }
//            });
            cbStudent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    LogMgr.w("student checkedChange " + b);
                    int position = getBindingAdapterPosition();
                    if(_filteredlist != null && position != NO_POSITION && position < _filteredlist.size()) {
                        RecipientStudentData recipient = _filteredlist.get(position);
                        recipient.isCheckStudent = b;
//                        notifyItemChanged(position);
                        if(cbBoth.isEnabled()) {
                            if (b && (recipient.isCheckParent || TextUtils.isEmpty(recipient.parentPhoneNumber) || (_isIgnoreInstalled && recipient.parentInstall.equals("Y")))) {
                                cbBoth.setChecked(true);
                            } else {
                                cbBoth.setChecked(false);
                            }
                        }
//                        _itemClickListener.onCheckedStudent(recipient);
                    }


                }
            });
            cbParent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    LogMgr.w("parent checkedChange " + b);
                    int position = getBindingAdapterPosition();
                    if(_filteredlist != null && position != NO_POSITION && position < _filteredlist.size()) {
                        RecipientStudentData recipient = _filteredlist.get(position);
                        recipient.isCheckParent = b;
//                        notifyItemChanged(position);
                        if(cbBoth.isEnabled()) {
                            if (b && (recipient.isCheckStudent || TextUtils.isEmpty(recipient.stPhoneNumber) || (_isIgnoreInstalled && recipient.stInstall.equals("Y")))) {
                                cbBoth.setChecked(true);
                            } else {
                                cbBoth.setChecked(false);
                            }
                        }
//                        _itemClickListener.onCheckedParent(recipient);
                    }

                }
            });
            layoutCheckBoxBoth = itemView.findViewById(R.id.layout_checkbox_both);
            layoutCheckBoxBoth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(cbBoth.isEnabled()) {
                        boolean todo = !cbBoth.isChecked();
                        cbBoth.setChecked(todo);
                        if (cbStudent.isEnabled()) {
                            cbStudent.setChecked(todo);
                            _itemClickListener.onCheckedStudent(getBindingAdapterPosition(), todo);
                        }
                        if (cbParent.isEnabled()) {
                            cbParent.setChecked(todo);
                            _itemClickListener.onCheckedParent(getBindingAdapterPosition(), todo);
                        }
                    }

                }
            });
            layoutCheckBoxStudent = itemView.findViewById(R.id.layout_checkbox_student);
            layoutCheckBoxStudent.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    int position = getBindingAdapterPosition();
                    if(_filteredlist != null && position != NO_POSITION && position < _filteredlist.size()) {
                        RecipientStudentData recipient = _filteredlist.get(position);
                        if (cbStudent.isEnabled()) {
                            cbStudent.setChecked(!cbStudent.isChecked());
                            _itemClickListener.onCheckedStudent(getBindingAdapterPosition(), cbStudent.isChecked());
                        } else {
                            if(TextUtils.isEmpty(recipient.stPhoneNumber)) {
                                Toast.makeText(_context, R.string.error_msg_empty_phonenumber, Toast.LENGTH_SHORT).show();
                            }else if("Y".equals(recipient.stInstall)) {
                                Toast.makeText(_context, R.string.error_msg_already_install, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });
            layoutCheckBoxParent = itemView.findViewById(R.id.layout_checkbox_parent);
            layoutCheckBoxParent.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    int position = getBindingAdapterPosition();
                    if(_filteredlist != null && position != NO_POSITION && position < _filteredlist.size()) {
                        RecipientStudentData recipient = _filteredlist.get(position);
                        if (cbParent.isEnabled()) {
                            cbParent.setChecked(!cbParent.isChecked());
                            _itemClickListener.onCheckedParent(position, cbParent.isChecked());
                        } else {
                            if(TextUtils.isEmpty(recipient.parentPhoneNumber)) {
                                Toast.makeText(_context, R.string.error_msg_empty_phonenumber, Toast.LENGTH_SHORT).show();
                            }else if("Y".equals(recipient.parentInstall)) {
                                Toast.makeText(_context, R.string.error_msg_already_install, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });
        }
    }
}
