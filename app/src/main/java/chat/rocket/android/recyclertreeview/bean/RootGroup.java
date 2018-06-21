package chat.rocket.android.recyclertreeview.bean;

import chat.rocket.android.R;
import chat.rocket.core.models.PublicSetting;
import tellh.com.recyclertreeview_lib.LayoutItemType;

/**
 * Created by user on 2018/1/10.
 */

public class RootGroup implements LayoutItemType {
    private PublicSetting rootGroupName;
    private int count;
    private boolean isRemind;
    public RootGroup(PublicSetting rootGroupName, int count,boolean isRemind) {
        this.rootGroupName = rootGroupName;
        this.count = count;
        this.isRemind=isRemind;
    }

    public PublicSetting getRootGroupName() {
        return rootGroupName;
    }

    public int getCount() {
        return count;
    }

    public boolean isRemind(){
        return isRemind;
    }
    @Override
    public int getLayoutId() {
        return R.layout.treeitem_rootgroup;
    }
}
