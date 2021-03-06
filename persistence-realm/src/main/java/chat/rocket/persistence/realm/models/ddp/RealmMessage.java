package chat.rocket.persistence.realm.models.ddp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat.rocket.core.JsonConstants;
import chat.rocket.core.SyncState;
import chat.rocket.core.models.Attachment;
import chat.rocket.core.models.AttachmentAuthor;
import chat.rocket.core.models.AttachmentChild;
import chat.rocket.core.models.AttachmentField;
import chat.rocket.core.models.AttachmentTitle;
import chat.rocket.core.models.Card;
import chat.rocket.core.models.DeptRole;
import chat.rocket.core.models.Email;
import chat.rocket.core.models.File;
import chat.rocket.core.models.Mention;
import chat.rocket.core.models.Message;
import chat.rocket.core.models.Report;
import chat.rocket.core.models.ReportList;
import chat.rocket.core.models.WebContent;
import chat.rocket.core.models.WebContentHeaders;
import chat.rocket.core.models.WebContentMeta;
import chat.rocket.core.models.WebContentParsedUrl;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * RealmMessage.
 */
@SuppressWarnings({"PMD.ShortClassName", "PMD.ShortVariable",
        "PMD.MethodNamingConventions", "PMD.VariableNamingConventions"})
public class RealmMessage extends RealmObject {
    //ref: Rocket.Chat:packages/rocketchat-lib/lib/MessageTypes.coffee

    public static final String ID = "_id";
    public static final String TYPE = "t";
    public static final String ROOM_ID = "rid";
    public static final String SYNC_STATE = "syncstate";
    public static final String TIMESTAMP = "ts";

    @SuppressWarnings({"PMD.AvoidFieldNameMatchingTypeName"})
    public static final String MESSAGE = "msg";
    public static final String USER = "u";
    public static final String USER_ID = "u._id";
    public static final String GROUPABLE = "groupable";
    public static final String ATTACHMENTS = "attachments";
    public static final String URLS = "urls";
    public static final String EDITED_AT = "editedAt";
    public static final String FILE = "file";

    @PrimaryKey
    private String _id;
    private String t; //type:
    private String rid; //roomId.
    private int syncstate;
    private long ts;
    private String msg;
    private RealmUser1 u;
    private boolean groupable;
    private String alias;
    private String avatar;
    private String attachments; //JSONArray.
    private String report; //JSONArray.
    private String card; //JSONArray.
    private String hidelink;
    private String file; //JSONArray.
    private String urls; //JSONArray.
    private long editedAt;
    private String msgId;
    private RealmList<RealmMention> mentions;

    private String nType;
    private String mediald;
    private String status;
    private String receiveMsg;
    private String fromMsg;
    private String time ;

