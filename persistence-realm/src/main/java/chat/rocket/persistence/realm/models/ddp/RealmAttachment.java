package chat.rocket.persistence.realm.models.ddp;

import io.realm.RealmObject;

/**
 * Created by user on 2018/2/8.
 */

public class RealmAttachment extends RealmObject {
    private String image_size;
    private String image_type;
    private String mage_url;
    private String nicktitle;
    private boolean collapsed;
    private String title;
    private String title_link;
    private boolean title_link_download;
    private String thumb_url;
    private String color;
    private String audio_size;
    private String audio_type;
    private String audio_url;
    private String video_size;
    private String video_type;
    private String video_url;

    public String getImage_size() {
        return image_size;
    }

    public void setImage_size(String image_size) {
        this.image_size = image_size;
    }

    public String getImage_type() {
        return image_type;
    }

    public void setImage_type(String image_type) {
        this.image_type = image_type;
    }

    public String getMage_url() {
        return mage_url;
    }

    public void setMage_url(String mage_url) {
        this.mage_url = mage_url;
    }

    public String getNicktitle() {
        return nicktitle;
    }

    public void setNicktitle(String nicktitle) {
        this.nicktitle = nicktitle;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle_link() {
        return title_link;
    }

    public void setTitle_link(String title_link) {
        this.title_link = title_link;
    }

    public boolean isTitle_link_download() {
        return title_link_download;
    }

    public void setTitle_link_download(boolean title_link_download) {
        this.title_link_download = title_link_download;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getAudio_size() {
        return audio_size;
    }

    public void setAudio_size(String audio_size) {
        this.audio_size = audio_size;
    }

    public String getAudio_type() {
        return audio_type;
    }

    public void setAudio_type(String audio_type) {
        this.audio_type = audio_type;
    }

    public String getAudio_url() {
        return audio_url;
    }

    public void setAudio_url(String audio_url) {
        this.audio_url = audio_url;
    }

    public String getVideo_size() {
        return video_size;
    }

    public void setVideo_size(String video_size) {
        this.video_size = video_size;
    }

    public String getVideo_type() {
        return video_type;
    }

    public void setVideo_type(String video_type) {
        this.video_type = video_type;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }
}
