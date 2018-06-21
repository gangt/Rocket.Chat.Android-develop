package chat.rocket.android.recyclertreeview.selectuserbinder;

import chat.rocket.android.R;
import chat.rocket.persistence.realm.models.ddp.UserEntity;
import tellh.com.recyclertreeview_lib.LayoutItemType;

/**
 * Created by helloworld on 2018/4/8
 */

public class MemberItemType implements LayoutItemType {

    private UserEntity member;

    public MemberItemType(UserEntity member) {
        this.member = member;
    }

    public UserEntity getMember() {
        return member;
    }

    @Override
    public int getLayoutId() {
        return R.layout.org_member_group;
    }

}
