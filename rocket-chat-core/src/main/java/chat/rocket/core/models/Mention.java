package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

/**
 * Created by user on 2018/3/13.
 */
@AutoValue
public abstract class Mention {
    public abstract String getId();

    public abstract String getUsername();

    @Nullable
    public abstract String getName();

    public static Builder builder() {
        return new AutoValue_Mention.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(String id);

        public abstract Builder setUsername(String username);

        public abstract Builder setName(String name);

        public abstract Mention build();

    }
}