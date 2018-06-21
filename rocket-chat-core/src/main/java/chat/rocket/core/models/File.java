package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import java.util.List;

import javax.annotation.Nullable;

@AutoValue
public abstract class File {

  @Nullable
  public abstract String getId();

  @Nullable
  public abstract String getName();


  public static Builder builder() {
    return new AutoValue_File.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setName(String name);

    public abstract Builder setId(String id);

    public abstract File build();
  }
}
