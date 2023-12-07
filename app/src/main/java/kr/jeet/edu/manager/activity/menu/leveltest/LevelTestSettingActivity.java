package kr.jeet.edu.manager.activity.menu.leveltest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.OnSpinnerOutsideTouchListener;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.BaseActivity;
import kr.jeet.edu.manager.adapter.LevelTestTimeListAdapter;
import kr.jeet.edu.manager.adapter.WrapContentSpinnerAdapter;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.dialog.TimePickerFragment;
import kr.jeet.edu.manager.model.data.LevelTestTimeData;
import kr.jeet.edu.manager.model.data.LevelTestTimeHeaderData;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.model.response.LevelTestTimeListResponse;
import kr.jeet.edu.manager.model.response.LevelTestTimeResponse;
import kr.jeet.edu.manager.server.RetrofitClient;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.view.CustomAppbarLayout;
import kr.jeet.edu.manager.view.CustomTimePicker;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LevelTestSettingActivity extends BaseActivity implements TimePickerFragment.OnTimeSetListener{
    private static final String TAG = "levelTestSetting";
    public interface TimeListItem extends Comparable<TimeListItem> {
        boolean isHeader();
        Constants.SchoolGradeType getGrade();
    }
    //region views
    PowerSpinnerView spinnerGrade;
    PowerSpinnerView spinnerDayOfWeek;
    TextView tvTime;
    TextView tvEmptyList;
    RecyclerView recyclerViewTimes;
    //endregion

    //region variables
    LevelTestTimeListAdapter _timeListAdapter;
    Date _selectedTime = new Date();
    ArrayList<TimeListItem> _timeList = new ArrayList<>();
    LevelTestTimeData _currentData = new LevelTestTimeData();

    //endregion
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_test_setting);
        mContext = this;
        initAppbar();
        initView();
    }

    void initView() {
        tvEmptyList = findViewById(R.id.tv_empty_list);
        recyclerViewTimes = findViewById(R.id.recyclerview_times);
        _timeListAdapter = new LevelTestTimeListAdapter(mContext, _timeList, new LevelTestTimeListAdapter.onItemClickListener() {
            @Override
            public void onDeleteClick(int position, TimeListItem item) {
                showMessageDialog(getString(R.string.dialog_title_alarm)
                        , getString(R.string.general_item_confirm_delete)
                        , new View.OnClickListener() {  //OKClickListener
                            @Override
                            public void onClick(View view) {
                                requestTimeDelete(position, ((LevelTestTimeData)item).seq);
                                hideMessageDialog();
                            }
                        },
                        new View.OnClickListener() {    //cancelClickListener
                            @Override
                            public void onClick(View view) {
                                hideMessageDialog();
                            }
                        });

//                _timeList.remove(item);
//                _timeListAdapter.notifyItemRemoved(position);
            }
        });
        FlexboxLayoutManager fblManager = new FlexboxLayoutManager(this);
        fblManager.setFlexWrap(FlexWrap.WRAP);
        fblManager.setFlexDirection(FlexDirection.ROW);
        fblManager.setJustifyContent(JustifyContent.FLEX_START);
        recyclerViewTimes.setLayoutManager(fblManager);
        recyclerViewTimes.setAdapter(_timeListAdapter);
        //region spinner
        spinnerGrade = findViewById(R.id.spinner_grade);
        setSpinnerAdapter(Constants.SchoolGradeType.getNameList(), spinnerGrade);
//        spinnerGrade.setItems(Constants.SchoolGradeType.getNameList());
        spinnerGrade.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                LogMgr.e(newItem + " selected");
                if(newItem.equals(oldItem)) return;
                _currentData.grade = newItem;
            }
        });
        spinnerGrade.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                spinnerGrade.dismiss();
            }
        });
        spinnerGrade.setLifecycleOwner(this);

        spinnerDayOfWeek = findViewById(R.id.spinner_day_of_week);
//        {
//            WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, Constants.WeekdayType.getNameList(), spinnerDayOfWeek);
//            spinnerDayOfWeek.setSpinnerAdapter(adapter);
//        }
        setSpinnerAdapter(Constants.WeekdayType.getNameList(), spinnerDayOfWeek);
