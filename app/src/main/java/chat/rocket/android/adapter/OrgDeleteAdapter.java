package chat.rocket.android.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;



import java.util.ArrayList;

import chat.rocket.android.R;
import chat.rocket.android.activity.business.OrgDeleteUserActivity;
import chat.rocket.persistence.realm.models.ddp.UserEntity;

/**
 * Created by jumper on 2018/5/10
 */

public class OrgDeleteAdapter extends RecyclerView.Adapter {

    private OrgDeleteUserActivity mContext;
    private ArrayList<UserEntity> tempDataList;

    public OrgDeleteAdapter(OrgDeleteUserActivity activity, ArrayList<UserEntity> tempDataList) {
        this.mContext = activity;
        if (tempDataList == null) {
            tempDataList = new ArrayList<>();
        }
        this.tempDataList = tempDataList;
    }

    public  ArrayList<UserEntity> getData(){
        return tempDataList;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_delete_user, parent,
                false);
//        AutoUtils.auto(view);
        return new ContentVH(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ContentVH vh = (ContentVH) holder;
        UserEntity entity = tempDataList.get(position);
        vh.tvName.setText(entity.getRealName());
        vh.tv_company.setText(entity.getCompanyName());
        vh.tv_dept.setText(entity.getDept());
        vh.tv_zhiwei.setText(entity.getZhiWei());
        vh.iv_delete.setOnClickListener(view ->
                {
                    tempDataList.remove(position);
                    notifyDataSetChanged();
                    mContext.setTitle(tempDataList.size());
                }

        );
    }

    @Override
    public int getItemCount() {
        return tempDataList.size();
    }

    private class ContentVH extends RecyclerView.ViewHolder {
        TextView tvName, tv_company, tv_zhiwei, tv_dept;
        ImageView iv_icon, iv_delete;

        public ContentVH(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tv_company = itemView.findViewById(R.id.tv_company);
            tv_zhiwei = itemView.findViewById(R.id.tv_zhiwei);
            tv_dept = itemView.findViewById(R.id.tv_dept);
            iv_icon = itemView.findViewById(R.id.iv_icon);
            iv_delete = itemView.findViewById(R.id.delete);
        }
    }
}
