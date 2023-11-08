package kr.jeet.edu.manager.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import kr.jeet.edu.manager.R;

public class SendSMSDialog extends Dialog {
    private Context context;
    private TextView tvTitle, tvRecipientCount, tvSMSLength;
    private EditText editTextContent;
    private Button cancelBtn, okBtn;
    //checkbox

    private ViewGroup titleLayout;
    public interface UpdateContentInterface {
        void onUpdatedContents(String content);
    }
    UpdateContentInterface _updateContentlistener;
    public SendSMSDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public SendSMSDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
        initView();
    }

    protected SendSMSDialog(@NonNull Context context, boolean cancelable, @Nullable DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
        initView();
    }

    private void initView() {
        this.setCanceledOnTouchOutside(false);
        this.setCancelable(false);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.layout_send_sms_dialog);

        // 버튼 테두리 라운드 적용
        findViewById(R.id.dialog_ly).setClipToOutline(true);

        titleLayout = findViewById(R.id.title_ly);
        tvTitle = (TextView) findViewById(R.id.title);
        tvRecipientCount = (TextView) findViewById(R.id.tv_recipient_count);
        tvSMSLength = (TextView) findViewById(R.id.tv_sms_length);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);
        okBtn = (Button) findViewById(R.id.okBtn);
        editTextContent = (EditText) findViewById(R.id.edit);
        editTextContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!TextUtils.isEmpty(charSequence)) {
                    okBtn.setEnabled(true);
                }else {
                    okBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        titleLayout.setVisibility(View.GONE);
    }

    public void setTitle(String str) {
        if(!TextUtils.isEmpty(str)) {
            tvTitle.setText(str);
            titleLayout.setVisibility(View.VISIBLE);
        } else {
            titleLayout.setVisibility(View.GONE);
        }
    }

    public void setEditText(String content) {
//        if(editText.getVisibility() == View.VISIBLE) {
        editTextContent.setText(content);
        editTextContent.requestFocus();
//        }
    }
    public void setRecipientCount(int count) {
        tvRecipientCount.setText(context.getString(R.string.content_recipient_counts, count));
    }

    public void setOkButtonText(String str) {
        okBtn.setText(str);
    }

    public void setOnOkButtonClickListener(View.OnClickListener listener) {
        okBtn.setOnClickListener(listener);
    }

    public void setOnCancelButtonClickListener(View.OnClickListener listener) {
        cancelBtn.setVisibility(View.VISIBLE);
        cancelBtn.setOnClickListener(listener);
    }
    public void setUpdatedContentListener(UpdateContentInterface listener) {
        _updateContentlistener = listener;
    }

    @Override
    public void dismiss() {
        _updateContentlistener.onUpdatedContents(editTextContent.getText().toString());
        super.dismiss();
    }
}
