package chat.rocket.android.recyclertreeview.viewbinder;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hadisatrio.optional.Optional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.login.SharedPreferencesUtil;
import chat.rocket.android.login.UserInfo;
import chat.rocket.android.recyclertreeview.bean.RootGroup;
import chat.rocket.core.PermissionsConstants;
import chat.rocket.core.models.Role;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.PermissionRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import io.reactivex.Flowable;
import tellh.com.recyclertreeview_lib.TreeNode;
import tellh.com.recyclertreeview_lib.TreeViewBinder;

/**
 * Created by user on 2018/1/10.
 */

public class RootGroupNodeBinder extends TreeViewBinder<RootGroupNodeBinder.ViewHolder> {
    private OnItemClickListener onItemClickListener;
    private PermissionRepository permissionRepository;
    private List<String> createWRoles;
    private List<String> createCRoles;
    private List<String> createPRoles;
    private User user;

    public RootGroupNodeBinder(OnItemClickListener onItemClickListener, PermissionRepository permissionRepository, User user) {
        this.onItemClickListener = onItemClickListener;
        this.permissionRepository = permissionRepository;
        this.user = user;
    }

    @Override
    public int getLayoutId() {
        return R.layout.treeitem_rootgroup;
    }

    @Override
    public ViewHolder provideViewHolder(View itemView) {
        return new ViewHolder(itemView);
    }

    @SuppressLint("RxLeakedSubscription")
    @Override
    public void bindView(ViewHolder holder, int position, TreeNode node) {
//        userRepository.getCurrent().distinctUntilChanged().subscribe(userOptional -> {
//            user = userOptional.get();
//        }, RCLog::e);
        createWRoles = new ArrayList<>();
        createCRoles = new ArrayList<>();
        createPRoles = new ArrayList<>();
        List<String> userRoles = user.getRoles();
        RootGroup content = (RootGroup) node.getContent();
        holder.tvName.setText(content.getRootGroupName().getValue());
        holder.tvCount.setText("(" + content.getCount() + ")");
        holder.remind.setVisibility(content.isRemind()?View.VISIBLE:View.GONE);
        holder.head.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
//        holder.line.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
        int imgUrl = 0;
        permissionRepository.getById(PermissionsConstants.CREATE_W)
                .subscribe(permissionOptional -> {
                    List<Role> createWroles = permissionOptional.get().getRoles();
                    for (Role role : createWroles) {
                        this.createWRoles.add(role.getName());
                    }
                }, RCLog::e);
        permissionRepository.getById(PermissionsConstants.CREATE_C)
                .subscribe(permissionOptional -> {
                    List<Role> createCroles = permissionOptional.get().getRoles();
                    for (Role role : createCroles) {
                        this.createCRoles.add(role.getName());
                    }
                }, RCLog::e);
        permissionRepository.getById(PermissionsConstants.CREATE_P)
                .subscribe(permissionOptional -> {
                    List<Role> createProles = permissionOptional.get().getRoles();
                    for (Role role : createProles) {
                        this.createPRoles.add(role.getName());
                    }
                }, RCLog::e);

        switch (content.getRootGroupName().getId()) {
            case "Layout_Person_Title":
                imgUrl = R.drawable.icon_work;
                holder.ivHistory.setVisibility(View.GONE);
                holder.ivAdd.setVisibility(Collections.disjoint(userRoles, createWRoles) ? View.GONE : View.VISIBLE);
                break;
            case "Layout_Meeting_Title":
                holder.ivAdd.setVisibility(View.VISIBLE);
                imgUrl = R.drawable.icon_conference;
                holder.ivHistory.setVisibility(View.VISIBLE);
                break;
            case "Layout_Channel_Title":
                holder.ivAdd.setVisibility((Collections.disjoint(userRoles, createCRoles)
                        && Collections.disjoint(userRoles, createPRoles)) ? View.GONE : View.VISIBLE);
                imgUrl = R.drawable.icon_organization;
                holder.ivHistory.setVisibility(View.GONE);
                break;
        }
        holder.ivIcon.setImageResource(imgUrl);
        holder.ivAdd.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.addGroupChat(holder, position, node);
            }
        });
        holder.ivHistory.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.historySetting();
            }
        });
    }

    public interface OnItemClickListener {
        void addGroupChat(ViewHolder holder, int position, TreeNode node);

        void historySetting();
    }

    public static class ViewHolder extends TreeViewBinder.ViewHolder {
        private View head;
//        private View line;
        private ImageView ivIcon;
        private TextView tvName;
        private TextView tvCount;
        private ImageView ivAdd;
        private ImageView ivHistory;
        private ImageView remind;
        public ViewHolder(View rootView) {
            super(rootView);
            head = rootView.findViewById(R.id.head);
            ivIcon = rootView.findViewById(R.id.iv_icon);
            tvName = rootView.findViewById(R.id.tv_name);
            tvCount = rootView.findViewById(R.id.tv_count);
            ivAdd = rootView.findViewById(R.id.iv_add);
            remind = rootView.findViewById(R.id.iv_remind);
//            line = rootView.findViewById(R.id.line);
            ivHistory = rootView.findViewById(R.id.iv_history_setting);
        }
    }
}
