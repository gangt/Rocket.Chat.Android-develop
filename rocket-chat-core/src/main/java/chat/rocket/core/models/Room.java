package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import java.util.List;

import javax.annotation.Nullable;

@AutoValue
public abstract class Room {

    public static final String TYPE_CHANNEL = "c";
    public static final String TYPE_GROUP = "p";
    public static final String TYPE_DIRECT_MESSAGE = "d";
    public static final String TYPE_LIVECHAT = "l";

    @Nullable
    public abstract String getId();

    @Nullable
    public abstract String getRoomId();

    @Nullable
    public abstract String getName();

    @Nullable
    public abstract String getType();

    public abstract boolean isOpen();

    public abstract boolean isPause();

    @Nullable
    public abstract String getEncrypt();

    public abstract boolean isAlert();

    public abstract int getUnread();

    public abstract long getUpdatedAt();

    public abstract long getLastSeen();

    public abstract boolean isFavorite();

    @Nullable
    public abstract String getCid();

    @Nullable
    public abstract String getCompanyId();

    @Nullable
    public abstract String getCompanyName();

    @Nullable
    public abstract String getDisplayName();

    @Nullable
    public abstract String getGId();

    @Nullable
    public abstract String getLevel();

    @Nullable
    public abstract String getGName();

    @Nullable
    public abstract String getMeetingSubject();

    @Nullable
    public abstract String getOrgName();

    public abstract boolean isRo();

    @Nullable
    public abstract String getS();

    @Nullable
    public abstract String getUId();

    @Nullable
    public abstract String getUUserName();

    @Nullable
    public abstract List<String> getUsernames();

    @Nullable
    public abstract List<String> getMuted();

    @Nullable
    public abstract String getHost();

    @Nullable
    public abstract String getRawContent();

    @Nullable
    public abstract String getAttendance();
    @Nullable
    public abstract String getTopic();

    @Nullable
    public abstract String getDescription();

    @Nullable
    public abstract List<Attendance> getAttendanceList();

    public abstract long getStartTime();

    public abstract long getEndTime();

    public boolean isChannel() {
        return TYPE_CHANNEL.equals(getType());
    }

    public boolean isPrivate() {
        return TYPE_GROUP.equals(getType());
    }

    public boolean isDirectMessage() {
        return TYPE_DIRECT_MESSAGE.equals(getType());
    }

    public boolean isLivechat() {
        return TYPE_LIVECHAT.equals(getType());
    }

    public static Builder builder() {
        return new AutoValue_Room.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder setId(String id);

        public abstract Builder setRoomId(String roomId);

        public abstract Builder setName(String name);

        public abstract Builder setType(String type);

        public abstract Builder setOpen(boolean open);

        public abstract Builder setPause(boolean pause);

        public abstract Builder setEncrypt(String encrypt);

        public abstract Builder setAlert(boolean alert);

        public abstract Builder setUnread(int unread);

        public abstract Builder setUpdatedAt(long updatedAt);

        public abstract Builder setLastSeen(long lastSeen);

        public abstract Builder setFavorite(boolean favorite);

        public abstract Builder setCid(String cid);

        public abstract Builder setCompanyId(String companyId);

        public abstract Builder setCompanyName(String companyName);

        public abstract Builder setDisplayName(String displayName);

        public abstract Builder setGId(String gId);

        public abstract Builder setLevel(String level);

        public abstract Builder setGName(String gName);

        public abstract Builder setMeetingSubject(String meetingSubject);

        public abstract Builder setOrgName(String orgName);

        public abstract Builder setRo(boolean ro);

        public abstract Builder setS(String s);

        public abstract Builder setUId(String uId);

        public abstract Builder setUUserName(String uUserName);

        public abstract Builder setUsernames(List<String> userNames);

        public abstract Builder setMuted(List<String> muted);

        public abstract Builder setHost(String host);

        public abstract Builder setAttendance(String attendance);

        public abstract Builder setAttendanceList(List<Attendance> attendance);

        public abstract Builder setStartTime(long startTime);

        public abstract Builder setEndTime(long endTime);

        public abstract Builder setRawContent(String rawContent);

        public abstract Builder setTopic(String rawContent);

        public abstract Builder setDescription(String description);

        public abstract Room build();
    }
}
