package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import java.util.List;

import javax.annotation.Nullable;

@AutoValue
public abstract class Message {
    @Nullable
    public abstract String getTime();

    @Nullable
    public abstract String getNType();

    @Nullable
    public abstract String getMediaId();

    @Nullable
    public abstract String getStatus();

    @Nullable
    public abstract String getReceiveMsg();
    @Nullable
    public abstract String getFromMsg();




    public abstract String getId();

    @Nullable
    public abstract String getType();

    public abstract String getRoomId();

    public abstract int getSyncState();

    public abstract long getTimestamp();

    public abstract String getMessage();

    @Nullable
    public abstract User getUser();

    @Nullable
    public abstract String getUserJson();

    public abstract boolean isGroupable();

    @Nullable
    public abstract File getFile();

    @Nullable
    public abstract String getFileJson();

    @Nullable
    public abstract List<Attachment> getAttachments();

    @Nullable
    public abstract Card getCard();

    @Nullable
    public abstract Report getReport();

    @Nullable
    public abstract String getAttachmentsJson();

    @Nullable
    public abstract String getReportJson();

    @Nullable
    public abstract String getCardJson();

    @Nullable
    public abstract String getHidelink();

    @Nullable
    public abstract List<WebContent> getWebContents();

    @Nullable
    public abstract List<Mention> getMentions();

    @Nullable
    public abstract String getAlias();

    @Nullable
    public abstract String getAvatar();

    public abstract long getEditedAt();

    public abstract Message withSyncState(int syncState);

    public abstract Message withUser(User user);

    public abstract Message withMessage(String message);

    public abstract Message withEditedAt(long editedAt);

    @Nullable
    public abstract String getMsgId();

    public static Builder builder() {
        return new AutoValue_Message.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder setNType(String nType);

        public abstract Builder setTime(String nType);

        public abstract Builder setMediaId(String mediaId);

        public abstract Builder setStatus(String status);

        public abstract Builder setReceiveMsg(String receiveMsg);

        public abstract Builder setFromMsg(String fromMsg);

        public abstract Builder setId(String id);

        public abstract Builder setType(String type);

        public abstract Builder setRoomId(String roomId);

        public abstract Builder setSyncState(int syncState);

        public abstract Builder setTimestamp(long timestamp);

        public abstract Builder setMessage(String message);

        public abstract Builder setUser(User user);

        public abstract Builder setUserJson(String user);

        public abstract Builder setGroupable(boolean groupable);

        public abstract Builder setFileJson(String file);

        public abstract Builder setFile(File file);

        public abstract Builder setCard(Card card);

        public abstract Builder setReport(Report report);

        public abstract Builder setAttachments(List<Attachment> attachments);

        public abstract Builder setAttachmentsJson(String attachmentsjson);

        public abstract Builder setReportJson(String report);

        public abstract Builder setCardJson(String card);

        public abstract Builder setHidelink(String hidelink);

        public abstract Builder setWebContents(List<WebContent> webContents);

        public abstract Builder setAlias(String alias);

        public abstract Builder setAvatar(String avatar);

        public abstract Builder setEditedAt(long editedAt);

        public abstract Builder setMentions(List<Mention> mentions);

        public abstract Builder setMsgId(String msgId);

        public abstract Message build();
    }
}
