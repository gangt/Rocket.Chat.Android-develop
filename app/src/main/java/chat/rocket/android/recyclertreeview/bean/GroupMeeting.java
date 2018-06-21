package chat.rocket.android.recyclertreeview.bean;

import chat.rocket.android.R;
import chat.rocket.core.models.Labels;
import tellh.com.recyclertreeview_lib.LayoutItemType;

/**
 * Created by user on 2018/1/10.
 */

public class GroupMeeting implements LayoutItemType {
    private String groupMeetingName;
    private int unRead;

    public GroupMeeting(String groupMeetingName) {
        this.groupMeetingName = groupMeetingName;
    }
    public int getUnRead(){
        return unRead;
    }
    public void setUnRead(int unRead){
        this.unRead=unRead;
    }
    public String getGroupMeetingName(){
        return groupMeetingName;
    }
    @Override
    public int getLayoutId() {
        return R.layout.treeitem_group_meeting;
    }
}
