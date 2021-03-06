package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class ReportList {

  @Nullable
  public abstract String getTitle();

  @Nullable
  public abstract String getLink();


  public static Builder builder() {
    return new AutoValue_ReportList.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setTitle(String title);

    public abstract Builder setLink(String link);

    public abstract ReportList build();
  }
}
