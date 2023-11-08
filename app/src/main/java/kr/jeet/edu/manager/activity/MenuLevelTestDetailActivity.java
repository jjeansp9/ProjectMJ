package kr.jeet.edu.manager.activity;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.DataManager;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.data.LTCData;
import kr.jeet.edu.manager.model.data.LevelTestData;
import kr.jeet.edu.manager.model.data.SchoolData;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.view.CustomAppbarLayout;

public class MenuLevelTestDetailActivity extends BaseActivity {
    private final static String TAG = "leveltestdetail";
    private LevelTestData mInfo;
    int _userGubun = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_level_test_detail);
        mContext = this;
        _userGubun = PreferenceUtil.getUserGubun(mContext);
        initIntentData();
        initAppbar();
        initView();
        setAnimMove(Constants.MOVE_DETAIL_RIGHT);
    }
    void initIntentData() {
        Intent intent = getIntent();
        if(intent != null) {
            if(intent.hasExtra(IntentParams.PARAM_LEVELTEST_INFO)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mInfo = intent.getParcelableExtra(IntentParams.PARAM_LEVELTEST_INFO, LevelTestData.class);
                }else{
                    mInfo = intent.getParcelableExtra(IntentParams.PARAM_LEVELTEST_INFO);
                }
            }
        }
        if(mInfo == null) {
            finish();
        }
    }
    @Override
    void initAppbar() {
        CustomAppbarLayout customAppbar = findViewById(R.id.customAppbar);
        customAppbar.setTitle(R.string.title_detail);
        customAppbar.setLogoVisible(true);
        customAppbar.setLogoClickable(true);
        setSupportActionBar(customAppbar.getToolbar());
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_icon_back);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    void initView() {
        String str = "";

        ((TextView)findViewById(R.id.txt_st_nm)).setText(TextUtils.isEmpty(mInfo.name) ? "" : mInfo.name);

        str = "";
        if(mInfo.sex != null) {
            str = mInfo.sex.equals("M") ? getString(R.string.male) : getString(R.string.female);
        }
        ((TextView)findViewById(R.id.txt_gender)).setText(str);

        str = TextUtils.isEmpty(mInfo.address) ? "" : mInfo.address;
        if(!TextUtils.isEmpty(mInfo.addressSub)) {
            str += (TextUtils.isEmpty(str) ? "" : " ") + mInfo.addressSub;
        }
        ((TextView)findViewById(R.id.txt_address)).setText(str);

        ((TextView)findViewById(R.id.txt_birth)).setText(TextUtils.isEmpty(mInfo.birth) ? "" : mInfo.birth);

        str = "";
//        for(SchoolData info : DataManager.getInstance().getSchoolList()) {
//            if(mInfo.scCode == info.scCode) {
//                str = TextUtils.isEmpty(info.scName) ? "" : (info.scName + " ");
//                break;
//            }
//        }
        SchoolData schoolData = DataManager.getInstance().getSchoolData(mInfo.scCode);
        if(schoolData != null) str = schoolData.scName + " ";
        if(!TextUtils.isEmpty(mInfo.grade)) {
            try {
                if (mInfo.grade.length() > 0) {
                    int lastChar = mInfo.grade.length() - 1;
                    str += TextUtils.isEmpty(mInfo.grade) ? "" : mInfo.grade.replace("학년", "").charAt(lastChar)+"학년";
                }
            }catch (Exception e) {
                LogMgr.e(TAG, e.getMessage());
            }
        }
        ((TextView)findViewById(R.id.txt_school)).setText(str);

        String phoneNo = Utils.formatPhoneNumber(mInfo.phoneNumber);
        if(_userGubun == Constants.USER_TYPE_TEACHER) {
            phoneNo = Utils.blindPhoneNumber(phoneNo);
        }
        ((TextView)findViewById(R.id.txt_st_phone_num)).setText(phoneNo);

        str = "";
        if(!TextUtils.isEmpty(mInfo.parentName)) {
            str = mInfo.parentName +" "+ getString(R.string.parents);
        }
        phoneNo = Utils.formatPhoneNumber(mInfo.parentPhoneNumber);
        if(_userGubun == Constants.USER_TYPE_TEACHER) {
            phoneNo = Utils.blindPhoneNumber(phoneNo);
        }
        if(!TextUtils.isEmpty(mInfo.parentPhoneNumber)) {
            str += TextUtils.isEmpty(str) ? "" : "\n" + phoneNo;
        }

        ((TextView)findViewById(R.id.txt_parent)).setText(str);

        ((TextView)findViewById(R.id.txt_cash_receipts)).setText(TextUtils.isEmpty(mInfo.cashReceiptNumber) ? "" : mInfo.cashReceiptNumber);
        ((TextView)findViewById(R.id.txt_reason)).setText(TextUtils.isEmpty(mInfo.reason) ? "" : mInfo.reason);

//        for(LTCData info : DataManager.getInstance().getLTCList()) {
//            if(info.ltcCode.equals(mInfo.bigo)) {
//                ((TextView)findViewById(R.id.txt_campus)).setText(TextUtils.isEmpty(info.ltcName) ? "" : (info.ltcName));
//                break;
//            }
//        }
        LTCData ltcData = DataManager.getInstance().getLTCData(mInfo.bigo);
        if(ltcData != null) {
            ((TextView)findViewById(R.id.txt_campus)).setText(ltcData.ltcName);
        }

        ((TextView)findViewById(R.id.txt_subject)).setText(TextUtils.isEmpty(mInfo.subjectName) ? "" : mInfo.subjectName);
        ((TextView)findViewById(R.id.txt_insert_date)).setText(TextUtils.isEmpty(mInfo.registerDate) ? "" : mInfo.registerDate);

        str = "";
        if(!TextUtils.isEmpty(mInfo.reservationDate)) {
            str = mInfo.reservationDate;
        }
        ((TextView)findViewById(R.id.txt_wish_date)).setText(str);

        if(!TextUtils.isEmpty(mInfo.wish)) {
            if (mInfo.wish.equals("0")) {
                str=getString(R.string.informed_question_sel_day_1);
            }else if (mInfo.wish.equals("1")) {
                str=getString(R.string.informed_question_sel_day_2);
            }else if (mInfo.wish.equals("2")) {
                str=getString(R.string.informed_question_sel_day_3);
            }else if (mInfo.wish.equals("3")) {
                str=getString(R.string.informed_question_sel_day_4);
            }else{
                ((ConstraintLayout)findViewById(R.id.layout_wish_day_week)).setVisibility(View.GONE);
            }
        }else{
            ((ConstraintLayout)findViewById(R.id.layout_wish_day_week)).setVisibility(View.GONE);
        }
        ((TextView)findViewById(R.id.txt_wish_day_week)).setText(str);

        {   // 이전학습방법
            //str = "학원 : ";
            str = "";
            String subStr = "";
            if(!TextUtils.isEmpty(mInfo.progressName1)) {
                if (mInfo.progressName1.contains("교습소")) subStr += mInfo.progressName1;
                else subStr += removeLastSpace(mInfo.progressName1.replace("학원", "")) + " 학원";
            }
            if(!TextUtils.isEmpty(mInfo.time1)) {
                if(TextUtils.isEmpty(mInfo.progressName1)) subStr += mInfo.time1;
                else subStr += "\n" + mInfo.time1;
            }
            if(!TextUtils.isEmpty(mInfo.date1)) subStr += (subStr.isEmpty()?"":" / ") + mInfo.date1;
            str += subStr.isEmpty() ? "" : subStr;

            if (TextUtils.isEmpty(str)) ((ConstraintLayout)findViewById(R.id.layout_study_sub_1)).setVisibility(View.GONE);
            else ((TextView)findViewById(R.id.txt_study_1)).setText(str);

            //str += "\n과외 : ";
            str = "";
            subStr = "";
            if(!TextUtils.isEmpty(mInfo.time2)) subStr += mInfo.time2;
            if(!TextUtils.isEmpty(mInfo.date2)) subStr += (subStr.isEmpty()?"":" / ") + mInfo.date2;
            str += subStr.isEmpty() ? "" : subStr;

            if (TextUtils.isEmpty(str)) ((ConstraintLayout)findViewById(R.id.layout_study_sub_2)).setVisibility(View.GONE);
            else ((TextView)findViewById(R.id.txt_study_2)).setText(str);

            //str += "\n가정학습(자기주도) : ";
            str = "";
            subStr = "";
            if(!TextUtils.isEmpty(mInfo.time3)) subStr += mInfo.time3;
            if(!TextUtils.isEmpty(mInfo.date3)) subStr += (subStr.isEmpty()?"":" / ") + mInfo.date3;
            str += subStr.isEmpty() ? "" : subStr;

            if (TextUtils.isEmpty(str)) ((ConstraintLayout)findViewById(R.id.layout_study_sub_3)).setVisibility(View.GONE);
            else ((TextView)findViewById(R.id.txt_study_3)).setText(str);

            //str += "\n학습지 : ";
            str = "";
            subStr = "";
            if(!TextUtils.isEmpty(mInfo.time4)) subStr += mInfo.time4;
            if(!TextUtils.isEmpty(mInfo.date4)) subStr += (subStr.isEmpty()?"":" / ") + mInfo.date4;
            str += subStr.isEmpty() ? "" : subStr;

            if (TextUtils.isEmpty(str)) ((ConstraintLayout)findViewById(R.id.layout_study_sub_4)).setVisibility(View.GONE);
            else ((TextView)findViewById(R.id.txt_study_4)).setText(str);
        }

        {   // 진도/심화학습
            str = "";
            String subStr = "";
            if(!TextUtils.isEmpty(mInfo.processEtc1)) {
                if (!TextUtils.isEmpty(mInfo.processText1))
                    subStr += "[ " + mInfo.processText1 + " ] ";
                else subStr += "[ 미선택 ] ";
                if (!TextUtils.isEmpty(mInfo.processEtc1)) subStr += mInfo.processEtc1;
                str += subStr.isEmpty() ? "" : subStr;
            }
            subStr = "";
            if(!TextUtils.isEmpty(mInfo.processEtc2)) {
                if (!TextUtils.isEmpty(mInfo.processText2))
                    subStr += "[ " + mInfo.processText2 + " ] ";
                else subStr += "[ 미선택 ] ";
                if (!TextUtils.isEmpty(mInfo.processEtc2)) subStr += mInfo.processEtc2;
                str += subStr.isEmpty() ? "" : (str.isEmpty() ? subStr : "\n" + subStr);
            }
            subStr = "";
            if(!TextUtils.isEmpty(mInfo.processEtc3)) {
                if (!TextUtils.isEmpty(mInfo.processText3))
                    subStr += "[ " + mInfo.processText3 + " ] ";
                else subStr += "[ 미선택 ] ";
                if (!TextUtils.isEmpty(mInfo.processEtc3)) subStr += mInfo.processEtc3;
                str += subStr.isEmpty() ? "" : (str.isEmpty() ? subStr : "\n" + subStr);
            }
            ((TextView)findViewById(R.id.txt_process)).setText(str);
        }

        if(!TextUtils.isEmpty(mInfo.study)) {
            str = "";
            String[] arrStudy = mInfo.study.split("\\^");
            for(int i = 0; i < arrStudy.length; i++) {
                str += ", " + arrStudy[i];
            }
            str = str.replaceFirst(", ", "");
            ((TextView)findViewById(R.id.txt_option)).setText(str);
        }else {
            ((ConstraintLayout)findViewById(R.id.ly_option)).setVisibility(View.GONE);
        }

        if(!TextUtils.isEmpty(mInfo.highSchool)) {
            str = "";
            String[] arrStudy = mInfo.highSchool.split("\\^");
            for (String s : arrStudy) {
                str += ", " + s;
            }
            str = str.replaceFirst(", ", "");
            ((TextView)findViewById(R.id.txt_wish_high_school)).setText(str);
        }else {
            ((ConstraintLayout)findViewById(R.id.ly_wish_high_school)).setVisibility(View.GONE);
        }

        str = "";
        if(!TextUtils.isEmpty(mInfo.gifted) && mInfo.grade.contains("중")) {
            str += "※ 지트 중등영재센터 입학 ";
            str += mInfo.gifted.equals("Y") ? "희망" : "미희망";
            if(!TextUtils.isEmpty(mInfo.etc)) str += "\n\n";
        }
        if(!TextUtils.isEmpty(mInfo.etc)) str += mInfo.etc;
        if(!str.isEmpty()) ((TextView)findViewById(R.id.txt_etc)).setText(str);
        else ((ConstraintLayout)findViewById(R.id.ly_etc)).setVisibility(View.GONE);

    }

    private String removeLastSpace(String str) {
        if (str == null || str.length() == 0) return str;

        char lastChar = str.charAt(str.length() - 1);
        if (lastChar == ' ') str = str.substring(0, str.length() - 1);

        return str;
    }
}