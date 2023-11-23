package kr.jeet.edu.manager.common;

import android.content.Context;

import androidx.recyclerview.widget.GridLayoutManager;

public class CustomGridLayoutMgr extends GridLayoutManager {
    public CustomGridLayoutMgr(Context context, int spanCount) {
        super(context, spanCount);
        setSpanSizeLookup(new SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // 아이템 개수 가져오기
                int totalItemCount = getItemCount();
                // 마지막 두 아이템인지 확인
                if (position >= totalItemCount - 1) {
                    return getSpanCount(); // 나머지 아이템은 설정한 span 사용
                } else {
                    return 1; // 마지막 하나의 아이템은 전체 span 사용
                }
            }
        });
    }

}
