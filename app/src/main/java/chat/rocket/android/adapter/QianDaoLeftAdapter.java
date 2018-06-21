package chat.rocket.android.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.entity.QianDaoAttendance;
import chat.rocket.android.helper.TextUtils;

/**
 * Created by helloworld on 2018/3/30
 */

public class QianDaoLeftAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<QianDaoAttendance> attendanceList;

    public QianDaoLeftAdapter(Context mContext, List<QianDaoAttendance> attendanceList) {
        this.mContext = mContext;
        this.attendanceList = attendanceList;
        if(this.attendanceList == null){
            this.attendanceList = new ArrayList<>();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.qiandao_item, null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyViewHolder vh = (MyViewHolder) holder;

        QianDaoAttendance attendance = attendanceList.get(position);
        if(attendance == null) return;
        vh.tv_name.setText(TextUtils.splitUsername(attendance.username));
        RequestOptions options = new RequestOptions().placeholder(R.drawable.default_hd_avatar)
                .error(R.drawable.default_hd_avatar);
        Glide.with(RocketChatApplication.getInstance())
                .load(attendance.avatar)
                .apply(options)
                .into(vh.iv_icon);
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public void setData(List<QianDaoAttendance> noSignAttendanceList) {
        attendanceList = noSignAttendanceList;
        if(this.attendanceList == null){
            this.attendanceList = new ArrayList<>();
        }
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView iv_icon;
        private TextView tv_name;
        public MyViewHolder(View view) {
            super(view);
            tv_name =  view.findViewById(R.id.tv_name);
            iv_icon =  view.findViewById(R.id.iv_icon);
        }
    }

}
