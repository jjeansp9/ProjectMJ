package kr.jeet.edu.manager.sns;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.auth.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.login.JoinActivity;
import kr.jeet.edu.manager.activity.login.LoginActivity;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.common.IntentParams;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.PreferenceUtil;
import kr.jeet.edu.manager.utils.Utils;

public class AppleLoginManager extends SNSLoginManager {
    private static final String TAG = "AppleLoginManager";

    private ComponentActivity mActivity;
    private OAuthProvider.Builder provider;
    private FirebaseAuth mAuth;
    private AlertDialog mProgressDialog = null;

    public AppleLoginManager(AppCompatActivity activity) {
        super(activity);
        this.mActivity = activity;
        initAuth();
    }

    private void initAuth(){
        if (provider == null){
            provider = OAuthProvider.newBuilder("apple.com");
            List<String> scopes = new ArrayList<>();
            scopes.add("email");
            scopes.add("name");
            provider.setScopes(scopes);
            provider.addCustomParameter("locale", Constants.LOCALE_LANGUAGE_KR); // 한국어로 설정
        }
    }

    @Override
    public void LoginProcess() {
        showProgressDialog();
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();

            Task<AuthResult> pending = mAuth.getPendingAuthResult();
            if (pending != null) {
                pending.addOnSuccessListener(this::actionAfterSuccess)
                        .addOnFailureListener( error -> {
                            LogMgr.e(TAG, error.getMessage());
                            mActivity.startActivity(new Intent(mActivity, LoginActivity.class));
                            ((Activity)mActivity).finish();
                            hideProgressDialog();
                        });
            } else {
                startAppleActivity();
            }
        }
    }

    private void startAppleActivity(){
        if (mAuth != null && provider != null){
            mAuth.startActivityForSignInWithProvider(mActivity, provider.build())
                    .addOnSuccessListener(this::actionAfterSuccess)
                    .addOnFailureListener( error -> {
                        LogMgr.e(TAG, error.getMessage());
                        mActivity.startActivity(new Intent(mActivity, LoginActivity.class));
                        ((Activity)mActivity).finish();
                        hideProgressDialog();
                    });
        }
    }

    @Override
    public void LogoutProcess() {

    }

    @Override
    public void DeleteAccountProcess() {
        if (mAuth != null){
            mAuth.signOut();
        }
    }

    private void actionAfterSuccess(AuthResult authResult){
        FirebaseUser user = authResult.getUser();
        //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null){
            String name = "";
            String email = "";
            String uId = "";

            for (UserInfo profile : user.getProviderData()) {
                String getProviderId = profile.getProviderId();
                String getName = profile.getDisplayName();
                String getUId = profile.getUid();

                if ("apple.com".equals(getProviderId)) {
                    name = getName;
                    uId = getUId;
                    LogMgr.i(
                            TAG, "apple Info: " +
                                    "\n name: " + name +
                                    "\n email: " + email +
                                    "\n providerId: " + getProviderId +
                                    "\n uid: " + uId
                    );
                }
            }

//                            for (UserInfo profile : user.getProviderData()) {
//                                // Id of the provider (ex: google.com)
//                                String providerId = profile.getProviderId();
//
//                                // UID specific to the provider
//                                String uid = profile.getUid();
//
//                                // Name, email address
//                                String name = profile.getDisplayName();
//                                String email = profile.getEmail();
//
//                                LogMgr.i(
//                                        TAG, "apple Info: " +
//                                                "\n name: " + name +
//                                                "\n email: " + email +
//                                                "\n providerId: " + providerId +
//                                                "\n uid: " + uid
//                                );
//                            }

            PreferenceUtil.setSNSUserId(mActivity, uId);
            PreferenceUtil.setLoginType(mActivity, Constants.LOGIN_TYPE_SNS_APPLE);

            if (mIsJoinStatus){
                Intent intent = new Intent(mActivity, JoinActivity.class);
                intent.putExtra(IntentParams.PARAM_LOGIN_TYPE, Constants.LOGIN_TYPE_SNS_APPLE);
                intent.putExtra(IntentParams.PARAM_LOGIN_USER_NAME, name);
                intent.putExtra(IntentParams.PARAM_LOGIN_USER_SNSID, uId);
                mActivity.startActivity(intent);
                mActivity.finish();
            }else{
                LogMgr.e("login complete -> ");
                if(mHandler != null) {
                    Bundle data = new Bundle();
                    data.putString("name", name);
                    data.putInt("loginType", Constants.LOGIN_TYPE_SNS_APPLE);

                    Message msg = Message.obtain();
                    msg.what = Constants.HANDLER_SNS_LOGIN_COMPLETE;
                    msg.obj = uId;
                    msg.setData(data);
                    mHandler.sendMessage(msg);
                }
            }
        }else{
            LogMgr.e(TAG, "apple Login user info is null");
        }
        hideProgressDialog();
    }

    private void showProgressDialog() {
        if (mProgressDialog == null){
            View view = mActivity.getLayoutInflater().inflate(R.layout.dialog_progressbar, null, false);
            TextView txt = view.findViewById(R.id.text);
            txt.setText(mActivity.getString(R.string.requesting));

            mProgressDialog = new AlertDialog.Builder(mActivity)
                    .setCancelable(false)
                    .setView(view)
                    .create();
            mProgressDialog.show();
        }
    }

    private void hideProgressDialog() {
        try {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }catch (Exception e){
            LogMgr.e("hideProgressDialog()", e.getMessage());
        }
    }
}
