package chat.rocket.android.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.zhy.autolayout.AutoLinearLayout;


import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.activity.business.WebViewActivity;
import chat.rocket.core.models.Card;
import chat.rocket.core.models.Report;
import chat.rocket.core.models.ReportList;

/**
 * created by jumper 2018/04/10
 */
public class RocketChatMessageReportLayout extends AutoLinearLayout {
    private LayoutInflater inflater;

    public RocketChatMessageReportLayout(Context context) {
        super(context);
        initialize(context, null);
    }

    public RocketChatMessageReportLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public RocketChatMessageReportLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RocketChatMessageReportLayout(Context context, AttributeSet attrs, int defStyleAttr,
                                         int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        inflater = LayoutInflater.from(context);
        setOrientation(VERTICAL);
    }

    public void setAttachments(Card card, Report report) {
        if (card==null&&report==null)
            return;
        removeAllViews();
        View reportView = inflater.inflate(R.layout.message_report, this, false);
        showCardView(card, reportView);
        showReportView(report, reportView);
        addView(reportView);
    }

    private void showReportView(Report report, View reportView){
        final View cardView = reportView.findViewById(R.id.ll_report);
        if (report==null) {
            cardView.setVisibility(GONE);
            return;
        }
        cardView.setVisibility(VISIBLE);
        TextView tv_link_title=cardView.findViewById(R.id.tv_link_title);
        TextView tv_link=cardView.findViewById(R.id.tv_link);
        tv_link_title.setText(report.getTheme());
        List<ReportList> reportLists= report.getReportList();
        if (reportLists!=null&&reportLists.size()>0){
            LinearLayout ll_cardView = reportView.findViewById(R.id.ll_report);
            for (int i = 0; i < reportLists.size(); i++) {
                TextView textView=new TextView(getContext());
                textView.setTextColor(getResources().getColor(R.color.color_39c3fa));
                textView.setText(report.getReportList().get(0).getTitle());
                ll_cardView.addView(textView);
                textView.setOnClickListener(view -> {
                    Intent intent = new Intent(RocketChatApplication.getInstance(), WebViewActivity.class);
                    intent.putExtra("link",report.getReportList().get(0).getLink());
                    intent.putExtra("title",report.getReportList().get(0).getTitle());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    RocketChatApplication.getInstance().startActivity(intent);
                }
                );
            }

        }

    }

    private void showCardView(Card card, View reportView) {
        final View cardView = reportView.findViewById(R.id.fl_card);
        if (card==null) {
            cardView.setVisibility(GONE);
            return;
        }
        cardView.setVisibility(VISIBLE);
        TextView title=cardView.findViewById(R.id.title);
        TextView subTitle=cardView.findViewById(R.id.subtitle);
        TextView sender=cardView.findViewById(R.id.sender);
        ImageView logo=cardView.findViewById(R.id.logo);
        title.setText(card.getTitle());
        subTitle.setText(card.getContent());
        sender.setText(card.getSender());
        RequestOptions error = new RequestOptions().placeholder(R.drawable.weining_icon)
                .error(R.drawable.weining_icon);
        Glide.with(getContext()).load(card.getLogo()).apply(error).into(logo);
        cardView.setOnClickListener(view -> {
            Intent intent = new Intent(RocketChatApplication.getInstance(), WebViewActivity.class);
            intent.putExtra("link",card.getUrl());
            intent.putExtra("title",card.getTitle());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            RocketChatApplication.getInstance().startActivity(intent);
        });
    }

}
