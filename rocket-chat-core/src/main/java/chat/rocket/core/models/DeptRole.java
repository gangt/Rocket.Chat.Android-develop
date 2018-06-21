package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class DeptRole {

  @Nullable
  public abstract String getOrg_path_name();
  @Nullable
  public abstract String getPos_desc();
  @Nullable
  public abstract String getCode();
  @Nullable
  public abstract String getPos_name();
  @Nullable
  public abstract String getPos_code();
  @Nullable
  public abstract String getOrg_desc();
  @Nullable
  public abstract String getOrg_name();
  @Nullable
  public abstract String getOrg_code();
  @Nullable
  public abstract String getPos_id();
  @Nullable
  public abstract String getOrg_id();

  public static Builder builder() {
    return new AutoValue_DeptRole.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setOrg_path_name(String address);
    public abstract Builder setPos_desc(String address);
    public abstract Builder setCode(String address);
    public abstract Builder setPos_name(String address);
    public abstract Builder setOrg_desc(String address);
    public abstract Builder setOrg_name(String address);
    public abstract Builder setPos_code(String address);
    public abstract Builder setOrg_code(String address);
    public abstract Builder setPos_id(String address);
    public abstract Builder setOrg_id(String address);

    public abstract DeptRole build();
  }
}
