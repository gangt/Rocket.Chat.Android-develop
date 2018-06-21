package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class Card {
  @Nullable
  public abstract String getContent();

  @Nullable
  public abstract String getLogo();

  @Nullable
  public abstract String getTitle();

  @Nullable
  public abstract String getSender();

  @Nullable
  public abstract String getUrl();


  public static Builder builder() {
    return new AutoValue_Card.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setContent(String content);

    public abstract Builder setLogo(String logo);

    public abstract Builder setTitle(String title);

    public abstract Builder setSender(String sender);

    public abstract Builder setUrl(String url);

    public abstract Card build();
  }
}
