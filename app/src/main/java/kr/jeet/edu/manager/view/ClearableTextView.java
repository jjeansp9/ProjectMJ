package kr.jeet.edu.manager.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.utils.LogMgr;

public class ClearableTextView extends LinearLayoutCompat implements View.OnClickListener{
    private static final String TAG = "deletetextview";
    LinearLayoutCompat layoutRoot;
    View btnClose;
    TextView tvContent;
    onTextViewClickListener _clickListener;

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.tv_content:
                LogMgr.e(TAG, "content clicked");
                _clickListener.onContentClick();
                break;
            case R.id.view_close:
                LogMgr.e(TAG, "close clicked");
                _clickListener.onDeleteClick();
                break;
        }
    }

    public interface onTextViewClickListener{
        void onContentClick();
        void onDeleteClick();
    }
    public ClearableTextView(@NonNull Context context) {
        super(context);
        initView();
    }

    public ClearableTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
        getAttrs(attrs);
    }

    public ClearableTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        getAttrs(attrs);
    }
    public void setCloseClickListener(onTextViewClickListener listener) {
        _clickListener = listener;
//        if(btnClose != null) {
//            btnClose.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    _deleteListener.onDeleteClick();
//                }
//            });
//        }
    }
    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_clearable_textview, this, false);
        addView(view);
        layoutRoot = findViewById(R.id.root);
        btnClose = findViewById(R.id.view_close);
        btnClose.setVisibility(View.GONE);
        btnClose.setOnClickListener(this);
        tvContent = findViewById(R.id.tv_content);
        tvContent.setOnClickListener(this);
    }
    private void getAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.DeletableTextView);
        setTypeArray(typedArray);
    }
    private void getAttrs(AttributeSet attrs, int defStyle) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.DeletableTextView, defStyle, 0);
        setTypeArray(typedArray);
    }
    private void setTypeArray(TypedArray typedArray) {
        String text = typedArray.getString(R.styleable.DeletableTextView_android_text);
        setText(text);
        String hint = typedArray.getString(R.styleable.DeletableTextView_android_hint);
        tvContent.setHint(hint);
    }
    public void setText(String text) {
        tvContent.setText(text);
        if(TextUtils.isEmpty(text)) {
            btnClose.setVisibility(View.GONE);
            tvContent.setCompoundDrawablesWithIntrinsicBounds(0, 0,  R.drawable.ic_vector_school, 0);
        }else{
            btnClose.setVisibility(View.VISIBLE);
            tvContent.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }
}
