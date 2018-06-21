package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

/**
 * Created by user on 2018/1/24.
 */

@AutoValue
public abstract class Auth {
    @Nullable
    public abstract String getId();

    @Nullable
    public abstract String getToken();

    public abstract long getTokenExpires();

    public static Auth.Builder builder() {
        return new AutoValue_Auth.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setId(String id);

        public abstract Builder setToken(String token);

        public abstract Builder setTokenExpires(long tokenExpires);

        public abstract Auth builder();
    }
}