//        spinnerDayOfWeek.setItems(Constants.WeekdayType.getNameList());
        spinnerDayOfWeek.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                LogMgr.e(newItem + " selected");
                if(newItem.equals(oldItem)) return;
                if(newItem.equals(Constants.WeekdayType.WEEKDAY.getNameKor())) {
                    _currentData.weekend = Constants.WeekdayType.WEEKDAY.getCode();
                }else if(newItem.equals(Constants.WeekdayType.WEEKEND.getNameKor())) {
                    _currentData.weekend = Constants.WeekdayType.WEEKEND.getCode();
                }
            }
        });
        spinnerDayOfWeek.setSpinnerOutsideTouchListener(new OnSpinnerOutsideTouchListener() {
            @Override
            public void onSpinnerOutsideTouch(@NonNull View view, @NonNull MotionEvent motionEvent) {
                spinnerDayOfWeek.dismiss();
            }
        });
        spinnerDayOfWeek.setLifecycleOwner(this);
        //endregion
        tvTime = findViewById(R.id.tv_time);
        tvTime.setOnClickListener(this);
        findViewById(R.id.btn_add).setOnClickListener(this);
    }
    private void setSpinnerAdapter(List<String> list, PowerSpinnerView view) {
        WrapContentSpinnerAdapter adapter = new WrapContentSpinnerAdapter(mContext, list, view);
        view.setSpinnerAdapter(adapter);
    }
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_setting_leveltest_time);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestTimeList();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.tv_time:
                showTimePicker();
                break;
            case R.id.btn_add:
                if(checkValid()) updateLevelTestTime();
                break;

        }
    }
    private boolean checkValid() {
        if(TextUtils.isEmpty(_currentData.grade)) {
            Toast.makeText(mContext, R.string.msg_empty_school_grade, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(_currentData.weekend < 0) {
            Toast.makeText(mContext, R.string.msg_empty_day_of_week, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(_currentData.time)) {
            Toast.makeText(mContext, R.string.msg_empty_time, Toast.LENGTH_SHORT).show();
            return false;
        }
        for(TimeListItem item : _timeList) {
            if(item instanceof LevelTestTimeData) {
                LevelTestTimeData levelItem = (LevelTestTimeData) item;
                if(_currentData.grade.equals(levelItem.grade)
                    && _currentData.weekend == levelItem.weekend
                    && _currentData.time.equals(levelItem.time)){
                    Toast.makeText(mContext, R.string.msg_duplicate_data, Toast.LENGTH_SHORT).show();
                    return false;
                }


            }
        }
        return true;
    }
    // 레벨테스트 시간 목록 조회
    private void requestTimeList(){

        if (RetrofitClient.getInstance() != null){
            int memberSeq = 0;  // 매니저앱에서는 memberSeq 를 0으로
            RetrofitClient.getApiInterface().getLevelTestTimeList().enqueue(new Callback<LevelTestTimeListResponse>() {
                @Override
                public void onResponse(Call<LevelTestTimeListResponse> call, Response<LevelTestTimeListResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            List<LevelTestTimeData> getData = null;

                            if (response.body() != null) {
                                initTimeData();
                                getData = response.body().data;
                                if (getData != null){
                                    getData.forEach(t->t.isHeader());
                                    _timeList.addAll(getData);
                                    Collections.sort(_timeList);
                                    _timeListAdapter.notifyDataSetChanged();

                                }else LogMgr.e(TAG, "ListData is null");
//                                //test//
//                                for(TimeListItem item : _timeList) {
//                                    LogMgr.w(TAG, "grade = " + ((LevelTestTimeData)item).grade + "/ time = " + ((LevelTestTimeData)item).time + "/ .isHeader() = " + item.isHeader());
//                                }
                            }
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestBoardList() Exception : ", e.getMessage());
                    }finally {
                        checkEmptyRecyclerView();
                    }
                }

                @Override
                public void onFailure(Call<LevelTestTimeListResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBoardList() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
//                    hideProgressDialog();
                    checkEmptyRecyclerView();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    //시간 등록
    private void updateLevelTestTime(){

        if (RetrofitClient.getInstance() != null){
            int memberSeq = 0;  // 매니저앱에서는 memberSeq 를 0으로
            RetrofitClient.getApiInterface().updateLevelTestTime(_currentData).enqueue(new Callback<LevelTestTimeResponse>() {
                @Override
                public void onResponse(Call<LevelTestTimeResponse> call, Response<LevelTestTimeResponse> response) {
                    try {
                        if (response.isSuccessful()){
                            LevelTestTimeData getData = null;

                            if (response.body() != null) {

                                getData = response.body().data;
                                if (getData != null){

                                    _timeList.add(getData);
                                    Collections.sort(_timeList);
                                    int index = _timeList.indexOf(getData);
                                    _timeListAdapter.notifyItemInserted(index);
//                                    _timeListAdapter.notifyDataSetChanged();
                                    clearInputPanel();
                                }else LogMgr.e(TAG, "ListData is null");
                            }
                        }else{
                            Toast.makeText(mContext, R.string.server_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch (Exception e){
                        LogMgr.e(TAG + "requestBoardList() Exception : ", e.getMessage());
                    }finally {
                        checkEmptyRecyclerView();
                    }
                }

                @Override
                public void onFailure(Call<LevelTestTimeResponse> call, Throwable t) {
                    try {
                        LogMgr.e(TAG, "requestBoardList() onFailure >> " + t.getMessage());
                    }catch (Exception e){
                    }
//                    hideProgressDialog();
                    checkEmptyRecyclerView();
                    Toast.makeText(mContext, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void checkEmptyRecyclerView() {
        if(_timeListAdapter != null) {
            if(_timeListAdapter.getItemCount() > 0) {
                tvEmptyList.setVisibility(View.GONE);
            }else{
                tvEmptyList.setVisibility(View.VISIBLE);
            }
        }else{
            tvEmptyList.setVisibility(View.VISIBLE);
        }
    }
    private void clearInputPanel() {
        if(spinnerGrade != null) {
            spinnerGrade.clearSelectedItem();
            spinnerGrade.setItems(Constants.SchoolGradeType.getNameList());
        }
        if(spinnerDayOfWeek != null) {
            spinnerDayOfWeek.clearSelectedItem();
            spinnerDayOfWeek.setItems(Constants.WeekdayType.getNameList());
        }
        tvTime.setText("");
        _currentData = new LevelTestTimeData();
        recyclerViewTimes.postDelayed(() -> _timeListAdapter.notifyDataSetChanged(), 500);

    }
    private void initTimeData() {
        _timeList.clear();
        _timeList.add(new LevelTestTimeHeaderData(Constants.SchoolGradeType.ELEMENTARY.getNameKor()));
        _timeList.add(new LevelTestTimeHeaderData(Constants.SchoolGradeType.MIDDLE.getNameKor()));
        _timeList.add(new LevelTestTimeHeaderData(Constants.SchoolGradeType.HIGH.getNameKor()));
    }
    private void requestTimeDelete(int position, int seq) {
        if(RetrofitClient.getInstance() != null) {
            RetrofitClient.getApiInterface().deleteLevelTestTime(seq).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    try{
                        if(response.isSuccessful()) {
                            _timeList.remove(position);
                            _timeListAdapter.notifyItemRemoved(position);

                        }else{
                            Toast.makeText(mContext, R.string.board_item_deleted_fail, Toast.LENGTH_SHORT).show();
                        }
                    }catch(Exception ex) {

                    }finally{
                        checkEmptyRecyclerView();
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    Toast.makeText(mContext, R.string.board_item_deleted_fail, Toast.LENGTH_SHORT).show();
                    checkEmptyRecyclerView();
                }
            });
        }
    }

//    private void updateSpinnerList(PowerSpinnerView powerSpinner, List<String> newList) {
//        powerSpinner.setItems(newList);
//        PopupWindow popupWindow = powerSpinner.getSpinnerWindow();
//        if (popupWindow != null) {
//            int itemCount = newList.size();
//            int maxHeight = calculatePopupMaxHeight(itemCount);
//            popupWindow.setHeight(maxHeight);
//        }
//    }
//
//    private int calculatePopupMaxHeight(int itemCount) {
//        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//        int screenHeight = displayMetrics.heightPixels;
//        int maxItemHeight = getResources().getDimensionPixelSize(R.dimen.spinner_item_height); // spinner에서 설정한 item 의 높이
//        int maxHeight = Math.min(screenHeight * 4 / 5, maxItemHeight * itemCount);
//
//        return maxHeight;
//    }

    private void showTimePicker() {
        TimePickerFragment dialog = new TimePickerFragment(mContext, this);
        dialog.setOnOkButtonClickListener(v -> dialog.dismiss());
        dialog.setOnCancelButtonClickListener(v -> dialog.dismiss());
        dialog.setTime(12, 0);
        dialog.show();
    }
    @Override
    public void onTimeSet(int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        _currentData.testTime = calendar.getTime();
        LogMgr.d("selected date =" + _currentData.testTime);
        SimpleDateFormat outputDateFormat = new SimpleDateFormat(Constants.TIME_FORMATTER_HH_MM);
        _currentData.time = outputDateFormat.format(_currentData.testTime);
        tvTime.setText(_currentData.time);
    }
}