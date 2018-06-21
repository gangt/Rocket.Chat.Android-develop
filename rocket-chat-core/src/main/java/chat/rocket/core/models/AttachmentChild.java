package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import java.util.List;

import javax.annotation.Nullable;

@AutoValue
public abstract class AttachmentChild {
    @Nullable
    public abstract String getTitle();

    @Nullable
    public abstract String getTime();

    @Nullable
    public abstract String getText();

    @Nullable
    public abstract String getAuthorName();

    @Nullable
    public abstract String getImage_url();

    @Nullable
    public abstract String getDescription();

    @Nullable
    public abstract String getTitle_link_download();

    @Nullable
    public abstract String getTitle_link();

    @Nullable
    public abstract AttachmentChild getAttachmentChild();

    public static Builder builder() {
        return new AutoValue_AttachmentChild.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder setTitle(String title);

        public abstract Builder setText(String text);

        public abstract Builder setAuthorName(String author_name);

        public abstract Builder setTime(String time);

        public abstract Builder setTitle_link_download(String title_link_download);

        public abstract Builder setImage_url(String image_url);

        public abstract Builder setDescription(String description);

        public abstract Builder setTitle_link(String title_link);

        public abstract Builder setAttachmentChild(AttachmentChild attachmentChild);

        public abstract AttachmentChild build();
    }
}
