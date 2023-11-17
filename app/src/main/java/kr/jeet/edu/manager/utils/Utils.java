package kr.jeet.edu.manager.utils;

import static kr.jeet.edu.manager.utils.LogMgr.TAG;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.demogorgorn.monthpicker.MonthPickerDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.AgreeTermsActivity;
import kr.jeet.edu.manager.activity.MainActivity;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.model.request.UpdatePushTokenRequest;
import kr.jeet.edu.manager.model.response.BaseResponse;
import kr.jeet.edu.manager.model.response.LoginResponse;
import kr.jeet.edu.manager.server.RetrofitApi;
import kr.jeet.edu.manager.server.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Utils {

    /**
     * 6자리 난수 생성
     */
    public static int generateAuthNum() {
        //return ThreadLocalRandom.current().nextInt(100000, 1000000);
        java.util.Random generator = new java.util.Random();
        generator.setSeed(System.currentTimeMillis());
        return generator.nextInt(1000000) % 1000000;
    }

    public static void refreshPushToken(Context context, int memberSeq, String... tokenArg) {
        LogMgr.e("requestPushTokenToServer() : [" + tokenArg + "]");

        if(tokenArg == null || tokenArg.length == 0) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if(!task.isSuccessful()) {
                        LogMgr.e("FCM registration token failed : " + task.getException().getMessage());
                        return ;
                    }

                    // todo. request server
                    String newToken = task.getResult();
                    LogMgr.e("new FCM token : " + newToken);
                    if(memberSeq > 0) {
                        requestUpdatePushTokenToServer(context, memberSeq, newToken);
                    }
                }
            });

        } else {
            // todo. request server
            String newToken = tokenArg.length > 0? tokenArg[0] : "";
            if(memberSeq > 0) {
                requestUpdatePushTokenToServer(context, memberSeq, newToken);
            }
        }
    }
    public static void requestUpdatePushTokenToServer(Context context, int memberSeq, String token) {
        if(memberSeq == 0) return;
        UpdatePushTokenRequest request = new UpdatePushTokenRequest(memberSeq, token);
        if(RetrofitClient.getInstance() != null) {
            RetrofitApi retrofitApi = RetrofitClient.getApiInterface();
            retrofitApi.updatePushToken(request).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    if(response.isSuccessful()) {
                        String oldToken = PreferenceUtil.getPrefPushToken(context);
                        LogMgr.e("old FCM token : " + oldToken);
                        if(!token.equalsIgnoreCase(oldToken)) {
                            LogMgr.e("preference put : " + token);
                            PreferenceUtil.setPrefPushToken(context, token);
                        }
//                        requestUpdatePushTopic(context, memberSeq);
                    } else {
                        LogMgr.e("updatePushToken() errBody : " + response.errorBody().toString());
                        Toast.makeText(context, R.string.server_fail, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    LogMgr.e("updatePushToken() onFailure >> " + t.getMessage());
                    Toast.makeText(context, R.string.server_error, Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
    // Push Topic update
//    public static void requestUpdatePushTopic(Context context, int memberSeq){
//
//        if (RetrofitClient.getInstance() != null){
//            RetrofitApi retrofitApi = RetrofitClient.getApiInterface();
//            String token = PreferenceUtil.getPrefPushToken(context);
//            retrofitApi.updateSubscribePushTopic(memberSeq, token).enqueue(new Callback<BaseResponse>() {
//                @Override
//                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
//                    try {
//                        if (response.isSuccessful()){
//                            LogMgr.e("requestUpdatePushTopic() success");
//
//                        }else{
//                            Toast.makeText(context, R.string.server_fail, Toast.LENGTH_SHORT).show();
//                        }
//                    }catch (Exception e){
//                        LogMgr.e(e.getMessage());
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<BaseResponse> call, Throwable t) {
//                    try {
//                        LogMgr.e("requestUpdatePushTopic() onFailure >> " + t.getMessage());
//                    }catch (Exception e){
//                    }
//                }
//            });
//        }
//    }

    /**
     * ID찾기에서 문자열 blind 처리 ( 1/2 ceil)
     * @param sourceString 원본 문자열
     * @return blind된 문자열
     */
    public static String makeBlind(String sourceString) {
        int blindCount = 0;
        String convertedString = "";
        if(TextUtils.isEmpty(sourceString)){
            return "";
        }
        blindCount = (sourceString.length() / 2);

        if(blindCount == 0) {
            convertedString = sourceString;
        }else{
            convertedString = sourceString.substring(0, sourceString.length() - blindCount);
            for(int i = 0; i < blindCount; i++) {
                convertedString += "*";
            }
        }
        return convertedString;
    }
    public static String getSNSProvider(String snsCode) {
        switch(snsCode) {
            case "N" :
                return Constants.LOGIN_TYPE_SNS_NAVER_STRING;
            case "K":
                return Constants.LOGIN_TYPE_SNS_KAKAO_STRING;
            case "G":
                return Constants.LOGIN_TYPE_SNS_GOOGLE_STRING;
            case "A":
                return Constants.LOGIN_TYPE_SNS_APPLE_STRING;
            default:
                return "";
        }
    }
    /**
     * 파라미터로 받은 editText의 개수만큼 focus 얻어오고 키보드를 내리는 메소드
     * */
//    public static void hideKeyboard(Context mContext, View... focusList) {
//        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
//        if (inputMethodManager == null || focusList == null) return;
//        for (int i = 0; i < focusList.length; i++) {
//            View view = focusList[i];
//            if (view != null) {
//                view.clearFocus();
//                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
//            }
//        }
//    }
    /**
     * 휴대폰번호 유효성검사
     * */
    public static boolean checkPhoneNumber(String str) {
        if(TextUtils.isEmpty(str)) return false;
        return Pattern.matches("^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}$", str);
    }
    /**
     * 전화번호에 - 붙은 Format 으로 변환
     * @param phoneNumber
     * @return formatted phone number format (ex 010-1234-5678)
     */
    public static  String formatPhoneNumber(String phoneNumber) {
        if(phoneNumber == null) return "";
        String cleanedNumber = phoneNumber.replaceAll("[^0-9]", "");
        try {
            if (cleanedNumber.length() == 10) {
                if (cleanedNumber.startsWith("02")) {   //서울은 02
                    return cleanedNumber.substring(0, 2) + "-" + cleanedNumber.substring(2, 6) + "-" + cleanedNumber.substring(6);
                } else {
                    return cleanedNumber.substring(0, 3) + "-" + cleanedNumber.substring(3, 6) + "-" + cleanedNumber.substring(6);
                }
            } else if (cleanedNumber.length() == 11) {
                return cleanedNumber.substring(0, 3) + "-" + cleanedNumber.substring(3, 7) + "-" + cleanedNumber.substring(7);
            } else {
                return cleanedNumber;
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return cleanedNumber;
    }
    /**
     * Number Cash Receipt
     * */
    public static String formatCashReceiptNum(String num){
        String number = "";
        if (num.equals("")){
            number = "010-000-1234";

        }else if (num.length() == Constants.PHONE_NUM_LENGTH_1){ // 휴대폰번호
            number = MessageFormat.format("{0}-{1}-{2}",
                    num.substring(0, 3),
                    num.substring(3, 7),
                    num.substring(7));
        }else if (num.length() == Constants.PHONE_NUM_LENGTH_2){ // 휴대폰번호2
            number = MessageFormat.format("{0}-{1}-{2}",
                    num.substring(0, 3),
                    num.substring(3, 6),
                    num.substring(6));
        }else if (num.length() == Constants.RES_REGISTRATION_NUM_LENGTH){ // 주민번호
            number = MessageFormat.format("{0}-{1}",
                    num.substring(0, 6),
                    num.substring(6));

        }else if (num.length() == Constants.COMPANY_REGISTRATION_NUM_LENGTH){ // 사업자 등록번호
            number = MessageFormat.format("{0}-{1}-{2}",
                    num.substring(0, 3),
                    num.substring(3, 5),
                    num.substring(5));
        }else if(num.length() == Constants.BIRTH_LENGTH){ // 생년월일
            number = MessageFormat.format("{0}-{1}-{2}",
                    num.substring(0,4),
                    num.substring(4, 6),
                    num.substring(6));

        }else{
            number = "010-000-1234";
        }

        return number;
    }
    public static boolean isToday(Date date) {
        Calendar calendarArg = Calendar.getInstance();
        calendarArg.setTime(date);
        Date today = new Date();
        Calendar calendarToday = Calendar.getInstance();
        calendarToday.setTime(today);
        if(calendarArg.get(Calendar.YEAR) == calendarToday.get(Calendar.YEAR) &&
                calendarArg.get(Calendar.MONTH) == calendarToday.get(Calendar.MONTH) &&
                calendarArg.get(Calendar.DAY_OF_MONTH) == calendarToday.get(Calendar.DAY_OF_MONTH)) {
            return true;
        }
        return false;
    }
    public static void yearMonthPicker(Context mContext, MonthPickerDialog.OnDateSetListener listener, int year, int month){

        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(mContext, listener, year, month);

        try{
            builder.setYearRange(Constants.PICKER_MIN_YEAR, Constants.PICKER_MAX_YEAR)
                    .setMonthRange(Calendar.JANUARY, Calendar.DECEMBER)
                    .setOnYearChangedListener(v -> {})
                    .setOnMonthChangedListener(v -> {})
                    .setMonthNamesArray(R.array.month_names)
                    .setPositiveText(R.string.ok)
                    .setNegativeText(R.string.cancel)
                    .build().show();
        }catch(Exception e){

        }
    }
    /**
     * 리스트뷰 구분선에 margin을 주기위한 customDivider
     * */
    public static DividerItemDecoration setDivider(Context mContext) {
        int[] attrs = new int[]{android.R.attr.listDivider};

        TypedArray a = mContext.obtainStyledAttributes(attrs);
        Drawable divider = a.getDrawable(0);
        int inset = mContext.getResources().getDimensionPixelSize(R.dimen.layout_margin);
        InsetDrawable insetDivider = new InsetDrawable(divider, inset, 0, inset, 0);
        a.recycle();

        DividerItemDecoration itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(insetDivider);
        return itemDecoration;
    }

    /**
     * 휴대폰번호의 일정부분 가리기 ( ex: 010-****-**56 )
     * */
    public static String blindPhoneNumber(String number) {
        if (number == null || number.length() != 13) throw new IllegalArgumentException("Invalid phone number format");

        String blind = "-****-**";
        String firstPart = number.substring(0, 3);  // Extracts "010"
        String lastPart = number.substring(number.length() - 2);  // Extracts the last two digits

        return firstPart + blind + lastPart;
    }

    /**
    * 날짜 포맷 [ yyyy-MM-dd , HH:mm -> M월 d일 (*요일)\n HH시 ss분 ]
    * */
    public static String formatDate(String inputDate, String inputTime, boolean isDetail) {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        SimpleDateFormat outputFormat = new SimpleDateFormat("M월 d일 ", Locale.KOREA);

        SimpleDateFormat inputTimeFormat = new SimpleDateFormat("HH:mm", Locale.KOREA);
        SimpleDateFormat outputTimeFormat = new SimpleDateFormat("HH시 mm분", Locale.KOREA);

        try {
            Date date = inputDateFormat.parse(inputDate);
            Date time = inputTimeFormat.parse(inputTime);

            String formattedDate = "";

            String formattedTime = "";
            if (time != null) formattedTime = outputTimeFormat.format(time);

            int dayOfWeek = 0;
            if (date != null) {
                formattedDate = outputFormat.format(date);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            }

            String dayOfWeekStr = "";
            switch (dayOfWeek) {
                case Calendar.SUNDAY:
                    dayOfWeekStr = "일요일";
                    break;
                case Calendar.MONDAY:
                    dayOfWeekStr = "월요일";
                    break;
                case Calendar.TUESDAY:
                    dayOfWeekStr = "화요일";
                    break;
                case Calendar.WEDNESDAY:
                    dayOfWeekStr = "수요일";
                    break;
                case Calendar.THURSDAY:
                    dayOfWeekStr = "목요일";
                    break;
                case Calendar.FRIDAY:
                    dayOfWeekStr = "금요일";
                    break;
                case Calendar.SATURDAY:
                    dayOfWeekStr = "토요일";
                    break;
            }

            if (isDetail) return formattedDate + "(" + dayOfWeekStr + ")\n" + formattedTime;
            else {
                LogMgr.e("Event", formattedDate + "(" + dayOfWeekStr + ")\n" + inputTime);
                return formattedDate + "(" + dayOfWeekStr + ") " + inputTime;
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }
    /**
     * 숫자 천단위마다 콤마(,) 추가하는 메소드
     * */
    public static String decimalFormat(int s){ return new DecimalFormat("###,###").format(s); }

    public static void updateSpinnerList(PowerSpinnerView powerSpinner, List<String> newList) {
        powerSpinner.setItems(newList);
//        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        powerSpinner.measure(widthMeasureSpec, heightMeasureSpec);
//        int spinnerWidth = powerSpinner.getMeasuredWidth();

        PopupWindow popupWindow = powerSpinner.getSpinnerWindow();
        if (popupWindow != null) {

            // PopupWindow의 최소 넓이를 PowerSpinnerView의 넓이로 설정합니다.
//            popupWindow.getContentView().setMinimumWidth(spinnerWidth * 2);
        }
        powerSpinner.setSpinnerPopupHeight(ConstraintLayout.LayoutParams.WRAP_CONTENT);
    }

//    private static int calculatePopupMaxHeight(Context context, int itemCount) {
//        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
//        int screenHeight = displayMetrics.heightPixels;
//        int maxItemHeight = context.getResources().getDimensionPixelSize(R.dimen.spinner_item_height); // spinner에서 설정한 item 의 높이
//        int maxHeight = Math.min(screenHeight * 4 / 5, maxItemHeight * itemCount);
//        return maxHeight;
//    }
//    public static void updateSpinnerList(PowerSpinnerView powerSpinner) { powerSpinner.setSpinnerPopupHeight(ConstraintLayout.LayoutParams.WRAP_CONTENT); }
    /**
     * EditText 공백제거
     * */
    public static void removeSpace(EditText editText) {
        String textWithoutSpaces = editText.getText().toString().replace(" ", "");
        if (!editText.getText().toString().equals(textWithoutSpaces)) {
            editText.setText(textWithoutSpaces);
            editText.setSelection(textWithoutSpaces.length());
        }
    }
    /**
     * view의 위치를 이동시키는 애니메이션 메소드
     * */
    public static void animateLayoutMoveLeft(final View view, Context context) {
        // 현재 위치
        float startX = view.getX();
        // 왼쪽으로 이동한 후의 위치 (예: 왼쪽으로 100dp 이동)
        float endX = startX - context.getResources().getDimensionPixelSize(R.dimen.anim_move);

        // ObjectAnimator를 사용하여 X 좌표를 변경하는 애니메이션 생성
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", startX, endX);
        animator.setDuration(Constants.LAYOUT_ANIM_DURATION); // 애니메이션 지속 시간
        animator.setInterpolator(new AccelerateDecelerateInterpolator()); // 가속도 감속도 인터폴레이터 설정
        animator.start();
    }
    /**
     * 문자열이 null 또는 길이가 0인 경우, 빈 문자열로 대체하여 예외를 방지
     * */
    public static String getStr(String s){ return TextUtils.isEmpty(s) ? "" : s; }
    /**
     * 한글(자음,모음 비허용), 영어, 공백만 허용, 이외의 문자를 입력했는지 체크
     * */
    public static boolean nameCheck(String str){ return Pattern.compile("^[a-zA-Z가-힣\\s]+$").matcher(str).find(); }
}
