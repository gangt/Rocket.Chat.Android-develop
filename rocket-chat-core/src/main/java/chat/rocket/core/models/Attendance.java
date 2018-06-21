package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Attendance {

    public abstract String getId();

    public abstract String getUserName();

    public abstract long getUpdate();

    public static Builder builder() {
        return new AutoValue_Attendance.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder setId(String id);

        public abstract Builder setUserName(String name);

        public abstract Builder setUpdate(long update);

        public abstract Attendance build();
    }

}
