package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

/**
 * Created by user on 2018/1/17.
 */

@AutoValue
public abstract class Labels {
    @Nullable
    public abstract String getId();

    @Nullable
    public abstract String getLevel();

    @Nullable
    public abstract String getName();

    @Nullable
    public abstract String getType();

    public abstract long getUpdatedAt();

    @Nullable
    public abstract String getCompanyId();

    @Nullable
    public abstract String getCompanyName();

    public static Labels.Builder builder() {
        return new AutoValue_Labels.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(String id);

        public abstract Builder setLevel(String level);

        public abstract Builder setName(String name);

        public abstract Builder setType(String type);

        public abstract Builder setUpdatedAt(long updatedAt);

        public abstract Builder setCompanyId(String companyId);

        public abstract Builder setCompanyName(String companyName);

        public abstract Labels build();
    }
}
