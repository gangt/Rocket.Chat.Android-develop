package chat.rocket.android.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.persistence.realm.models.ddp.UserEntity;
import indexablerv.IndexableAdapter;

/**
 * Created by helloworld on 2018/2/3
 */

public class DeleteUserAdapter extends IndexableAdapter<UserEntity> {

    private LayoutInflater mInflater;
    private ArrayList<UserEntity> selectList;

    public DeleteUserAdapter(Activity activity) {
        super();
        mInflater = LayoutInflater.from(activity);
        if(selectList == null){
            selectList = new ArrayList<>();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateTitleViewHolder(ViewGroup parent) {
        View view = mInflater.inflate(R.layout.item_index_contact, parent, false);
        return new IndexVH(view);
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent) {
        View view = mInflater.inflate(R.layout.item_contact, parent, false);
        return new ContentVH(view);
    }

    @Override
    public void onBindTitleViewHolder(RecyclerView.ViewHolder holder, String indexTitle) {
        IndexVH vh = (IndexVH) holder;
        vh.tv.setText(indexTitle);
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, final UserEntity entity) {
        ContentVH vh = (ContentVH) holder;
        vh.tvName.setText(entity.getRealName());
        vh.cb_check.setChecked(selectList.contains(entity));
        vh.tv_company.setText(entity.getCompanyName());
        vh.tv_dept.setText(entity.getDept());
        String zhiWei = entity.getZhiWei();
        if(zhiWei != null && zhiWei.contains("_")){
            zhiWei = zhiWei.split("_")[1];
        }
        vh.tv_zhiwei.setText(zhiWei);
        Glide.with(RocketChatApplication.getInstance()).load(entity.getAvatar()).into(vh.iv_icon);
        vh.ll_check.setOnClickListener(v->{
            if (!selectList.contains(entity)) {
                vh.cb_check.setChecked(true);
                selectList.add(entity);
            }else{
                vh.cb_check.setChecked(false);
                selectList.remove(entity);
            }
        });
    }

    public ArrayList<UserEntity> getSelectList(){
        return selectList;
    }

    private class IndexVH extends RecyclerView.ViewHolder {
        TextView tv;

        public IndexVH(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tv_index);
        }
    }

    private class ContentVH extends RecyclerView.ViewHolder {
        TextView tvName,tv_company,tv_zhiwei,tv_dept;
        CheckBox cb_check;
        ImageView iv_icon;
        LinearLayout ll_check;

        public ContentVH(View itemView) {
            super(itemView);
            tvName =  itemView.findViewById(R.id.tv_name);
            tv_company =  itemView.findViewById(R.id.tv_company);
            tv_zhiwei =  itemView.findViewById(R.id.tv_zhiwei);
            tv_dept =  itemView.findViewById(R.id.tv_dept);
            cb_check = itemView.findViewById(R.id.cb_check);
            ll_check = itemView.findViewById(R.id.ll_check);
            iv_icon =  itemView.findViewById(R.id.iv_icon);
        }
    }
}
