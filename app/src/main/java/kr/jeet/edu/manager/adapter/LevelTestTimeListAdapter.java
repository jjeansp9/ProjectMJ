package kr.jeet.edu.manager.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.LevelTestSettingActivity;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.model.data.LevelTestTimeData;
import kr.jeet.edu.manager.model.data.LevelTestTimeHeaderData;

public class LevelTestTimeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final int LAYOUT_HEADER = 0;
    private static final int LAYOUT_CONTENT = 1;
    SimpleDateFormat outputTimeFormat = new SimpleDateFormat(Constants.TIME_FORMATTER_A_HH_MM);
    public interface onItemClickListener{
        //        void onItemClick(RecipientData item);
        void onDeleteClick(int position, LevelTestSettingActivity.TimeListItem item);
    }
    private onItemClickListener listener;
    private LayoutInflater inflater;
    private Context context;
    private ArrayList<LevelTestSettingActivity.TimeListItem> timeList;
    public LevelTestTimeListAdapter(Context context, ArrayList<LevelTestSettingActivity.TimeListItem> list, onItemClickListener listener) {
        this.context = context;
        this.timeList = list;
        this.listener = listener;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        if(timeList.get(position).isHeader()) return LAYOUT_HEADER;
        else return LAYOUT_CONTENT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        if(viewType == LAYOUT_HEADER) {
            View view = inflater.inflate(R.layout.layout_time_header, parent, false);
            holder = new HeaderViewHolder(view);
        }else if(viewType == LAYOUT_CONTENT){
            View view = inflater.inflate(R.layout.layout_chip_item, parent, false);
            holder = new ContentViewHolder(view);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        LevelTestSettingActivity.TimeListItem item = timeList.get(position);
        if(holder == null) return;
        if(holder.getItemViewType() == LAYOUT_HEADER) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            LevelTestTimeHeaderData headerItem = (LevelTestTimeHeaderData) item;
            headerHolder.tvHeaderTitle.setText(headerItem.title);
        }else {
            ContentViewHolder contentHolder = (ContentViewHolder) holder;
            LevelTestTimeData contentData = (LevelTestTimeData) item;
            SimpleDateFormat inputDateFormat = new SimpleDateFormat(Constants.TIME_FORMATTER_HH_MM);
            try {
                contentData.testTime = inputDateFormat.parse(contentData.time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String timeStr = outputTimeFormat.format(contentData.testTime);
            Constants.WeekdayType type = Constants.WeekdayType.getByCode(contentData.weekend);
            String weekday = "";
            if(type != null){
                weekday = type.getNameKor();
            }
            String weekdayStr = String.format("%s %s", weekday, timeStr);
//            String timeStr = outputTimeFormat.format(contentData.time);

            SpannableString span = new SpannableString(weekdayStr);
            if(contentData.weekend == Constants.WeekdayType.WEEKEND.getCode()) {
                span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.saturday)), 0, weekday.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }else{
                span.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.font_color_666)), 0, weekday.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            span.setSpan(new StyleSpan(Typeface.BOLD), 0, span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            contentHolder.chip.setText(span);
        }
    }

    @Override
    public int getItemCount() {
        if(timeList == null) return 0;
        return timeList.size();
    }
    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeaderTitle;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeaderTitle = itemView.findViewById(R.id.tv_header_title);
        }
    }
    public class ContentViewHolder extends RecyclerView.ViewHolder{
        private ConstraintLayout rootLayout;
        private Chip chip;

        public ContentViewHolder(@NonNull View itemView){
            super(itemView);
            rootLayout = itemView.findViewById(R.id.root);
            chip = itemView.findViewById(R.id.chip);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getBindingAdapterPosition();
                    listener.onDeleteClick(position, timeList.get(position));
                }
            });

        }
    }
}
