package chat.rocket.android.recyclertreeview.bean;

import chat.rocket.android.R;
import chat.rocket.core.models.Labels;
import tellh.com.recyclertreeview_lib.LayoutItemType;

/**
 * Created by user on 2018/1/10.
 */

public class Group implements LayoutItemType {
    private Labels labels;
    private int unRead;

    public Group(Labels labels) {
        this.labels = labels;
    }
    public int getUnRead(){
        return unRead;
    }
    public void setUnRead(int unRead){
        this.unRead=unRead;
    }
    public Labels getLabel(){
        return labels;
    }
    @Override
    public int getLayoutId() {
        return R.layout.treeitem_group;
    }
}
