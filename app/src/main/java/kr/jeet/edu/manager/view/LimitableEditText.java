package kr.jeet.edu.manager.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.utils.LogMgr;
import kr.jeet.edu.manager.utils.Utils;

public class LimitableEditText extends ConstraintLayout implements TextWatcher {
    private static final String TAG = "limitEditText";
    private static final int DEFAULT_MAX_LENGTH = 2000;
    private Context _context;
//    private Drawable clearDrawable;
    View rootView;
    EditText editText;
    TextView tvCount;
    int maxLength = DEFAULT_MAX_LENGTH;

    public LimitableEditText(final Context context) {
        super(context);
        this._context = context;
        init();
    }

    public LimitableEditText(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this._context = context;
        init();
        getAttrs(attrs);

    }

    public LimitableEditText(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this._context = context;
        init();
        getAttrs(attrs, defStyleAttr);

    }
    private void getAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LimitableEditText);
        setTypeArray(typedArray);
    }
    private void getAttrs(AttributeSet attrs, int defStyle) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.LimitableEditText, defStyle, 0);
        setTypeArray(typedArray);
    }
    private void setTypeArray(TypedArray typedArray) {
        maxLength = typedArray.getInt(R.styleable.LimitableEditText_android_maxLength, DEFAULT_MAX_LENGTH);
        int minLines = typedArray.getInt(R.styleable.LimitableEditText_android_minLines, 1);
        int maxLines = typedArray.getInt(R.styleable.LimitableEditText_android_maxLines, 0);
        String hint = typedArray.getString(R.styleable.DeletableTextView_android_hint);
//        float textSize = typedArray.getDimension(R.styleable.LimitableEditText_android_textSize, 0);
        float textSizeInPixels = typedArray.getDimensionPixelSize(R.styleable.LimitableEditText_android_textSize, 0);
        int textColor = typedArray.getColor(R.styleable.LimitableEditText_android_textColor, _context.getColor(R.color.font_color_default));
        editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
        editText.setMinLines(minLines);
        if(maxLines > 0) {
            editText.setMaxLines(maxLines);
        }
        editText.setHint(hint);
        LogMgr.w(TAG, "font textSizeInPixels = " + textSizeInPixels);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPixels);
        editText.setTextColor(textColor);
        setTextCount(0);
    }
    public void setMaxLength(int length) {
        maxLength = length;
        editText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
        setTextCount(0);
    }
    private void init() {
        LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_limitable_edittext, this, true);
        rootView = view.findViewById(R.id.root_view_limitable_edittext);
        editText = view.findViewById(R.id.edittext_limitable_edittext);
        tvCount = view.findViewById(R.id.tv_count_limitable_edittext);
        editText.addTextChangedListener(this);
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                updateBackground(hasFocus);
            }
        });
    }
    private void updateBackground(boolean hasFocus) {
        Drawable background;
        if (hasFocus) {
            background = getResources().getDrawable(R.drawable.bg_edittext_focused, null);
        } else {
            background = getResources().getDrawable(R.drawable.bg_edittext_default, null);
        }

        // 배경 적용
        rootView.setBackground(background);
    }
    private void setTextCount(int count) {
        tvCount.setText(String.format("%d / %d", count, maxLength));
    }
    public void setText(String str) {
        editText.setText(str);
        setTextCount(str.length());
    }
    public String getText() {
        if(editText.getText() == null) return "";
        return editText.getText().toString();
    }
    public EditText getEditText() {
        return editText;
    }
    @Override
    public final void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
        if (editText.isFocused()) {
            setTextCount(s.length());
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.rootView.setEnabled(enabled);
        this.editText.setEnabled(enabled);
    }
}
