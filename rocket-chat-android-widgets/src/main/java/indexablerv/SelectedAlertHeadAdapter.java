package indexablerv;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.android.widget.R;

/**
 * Created by lyq on 2018/5/29.
 */

public class SelectedAlertHeadAdapter extends IndexableHeaderAdapter {

    public SelectedAlertHeadAdapter(String index, String indexTitle, List datas) {
        super(index, indexTitle, datas);
    }

    @Override
    public int getItemViewType() {
        return 100;
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_alert_head, parent, false);
        return new  HeaderViewHolder(inflate);
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, Object entity) {
        HeaderViewHolder vh = (HeaderViewHolder) holder;
      String data = (String)entity;
        if(data.equals("所有成员")){
            vh.icon.setImageResource(R.drawable.meeting_icon);
            vh.line.setVisibility(View.VISIBLE);
        }else {
            vh.icon.setImageResource(R.drawable.online_user_icon);
            vh.line.setVisibility(View.GONE);
        }
        vh.name.setText(data);
    }
    class HeaderViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView name;
        private View line;
        public HeaderViewHolder(View itemView) {
            super(itemView);
            icon=itemView.findViewById(R.id.iv_icon);
            name=itemView.findViewById(R.id.tv_name);
            line=itemView.findViewById(R.id.line);
        }
    }
}
