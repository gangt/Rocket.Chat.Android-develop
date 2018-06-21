package chat.rocket.android.log;

import android.util.Log;

public class RCLog {


  /**
   * isDebug :是用来控制，是否打印日志
   */
  private static final boolean isDeBug = true;

  private static final String myDefaultLog = "myDebug";

  public static void v(String log, boolean isDefultLog) {
    if(isDeBug && isDefultLog) {
      Log.v(myDefaultLog, log);
    }
  }

  public static void d(String log, boolean isDefultLog) {
    if(isDeBug && isDefultLog) {
      Log.d(myDefaultLog, log);
    }
  }

  public static void d(String log, Object... args) {
    if(isDeBug) Log.d(getTag(), String.format(log, args));
  }

  public static void d(Throwable throwable) {
    if(isDeBug) Log.d(getTag(), throwable.getMessage(), throwable);
  }

  public static void d(Throwable throwable, String log, Object... args) {
    if(isDeBug) Log.d(getTag(), String.format(log, args), throwable);
  }

  public static void w(String log, Object... args) {
    if(isDeBug) Log.w(getTag(), String.format(log, args));
  }

  public static void w(Throwable throwable) {
    if(isDeBug) Log.w(getTag(), throwable.getMessage(), throwable);
  }

  public static void w(Throwable throwable, String log, Object... args) {
    if(isDeBug) Log.w(getTag(), String.format(log, args), throwable);
  }

  public static void e(String log, boolean isDefultLog) {
    if(isDeBug && isDefultLog) {
      Log.e(myDefaultLog, log);
    }
  }

  public static void e(String log, Object... args) {
    if(isDeBug) Log.e(getTag(), String.format(log, args));
  }

  public static void e(Throwable throwable) {
    if(isDeBug) Log.e(getTag(), throwable.getMessage(), throwable);
  }

  public static void e(Throwable throwable, String log, Object... args) {
    if(isDeBug) Log.e(getTag(), String.format(log, args), throwable);
  }

  private static String getTag() {
    StackTraceElement[] elements = Thread.currentThread().getStackTrace();
    if (elements.length >= 5) {
      return getSimpleName(elements[4].getClassName());
    } else {
      return "Rocket.Chat";
    }
  }

  private static String getSimpleName(String className) {
    int idx = className.lastIndexOf(".");
    return className.substring(idx + 1);
  }
}
