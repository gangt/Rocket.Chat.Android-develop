package chat.rocket.android.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.recyclertreeview.selectuserbinder.OrgMember;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;

/**
 * Created by helloworld on 2018/4/11
 */

public class OrgMemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<OrgMember> allDataList;
    private Context mContext;
    private RealmUserRepository userRepository;

    public OrgMemberAdapter(Activity activity, List<OrgMember> allDataList, RealmUserRepository userRepository) {
        this.allDataList = allDataList;
        this.userRepository = userRepository;
        this.mContext = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(mContext, R.layout.org_item_contact, null);
        return new ContentVH(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        OrgMember entity = allDataList.get(position);
        ContentVH vh = (ContentVH) holder;

        vh.tvName.setText(entity.getFullname());
//        vh.tv_company.setText(entity.getCompanyName());
        vh.tv_dept.setText(entity.getThisposName());
        vh.tv_zhiwei.setText(entity.getThisposName());
        String avatarByUsername = userRepository.getAvatarByUsername(entity.getUsername());
        RequestOptions options = new RequestOptions().placeholder(R.drawable.default_hd_avatar)
                .error(R.drawable.default_hd_avatar);
        Glide.with(RocketChatApplication.getInstance())
                .load(avatarByUsername)
                .apply(options)
                .into(vh.iv_icon);

        vh.iv_delete.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
            }
        });
    }

    @Override
    public int getItemCount() {
        return allDataList.size();
    }

    private class ContentVH extends RecyclerView.ViewHolder {
        TextView tvName,tv_company,tv_zhiwei,tv_dept;
        CheckBox cb_check;
        ImageView iv_icon,iv_delete;
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
            iv_delete =  itemView.findViewById(R.id.iv_delete);
        }
    }

}
