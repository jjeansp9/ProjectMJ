package kr.jeet.edu.manager.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.adapter.SchoolListAdapter;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.model.data.SchoolData;
import kr.jeet.edu.manager.utils.LogMgr;

public class SchoolListBottomSheetDialog extends BottomSheetDialogFragment {
    private static final String TAG = "bottomsheet";
    RecyclerView _recyclerViewSchoolList;
    EditText _editTextSearch;

    List<SchoolData> _schoolList = new ArrayList<>();
    SchoolListAdapter _schoolListAdapter;
    public SchoolListBottomSheetDialog(SchoolListAdapter adapter) {
        this._schoolListAdapter = adapter;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        return new BottomSheetDialog(requireContext(), getTheme());
//        return super.onCreateDialog(savedInstanceState);
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            setupRatio(bottomSheetDialog);
        });
        return  dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.layout_bottomsheet_recyclerview, container, false);
        _recyclerViewSchoolList = view.findViewById(R.id.recyclerview_school);
        _editTextSearch = view.findViewById(R.id.et_search);
        _editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                _schoolListAdapter.getFilter().filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        _schoolList.addAll(DataManager.getInstance().getSchoolListMap().values());

        _recyclerViewSchoolList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        _recyclerViewSchoolList.setAdapter(_schoolListAdapter);
        _schoolListAdapter.notifyDataSetChanged();
        LogMgr.e(TAG, "_schoolList size = " + _schoolList.size());

    }
//    @Override
//    public void onStart() {
//        super.onStart();
//
//        // BottomSheetDialog의 높이를 화면의 50%로 설정
//        Dialog dialog = getDialog();
//        if (dialog != null) {
//
//            ((BottomSheetDialog)dialog).getBehavior().setPeekHeight((int) (getResources().getDisplayMetrics().heightPixels * 0.5), true);
//            //            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
////            if (bottomSheet != null) {
////                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
////                behavior.setPeekHeight((int) (getResources().getDisplayMetrics().heightPixels * 0.5)); // 50%로 설정
////            }
//        }

    private void setupRatio(BottomSheetDialog bottomSheetDialog) {
        //id = com.google.android.material.R.id.design_bottom_sheet for Material Components
        //id = android.support.design.R.id.design_bottom_sheet for support librares
        FrameLayout bottomSheet = (FrameLayout)
                bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
        layoutParams.height = getBottomSheetDialogDefaultHeight();
        bottomSheet.setLayoutParams(layoutParams);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
    private int getBottomSheetDialogDefaultHeight() {
        return getWindowHeight() * 85 / 100;
    }
    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if(_editTextSearch != null) {
            _editTextSearch.getText().clear();
        }
        super.onDismiss(dialog);

    }
}
