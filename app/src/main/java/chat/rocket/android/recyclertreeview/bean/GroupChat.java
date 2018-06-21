package chat.rocket.android.recyclertreeview.bean;

import chat.rocket.android.R;
import chat.rocket.core.models.Subscription;
import tellh.com.recyclertreeview_lib.LayoutItemType;

/**
 * Created by user on 2018/1/10.
 */

public class GroupChat implements LayoutItemType {
    private Subscription subscription;

    public GroupChat(Subscription subscription) {
        this.subscription = subscription;
    }
    public Subscription getSubscription(){
        return subscription;
    }
    @Override
    public int getLayoutId() {
        return R.layout.treeitem_group_chat;
    }
}
