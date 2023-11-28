package kr.jeet.edu.manager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.activity.menu.reportcard.ShowReportCardActivity;

import kr.jeet.edu.manager.model.data.ReportCardExamData;
import kr.jeet.edu.manager.model.data.ReportCardExamFooterData;
import kr.jeet.edu.manager.model.data.ReportCardExamHeaderData;

public class ReportCardShowType0Adapter extends AbstractReportCardShowAdapter  {

    public static final int LAYOUT_HEADER = 0;
    public static final int LAYOUT_CONTENT = 1;
    public static final int LAYOUT_FOOTER = 2;

    private LayoutInflater inflater;
    private Context context;
    private ArrayList<ShowReportCardActivity.ExamListTypeItem> examList;
    public ReportCardShowType0Adapter(Context context, ArrayList<ShowReportCardActivity.ExamListTypeItem> list) {
        this.context = context;
        this.examList = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType(int position) {
        return examList.get(position).getType();

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        if(viewType == LAYOUT_HEADER) {
            View view = inflater.inflate(R.layout.layout_exam_type_0_header, parent, false);
            holder = new HeaderViewHolder(view);
        }else if(viewType == LAYOUT_CONTENT){
            View view = inflater.inflate(R.layout.layout_exam_type_0_content, parent, false);
            holder = new ContentViewHolder(view);
        }else if(viewType == LAYOUT_FOOTER) {
            View view = inflater.inflate(R.layout.layout_exam_type_0_footer, parent, false);
            holder = new FooterViewHolder(view);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ShowReportCardActivity.ExamListTypeItem item = examList.get(position);
        if(holder == null) return;
        if(holder.getItemViewType() == LAYOUT_HEADER) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            ReportCardExamHeaderData headerItem = (ReportCardExamHeaderData) item;
            if(position == 0) {
                headerHolder.rootView.setBackground(context.getDrawable(R.drawable.bg_card_header_round));
            }else{
                headerHolder.rootView.setBackground(context.getDrawable(R.drawable.bg_card_header_edge));
            }
            headerHolder.tvHeaderTitle.setText(headerItem.esTitle);
        }else if(holder.getItemViewType() == LAYOUT_CONTENT) {
            ContentViewHolder contentHolder = (ContentViewHolder) holder;
            ReportCardExamData contentItem = (ReportCardExamData) item;
            contentHolder.tvIndex.setText(String.valueOf(contentItem.esNum));
            contentHolder.tvSubject.setText(contentItem.esName);
            contentHolder.tvScore.setText(contentItem.esScore);
        }else if(holder.getItemViewType() == LAYOUT_FOOTER) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            ReportCardExamFooterData footerItem = (ReportCardExamFooterData) item;
            footerViewHolder.tvTitleTotalScore.setText(context.getString(R.string.title_exam_sub_total_score, footerItem.esTitle));
            footerViewHolder.tvTotalScore.setText(String.valueOf(footerItem.totalScore));
            String rateString = "";
            try {
                double score = Double.parseDouble(footerItem.correctRate);
                if (score % 1 == 0) {
                    rateString = String.format("%.0f", score);
                } else {
                    rateString = String.valueOf(score);
                }
            }catch(Exception ex){}
            footerViewHolder.tvTotalRate.setText(rateString);
        }
    }

    @Override
    public int getItemCount() {
        if(examList == null) return 0;
        return examList.size();
    }
    class HeaderViewHolder extends RecyclerView.ViewHolder {
        View rootView;
        TextView tvHeaderTitle;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeaderTitle = itemView.findViewById(R.id.tv_title);
            rootView = itemView;
        }
    }
    public class ContentViewHolder extends RecyclerView.ViewHolder{
        TextView tvIndex, tvSubject, tvScore;
        public ContentViewHolder(@NonNull View itemView){
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tv_sub_index);
            tvSubject = itemView.findViewById(R.id.tv_sub_subject);
            tvScore = itemView.findViewById(R.id.tv_sub_score);

        }
    }
    class FooterViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitleTotalScore, tvTotalScore, tvTotalRate;
        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitleTotalScore = itemView.findViewById(R.id.tv_title_total_score);
            tvTotalScore = itemView.findViewById(R.id.tv_total_score);
            tvTotalRate = itemView.findViewById(R.id.tv_total_rate);
        }
    }
}
