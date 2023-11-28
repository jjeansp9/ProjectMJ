package kr.jeet.edu.manager.sns;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.os.Message;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import kr.jeet.edu.manager.activity.login.JoinActivity;
import kr.jeet.edu.manager.activity.login.LoginActivity;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;

public class GoogleLoginManager extends SNSLoginManager {
    private static final String TAG = GoogleLoginManager.class.getSimpleName();

    private ComponentActivity mActivity;

    GoogleSignInClient mGoogleSignInClient = null;

    ActivityResultLauncher<Intent> resultLauncher;

    public GoogleLoginManager(AppCompatActivity activity) {
        super(activity);
        this.mActivity = activity;

       resultLauncher = mActivity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    LogMgr.e("로그인 성공");
                    if (result.getData() != null){
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    }

                }else{
                    mActivity.startActivity(new Intent(mActivity, LoginActivity.class));
                    ((Activity)mActivity).finish();
                    LogMgr.e("로그인 실패 : " + result.getResultCode());
                }
            }
        });
    }

    @Override
    public void LoginProcess() {
        try{
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(mActivity, gso);

            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            resultLauncher.launch(signInIntent);
        }catch (Exception e){
            LogMgr.e(TAG, e.getMessage());
            mActivity.finish();
        }
    }

    @Override
    public void LogoutProcess() {
        try{
            mGoogleSignInClient.signOut().addOnCompleteListener(mActivity, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    LogMgr.e(TAG,"Google Logout Complete");
                }
            });
        }catch (Exception e){
            LogMgr.e(TAG, e.getMessage());
        }
    }

    @Override
    public void DeleteAccountProcess() { // 회원탈퇴
        try{
            if (mGoogleSignInClient != null){
                mGoogleSignInClient.revokeAccess().addOnCompleteListener(mActivity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        LogMgr.e(TAG,"Google Delete Account Complete");
                    }
                });
            }else{
                LogMgr.e(TAG,"GoogleSignInClient is null");
            }
        }catch (Exception e){

        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String userId = account.getId();
            String name = account.getDisplayName();

            LogMgr.e(TAG, "Google userId : " + userId);
            LogMgr.e(TAG, "Google name : " + name);

            PreferenceUtil.setSNSUserId(mActivity, userId);
            PreferenceUtil.setLoginType(mActivity, Constants.LOGIN_TYPE_SNS_GOOGLE);

            if (mIsJoinStatus){ // 회원가입 모드
                Intent intent = new Intent(mActivity, JoinActivity.class);
                intent.putExtra(IntentParams.PARAM_LOGIN_TYPE, Constants.LOGIN_TYPE_SNS_GOOGLE);
                intent.putExtra(IntentParams.PARAM_LOGIN_USER_NAME, name);
                intent.putExtra(IntentParams.PARAM_LOGIN_USER_SNSID, userId);
                mActivity.startActivity(intent);
                mActivity.finish();

            }else{ // 로그인 모드
                if (mHandler != null){
                    Message msg = Message.obtain();
                    msg.what = Constants.HANDLER_SNS_LOGIN_COMPLETE;
                    msg.obj = userId;
                    mHandler.sendMessage(msg);
                }
            }

        } catch (ApiException e) {
        }
    }
}