    public static JSONObject customizeJson(JSONObject messageJson) throws JSONException {
        try {
            long ts = messageJson.getJSONObject(TIMESTAMP).getLong(JsonConstants.DATE);
            messageJson.remove(TIMESTAMP);
            messageJson.put(TIMESTAMP, ts).put(SYNC_STATE, SyncState.SYNCED);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (messageJson.isNull(GROUPABLE)) {
            messageJson.put(GROUPABLE, true);
        }
        if (!messageJson.isNull("role")) {
            messageJson.put("alias", messageJson.getString("role"));
        }

        long editedAt = 0L;
        JSONObject editedAtObj = messageJson.optJSONObject(EDITED_AT);
        if (editedAtObj != null) {
            editedAt = editedAtObj.optLong(JsonConstants.DATE);
        }

        messageJson.remove(EDITED_AT);
        messageJson.put(EDITED_AT, editedAt);

        return messageJson;
    }

    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getType() {
        return t;
    }

    public void setType(String t) {
        this.t = t;
    }

    public String getRoomId() {
        return rid;
    }

    public void setRoomId(String rid) {
        this.rid = rid;
    }

    public int getSyncState() {
        return syncstate;
    }

    public void setSyncState(int syncstate) {
        this.syncstate = syncstate;
    }

    public long getTimestamp() {
        return ts;
    }

    public void setTimestamp(long ts) {
        this.ts = ts;
    }

    public String getMessage() {
        return msg;
    }

    public void setMessage(String msg) {
        this.msg = msg;
    }

    public RealmUser1 getUser() {
        return u;
    }

    public void setUser(RealmUser1 u) {
        this.u = u;
    }

    public boolean isGroupable() {
        return groupable;
    }

    public void setGroupable(boolean groupable) {
        this.groupable = groupable;
    }

    public String getAttachments() {
        return attachments;
    }

    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public void setHidelink(String hidelink) {
        this.hidelink = hidelink;
    }

    public String getUrls() {
        return urls;
    }

    public void setUrls(String urls) {
        this.urls = urls;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public long getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(long editedAt) {
        this.editedAt = editedAt;
    }

    public RealmList<RealmMention> getMentions() {
        return mentions;
    }

    public void setMentions(RealmList<RealmMention> mentions) {
        this.mentions = mentions;
    }

    public Message asMessage() {
        final int total = mentions != null ? mentions.size() : 0;

        final List<Mention> coreMentoins = new ArrayList<>(total);

        for (int i = 0; i < total; i++) {
            coreMentoins.add(mentions.get(i).asMention());
        }


        return Message.builder()
                .setId(_id)
                .setType(t)
                .setRoomId(rid)
                .setSyncState(syncstate)
                .setTimestamp(ts)
                .setMessage(msg == null ? "" : msg)
                .setUser(u != null ? u.asUser() : null)
                .setGroupable(groupable)
                .setAlias(alias)
                .setAvatar(avatar)
                .setEditedAt(editedAt)
                .setFileJson(file)
                .setFile(getFiles())
                .setCard(getCard())
                .setReport(getReport())
                .setAttachments(getCoreAttachments())
                .setAttachmentsJson(attachments)
                .setReportJson(report)
                .setCardJson(card)
                .setHidelink(hidelink)
                .setWebContents(getWebContents())
                .setMentions(coreMentoins)
                .setMsgId(msgId)
                .setNType(nType)
                .setMediaId(mediald)
                .setStatus(status)
                .setReceiveMsg(receiveMsg)
                .setFromMsg(fromMsg)
                .setTime(time)
                .build();
    }

    private File getFiles() {
        JSONObject jsonObject = new JSONObject();
        if (file == null || file.length() == 0)
            return null;
        try {
            jsonObject = new JSONObject(file);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return File.builder()
                .setId(jsonObject.optString("_id", null))
                .setName(jsonObject.optString("name", null)).build();
    }

    private Card getCard() {
        JSONObject jsonObject = new JSONObject();
        if (card == null || card.length() == 0)
            return null;
        try {
            jsonObject = new JSONObject(card);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Card.builder()
                .setLogo(jsonObject.optString("logo", null))
                .setContent(jsonObject.optString("content", null))
                .setTitle(jsonObject.optString("title", null))
                .setSender(jsonObject.optString("sender", null))
                .setUrl(jsonObject.optString("url", null)).build();
    }

    private Report getReport() {
        JSONObject jsonObject = new JSONObject();
        if (report == null || report.length() == 0)
            return null;
        try {
            jsonObject = new JSONObject(report);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Report.builder()
                .setTheme(jsonObject.optString("theme", null))
                .setReportList(getReportList(jsonObject.optString("list", null)))
                .build();
    }

    private List<ReportList> getReportList(String json){
        if (json == null || json.length() == 0) {
            return null;
        } else {
            List<ReportList> reportLists = new ArrayList<>();
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0, size = jsonArray.length(); i < size; i++) {
                    ReportList reportList = ReportList.builder()
                            .setLink(jsonArray.getJSONObject(i).optString("link", null))
                            .setTitle(jsonArray.getJSONObject(i).optString("title", null))
                            .build();
                    if (reportList != null) {
                        reportLists.add(reportList);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return reportLists;
        }
    }
    private List<Attachment> getCoreAttachments() {
        if (attachments == null || attachments.length() == 0) {
            return null;
        } else {
            List<Attachment> coreAttachments = new ArrayList<>();
            try {
                JSONArray jsonArray = new JSONArray(attachments);
                for (int i = 0, size = jsonArray.length(); i < size; i++) {
                    final Attachment coreAttachment = getCoreAttachment(jsonArray.getJSONObject(i));
                    if (coreAttachment != null) {
                        coreAttachments.add(coreAttachment);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return coreAttachments;
        }
    }

    private Attachment getCoreAttachment(JSONObject jsonCoreAttachment) {
        JSONObject attachments = new JSONObject();
        JSONObject timeStamp = new JSONObject();
        try {
            if (jsonCoreAttachment.has("attachments") && jsonCoreAttachment.getJSONArray("attachments").length() != 0)
                attachments = jsonCoreAttachment.getJSONArray("attachments").getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            if (jsonCoreAttachment.has("ts"))
                timeStamp = jsonCoreAttachment.getJSONObject("ts");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Attachment.builder()
                .setAuthorName(jsonCoreAttachment.optString("author_name", null))
                .setTimestamp(timeStamp.optString("$date", ""))
                .setColor(jsonCoreAttachment.optString("color", null))
                .setText(jsonCoreAttachment.optString("text", null))
                .setThumbUrl(jsonCoreAttachment.optString("thumb_url", null))
                .setMessageLink(jsonCoreAttachment.optString("message_link", null))
                .setCollapsed(jsonCoreAttachment.optBoolean("collapsed"))
                .setImageUrl(jsonCoreAttachment.optString("image_url", null))
                .setAudioUrl(jsonCoreAttachment.optString("audio_url", null))
                .setVideoUrl(jsonCoreAttachment.optString("video_url", null))
                .setTitleLink(jsonCoreAttachment.optString("title_link", null))
                .setAttachmentAuthor(getAttachmentAuthor(jsonCoreAttachment))
                .setAttachmentTitle(getAttachmentTitle(jsonCoreAttachment))
                .setAttachmentFields(getAttachmentFields(jsonCoreAttachment))
                .setAttachmentChild(getAttachmentChild(attachments))
                .build();
    }

    private AttachmentAuthor getAttachmentAuthor(JSONObject jsonCoreAttachment) {
        if (jsonCoreAttachment.isNull("author_name") || jsonCoreAttachment.isNull("author_link")
                || jsonCoreAttachment.isNull("author_icon")) {
            return null;
        }

        return AttachmentAuthor.builder()
                .setName(jsonCoreAttachment.optString("author_name"))
                .setLink(jsonCoreAttachment.optString("author_link"))
                .setIconUrl(jsonCoreAttachment.optString("author_icon"))
                .build();
    }

    private AttachmentTitle getAttachmentTitle(JSONObject jsonCoreAttachment) {
        if (jsonCoreAttachment.isNull("title")) {
            return null;
        }

        return AttachmentTitle.builder()
                .setTitle(jsonCoreAttachment.optString("title"))
                .setLink(jsonCoreAttachment.optString("title_link", null))
                .setDownloadLink(jsonCoreAttachment.optString("title_link_download", null))
                .build();
    }

    private List<AttachmentField> getAttachmentFields(JSONObject jsonCoreAttachment) {
        final JSONArray jsonFields = jsonCoreAttachment.optJSONArray("fields");
        if (jsonFields == null) {
            return null;
        }

        final List<AttachmentField> attachmentFields = new ArrayList<>();
        for (int i = 0, size = jsonFields.length(); i < size; i++) {
            final JSONObject fieldObject = jsonFields.optJSONObject(i);
            if (fieldObject == null || fieldObject.isNull("title") || fieldObject.isNull("value")) {
                continue;
            }

            attachmentFields.add(AttachmentField.builder()
                    .setShort(fieldObject.optBoolean("short"))
                    .setTitle(fieldObject.optString("title"))
                    .setText(fieldObject.optString("value"))
                    .build());
        }

        return attachmentFields;
    }

    private AttachmentChild getAttachmentChild(JSONObject jsonCoreAttachment) {
        JSONObject attachments = null;
        JSONObject timeStamp = new JSONObject();
        if (jsonCoreAttachment == null || jsonCoreAttachment.toString().equals("{}")) {
            return null;
        } else {
            try {
                timeStamp = jsonCoreAttachment.getJSONObject("ts");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                if (jsonCoreAttachment.getJSONArray("attachments") != null && !jsonCoreAttachment.getJSONArray("attachments").toString().equals("[]"))
                    attachments = jsonCoreAttachment.getJSONArray("attachments").getJSONObject(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return AttachmentChild.builder()
                    .setText(jsonCoreAttachment.optString("text", null))
                    .setAuthorName(jsonCoreAttachment.optString("author_name", null))
                    .setTime(timeStamp.optString("$date", ""))
                    .setTitle(jsonCoreAttachment.optString("title", null))
                    .setTitle_link(jsonCoreAttachment.optString("title_link", null))
                    .setImage_url(jsonCoreAttachment.optString("image_url", null))
                    .setTitle_link_download(jsonCoreAttachment.optString("title_link_download", null))
                    .setDescription(jsonCoreAttachment.optString("description", null))
                    .setAttachmentChild(getAttachmentChild(attachments))
                    .build();
        }

    }


    private List<WebContent> getWebContents() {
        if (urls == null || urls.length() == 0) {
            return null;
        }

        final List<WebContent> webContents = new ArrayList<>();

        try {
            final JSONArray jsonArray = new JSONArray(urls);
            for (int i = 0, size = jsonArray.length(); i < size; i++) {
                final WebContent webContent = getWebContent(jsonArray.getJSONObject(i));
                if (webContent != null) {
                    webContents.add(webContent);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return webContents;
    }

    private WebContent getWebContent(JSONObject jsonWebContent) {
        return WebContent.builder()
                .setUrl(jsonWebContent.optString("url"))
                .setMetaMap(getWebContentMetaMap(jsonWebContent.optJSONObject("meta")))
                .setHeaders(getWebContentHeaders(jsonWebContent.optJSONObject("headers")))
                .setParsedUrl(getWebContentParsedUrl(jsonWebContent.optJSONObject("parsedUrl")))
                .build();
    }

    private Map<WebContentMeta.Type, WebContentMeta> getWebContentMetaMap(
            JSONObject jsonWebContentMeta) {
        if (jsonWebContentMeta == null) {
            return null;
        }

        Map<WebContentMeta.Type, WebContentMeta> metaMap = new HashMap<>(3);

        if (!jsonWebContentMeta.isNull("ogTitle")
                || !jsonWebContentMeta.isNull("ogDescription")
                || !jsonWebContentMeta.isNull("ogImage")) {
            metaMap.put(
                    WebContentMeta.Type.OPEN_GRAPH,
                    WebContentMeta.builder()
                            .setType(WebContentMeta.Type.OPEN_GRAPH)
                            .setTitle(jsonWebContentMeta.optString("ogTitle", null))
                            .setDescription(jsonWebContentMeta.optString("ogDescription", null))
                            .setImage(jsonWebContentMeta.optString("ogImage", null))
                            .build()
            );
        }

        if (!jsonWebContentMeta.isNull("twitterTitle")
                || !jsonWebContentMeta.isNull("twitterDescription")
                || !jsonWebContentMeta.isNull("twitterImage")) {
            metaMap.put(
                    WebContentMeta.Type.TWITTER,
                    WebContentMeta.builder()
                            .setType(WebContentMeta.Type.TWITTER)
                            .setTitle(jsonWebContentMeta.optString("twitterTitle", null))
                            .setDescription(jsonWebContentMeta.optString("twitterDescription", null))
                            .setImage(jsonWebContentMeta.optString("twitterImage", null))
                            .build()
            );
        }

        if (!jsonWebContentMeta.isNull("pageTitle")
                || !jsonWebContentMeta.isNull("description")) {
            metaMap.put(
                    WebContentMeta.Type.OTHER,
                    WebContentMeta.builder()
                            .setType(WebContentMeta.Type.OTHER)
                            .setTitle(jsonWebContentMeta.optString("pageTitle", null))
                            .setDescription(jsonWebContentMeta.optString("description", null))
                            .build()
            );
        }

        return metaMap;
    }

    private WebContentHeaders getWebContentHeaders(JSONObject jsonWebContentHeaders) {
        if (jsonWebContentHeaders == null || jsonWebContentHeaders.isNull("contentType")) {
            return null;
        }

        return WebContentHeaders.builder()
                .setContentType(jsonWebContentHeaders.optString("contentType"))
                .build();
    }

    private WebContentParsedUrl getWebContentParsedUrl(JSONObject jsonWebContentParsedUrl) {
        if (jsonWebContentParsedUrl == null || jsonWebContentParsedUrl.isNull("host")) {
            return null;
        }

        return WebContentParsedUrl.builder()
                .setHost(jsonWebContentParsedUrl.optString("host"))
                .build();
    }

    @Override
    public String toString() {
        return "RealmMessage{" +
                "_id='" + _id + '\'' +
                ", t='" + t + '\'' +
                ", rid='" + rid + '\'' +
                ", syncstate=" + syncstate +
                ", ts=" + ts +
                ", msg='" + msg + '\'' +
                ", u=" + u +
                ", groupable=" + groupable +
                ", alias='" + alias + '\'' +
                ", avatar='" + avatar + '\'' +
                ", attachments='" + attachments + '\'' +
                ", urls='" + urls + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RealmMessage message = (RealmMessage) o;

        if (syncstate != message.syncstate) {
            return false;
        }
        if (ts != message.ts) {
            return false;
        }
        if (groupable != message.groupable) {
            return false;
        }
        if (_id != null ? !_id.equals(message._id) : message._id != null) {
            return false;
        }
        if (t != null ? !t.equals(message.t) : message.t != null) {
            return false;
        }
        if (rid != null ? !rid.equals(message.rid) : message.rid != null) {
            return false;
        }
        if (msg != null ? !msg.equals(message.msg) : message.msg != null) {
            return false;
        }
        if (u != null ? !u.equals(message.u) : message.u != null) {
            return false;
        }
        if (alias != null ? !alias.equals(message.alias) : message.alias != null) {
            return false;
        }
        if (avatar != null ? !avatar.equals(message.avatar) : message.avatar != null) {
            return false;
        }
        if (attachments != null ? !attachments.equals(message.attachments)
                : message.attachments != null) {
            return false;
        }
        return urls != null ? urls.equals(message.urls) : message.urls == null;

    }

    @Override
    public int hashCode() {
        int result = _id != null ? _id.hashCode() : 0;
        result = 31 * result + (t != null ? t.hashCode() : 0);
        result = 31 * result + (rid != null ? rid.hashCode() : 0);
        result = 31 * result + syncstate;
        result = 31 * result + (int) (ts ^ (ts >>> 32));
        result = 31 * result + (msg != null ? msg.hashCode() : 0);
        result = 31 * result + (u != null ? u.hashCode() : 0);
        result = 31 * result + (groupable ? 1 : 0);
        result = 31 * result + (alias != null ? alias.hashCode() : 0);
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        result = 31 * result + (attachments != null ? attachments.hashCode() : 0);
        result = 31 * result + (urls != null ? urls.hashCode() : 0);
        return result;
    }
}
