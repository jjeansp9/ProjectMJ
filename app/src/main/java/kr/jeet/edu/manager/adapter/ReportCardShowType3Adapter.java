package kr.jeet.edu.manager.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import kr.jeet.edu.manager.activity.menu.reportcard.ShowReportCardActivity;
import kr.jeet.edu.manager.common.Constants;
import kr.jeet.edu.manager.model.data.ReportCardExamData;
import kr.jeet.edu.manager.utils.Utils;
import kr.jeet.edu.manager.R;
import kr.jeet.edu.manager.model.data.ReportCardExamFooterData;

public class ReportCardShowType3Adapter extends AbstractReportCardShowAdapter  {

    private static final int VIEW_TYPE_MATH = 1;
    private static final int VIEW_TYPE_MATH_FOOTER = 2;

    private final int SET_CORNER_START_TOP = 0;
    private final int SET_CORNER_END_TOP = Constants.REPORT_MATH_SPAN_COUNT - 1;

    private LayoutInflater inflater;
    private Context mContext;
    private ArrayList<ShowReportCardActivity.ExamListTypeItem> examList;

    public ReportCardShowType3Adapter(Context mContext, ArrayList<ShowReportCardActivity.ExamListTypeItem> list) {
        this.mContext = mContext;
        this.examList = list;
        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        RecyclerView.ViewHolder holder = null;

        if(viewType == VIEW_TYPE_MATH) {
            View view = inflater.inflate(R.layout.layout_exam_type_3_content, parent, false);
            holder = new MathViewHolder(view);
        }else {
            View view = inflater.inflate(R.layout.layout_exam_type_3_footer, parent, false);
            holder = new FooterViewHolder(view);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position == NO_POSITION) return;
        ShowReportCardActivity.ExamListTypeItem item = examList.get(position);
        if(holder == null) return;

        if (item != null) {
            try {
                String str = "";
                if (holder.getItemViewType() == VIEW_TYPE_MATH) {
                    MathViewHolder mathVH = (MathViewHolder) holder;
                    ReportCardExamData contentItem = (ReportCardExamData) item;
                    str = String.valueOf(contentItem.esNum);
                    mathVH.tvNo.setText(TextUtils.isEmpty(str) ? "" : str.equals("0") ? "" : str);

                    str = Utils.getStr(contentItem.esScore);
                    if (!str.equals("O")) mathVH.tvScore.setTextColor(mContext.getColor(R.color.red));
                    mathVH.tvScore.setText(str);

                    if (position == SET_CORNER_START_TOP) mathVH.tvNo.setBackgroundResource(R.drawable.bg_border_pink_light_start_top);
                    else if (position == SET_CORNER_END_TOP) mathVH.tvNo.setBackgroundResource(R.drawable.bg_border_pink_light_end_top);
                } else {
                    FooterViewHolder bodyVH = (FooterViewHolder) holder;
                    ReportCardExamFooterData footerItem = (ReportCardExamFooterData) item;
                    str = footerItem.correctCount + " / " + footerItem.esNum;
                    bodyVH.tvCorrectCount.setText(Utils.getStr(str));
                    str = String.valueOf(footerItem.totalScore);
                    bodyVH.tvTotalScore.setText(Utils.getStr(str));

                }
            }catch (Exception e) {}
        }
    }

    @Override
    public int getItemCount() {
        if(examList == null) return 0;
        return examList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return examList.get(position).getType();
    }

    public class MathViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNo, tvScore;
        public MathViewHolder(@NonNull View itemView) {
            super(itemView);

            tvNo = itemView.findViewById(R.id.tv_report_math_no);
            tvScore = itemView.findViewById(R.id.tv_report_math_score);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {

        private TextView tvCorrectCount, tvTotalScore;

        public FooterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCorrectCount = itemView.findViewById(R.id.tv_correct_count);
            tvTotalScore = itemView.findViewById(R.id.tv_total_score);
        }
    }
}
