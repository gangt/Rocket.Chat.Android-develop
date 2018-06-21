package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by user on 2018/1/17.
 */

@AutoValue
public abstract class Subscription {

    public static final String TYPE_CHANNEL = "c";
    public static final String TYPE_GROUP = "p";
    public static final String TYPE_DIRECT_MESSAGE = "d";
    public static final String TYPE_LIVECHAT = "l";

    @Nullable
    public abstract String getId();

    @Nullable
    public abstract String getAlert();

    @Nullable
    public abstract String getCId();

    @Nullable
    public abstract String getCompanyId();

    @Nullable
    public abstract String getCompanyName();

    @Nullable
    public abstract String getDisplayName();

    @Nullable
    public abstract String getEncrypt();

    @Nullable
    public abstract String getLabelId();

    @Nullable
    public abstract String getLabelName();

    @Nullable
    public abstract String getName();

    @Nullable
    public abstract String getOpen();
    @Nullable
    public abstract String getBlocked();
    @Nullable
    public abstract String getBlocker();

    @Nullable
    public abstract String getRid();

    @Nullable
    public abstract String getS();

    @Nullable
    public abstract String getSubMeetingType();

    @Nullable
    public abstract String getT();

    @Nullable
    public abstract String getUnread();

    public abstract long getUpdatedAt();

    public abstract long getLs();

    @Nullable
    public abstract List<String> getRoles();

    public abstract long getSortTime();

    public abstract long getTs();

    @Nullable
    public abstract String getUId();

    @Nullable
    public abstract String getUUserName();

    @Nullable
    public abstract String getGId();

    @Nullable
    public abstract String getLevel();

    @Nullable
    public abstract String getGName();

    @Nullable
    public abstract String getMnd();

    @Nullable
    public abstract String getStatus();

    public boolean isDirectMessage() {
        return TYPE_DIRECT_MESSAGE.equals(getT());
    }

    public boolean isChannel() {
        return TYPE_CHANNEL.equals(getT());
    }
    public boolean isPrivate() {
        return TYPE_GROUP.equals(getT());
    }
    public boolean isLivechat() {
        return TYPE_LIVECHAT.equals(getT());
    }
    public static Subscription.Builder builder() {
        return new AutoValue_Subscription.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(String id);

        public abstract Builder setAlert(String alert);

        public abstract Builder setCId(String cId);

        public abstract Builder setCompanyId(String companyId);

        public abstract Builder setCompanyName(String companyName);

        public abstract Builder setDisplayName(String displayName);

        public abstract Builder setEncrypt(String encrypt);

        public abstract Builder setLabelId(String labelId);

        public abstract Builder setLabelName(String labelName);

        public abstract Builder setName(String name);

        public abstract Builder setOpen(String open);

        public abstract Builder setBlocked(String open);

        public abstract Builder setBlocker(String open);

        public abstract Builder setRid(String rid);

        public abstract Builder setS(String s);

        public abstract Builder setSubMeetingType(String subMeetingType);

        public abstract Builder setT(String t);

        public abstract Builder setUnread(String unread);

        public abstract Builder setUpdatedAt(long updatedAt);

        public abstract Builder setLs(long ls);

        public abstract Builder setRoles(List<String> roles);

        public abstract Builder setSortTime(long sortTime);

        public abstract Builder setTs(long ts);

        public abstract Builder setUId(String uId);

        public abstract Builder setUUserName(String uUserName);

        public abstract Builder setGId(String gId);

        public abstract Builder setLevel(String level);

        public abstract Builder setGName(String gName);

        public abstract Builder setMnd(String mnd);

        public abstract Builder setStatus(String status);

        public abstract Subscription build();
    }
}
