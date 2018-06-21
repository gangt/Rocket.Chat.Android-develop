package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import java.util.List;

import javax.annotation.Nullable;

@AutoValue
public abstract class Report {

  @Nullable
  public abstract String getTheme();

  @Nullable
  public  abstract List<ReportList> getReportList();

  public static Builder builder() {
    return new AutoValue_Report.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setTheme(String theme);

    public abstract Builder setReportList(List<ReportList> reportList);

    public abstract Report build();
  }
}
