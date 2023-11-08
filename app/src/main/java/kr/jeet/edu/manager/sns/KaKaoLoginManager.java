package kr.jeet.edu.manager.sns;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Message;

import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.Gender;
import com.kakao.sdk.user.model.User;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.JoinActivity;
import kr.jeet.edu.manager.activity.LoginActivity;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;

public class KaKaoLoginManager extends SNSLoginManager {
    private static final String TAG = KaKaoLoginManager.class.getSimpleName();

    private Context mContext;

    public KaKaoLoginManager(Context context) {
        super(context);
        this.mContext = context;
        KakaoSdk.init(mContext,mContext.getString(R.string.kakao_native_app_key));
    }

    @Override
    public void LoginProcess() {
        try {
            // 해당 기기에 카카오톡이 설치되어 있는 확인
            if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(mContext)){
                UserApiClient.getInstance().loginWithKakaoTalk(mContext, kakaoTalkCallback);
            }else{
                // 카카오톡이 설치되어 있지 않다면
                UserApiClient.getInstance().loginWithKakaoAccount(mContext, kakaoTalkCallback);
            }
        }
        catch (Exception ex) {
            LogMgr.e(TAG, ex.getMessage());
        }
    }

    @Override
    public void LogoutProcess() {
        try {
            UserApiClient.getInstance().logout(new Function1<Throwable, Unit>() {
                @Override
                public Unit invoke(Throwable throwable) {
                    LogMgr.e(TAG,"kakao Talk logout");
                    //updateKakaoLoginUi();
                    return null;
                }
            });
        }
        catch (Exception ex) {
            LogMgr.e(TAG, ex.getMessage());
        }
    }

    @Override
    public void DeleteAccountProcess() { // 회원탈퇴
        try{
            UserApiClient.getInstance().unlink(new Function1<Throwable, Unit>() {
                @Override
                public Unit invoke(Throwable throwable) {
                    if (throwable == null){
                        LogMgr.e(TAG, "Kakao Account Delete Success");
                    }else{
                        LogMgr.e(TAG, "Kakao Account Delete Fail");
                    }
                    return null;
                }
            });
        }catch (Exception e){

        }
    }

    Function2<OAuthToken,Throwable, Unit> kakaoTalkCallback =new Function2<OAuthToken, Throwable, Unit>() {
        @Override
        // 콜백 메서드 ,
        public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
            LogMgr.e(TAG,"CallBack Method");
            //oAuthToken != null 이라면 로그인 성공
            if(throwable != null) {
                LogMgr.e(TAG,throwable.getLocalizedMessage());
            } else if(oAuthToken!=null){
                // 토큰이 전달된다면 로그인이 성공한 것이고 토큰이 전달되지 않으면 로그인 실패한다.
                updateKakaoLoginUi();

            }else {
                //로그인 실패
                LogMgr.e(TAG, "invoke: login fail" );
                mContext.startActivity(new Intent(mContext, LoginActivity.class));
                ((Activity)mContext).finish();
            }

            return null;
        }
    };

    private void updateKakaoLoginUi() {
        // 로그인 여부에 따른 UI 설정
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null) {
                    String userId = String.valueOf(user.getId());
                    String name = user.getKakaoAccount().getProfile().getNickname();
                    String gender = "";
                    Gender userGender = user.getKakaoAccount().getGender();
                    if (userGender != null) gender = userGender.toString();

                    LogMgr.e(TAG, "userId = " + userId);
                    LogMgr.e(TAG, "gender = " + gender);
                    LogMgr.e(TAG, "name = " + name);

                    if(gender.equals("MALE")) {
                        gender = "M";
                    }
                    else {
                        gender = "F";
                    }

                    PreferenceUtil.setSNSUserId(mContext, userId);
                    PreferenceUtil.setLoginType(mContext, Constants.LOGIN_TYPE_SNS_KAKAO);

                    if(mIsJoinStatus == true) //회원가입 모드
                    {
                        Intent intent = new Intent(mContext, JoinActivity.class);
                        intent.putExtra(IntentParams.PARAM_LOGIN_TYPE, Constants.LOGIN_TYPE_SNS_KAKAO);
                        intent.putExtra(IntentParams.PARAM_LOGIN_USER_NAME, name);
                        intent.putExtra(IntentParams.PARAM_LOGIN_USER_GENDER, gender);
                        intent.putExtra(IntentParams.PARAM_LOGIN_USER_SNSID, userId);
                        mContext.startActivity(intent);
                        ((Activity)mContext).finish();
                    }
                    else
                    {
                        //server login api send
                        LogMgr.e("login complete -> ");
                        if(mHandler != null) {
                            Message msg = Message.obtain();
                            msg.what = Constants.HANDLER_SNS_LOGIN_COMPLETE;
                            msg.obj = userId;
                            mHandler.sendMessage(msg);
                        }
                    }

                } else {
                    // 로그인 되어있지 않으면
                }
                return null;
            }
        });
    }

}

