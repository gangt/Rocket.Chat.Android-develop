package chat.rocket.android.helper;

import android.os.Build;
import android.text.format.DateFormat;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import chat.rocket.android.log.RCLog;

/**
 * Utility class for converting epoch ms and date-time string.
 */
public class DateTime {
  private static final String TAG = "DateTime";

  private static final SimpleDateFormat TIME_FORMAT;
  private static final SimpleDateFormat DATE_FORMAT;
  private static final SimpleDateFormat DAY_FORMAT;
  private static final SimpleDateFormat DAY_TIME_FORMAT;
  private static final SimpleDateFormat DATE_TIME_FORMAT;
  private static final SimpleDateFormat DATE_TIME_FORMAT2;
  private static final SimpleDateFormat DATE_TIME_FORMAT3;
  private static final SimpleDateFormat DATE_TIME_FORMAT4;
  private static final SimpleDateFormat DATE_TIME_FORMAT5;

  static {
    Locale locale = Locale.getDefault();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      TIME_FORMAT = new SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "HHmm"), locale);
      DATE_FORMAT =
          new SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "yyyyMMdd"), locale);
      DAY_FORMAT = new SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "MMdd"), locale);
      DAY_TIME_FORMAT =
          new SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "MMddHHmm"), locale);
      DATE_TIME_FORMAT =
          new SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "yyyyMMddHHmm"), locale);
    } else {
      TIME_FORMAT = new SimpleDateFormat("HH:mm", locale);
      DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd", locale);
      DAY_FORMAT = new SimpleDateFormat("MM/dd", locale);
      DAY_TIME_FORMAT = new SimpleDateFormat("MM/dd HH:mm", locale);
      DATE_TIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm", locale);
    }
    DATE_TIME_FORMAT2 = new SimpleDateFormat("yyyy-MM-dd HH:mm", locale);
    DATE_TIME_FORMAT3 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA);
    DATE_TIME_FORMAT4 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
    DATE_TIME_FORMAT5 = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);

  }

  /**
   * convert datetime ms to String.
   */
  public static String fromEpocMs(long epocMs, Format format) {
    Calendar cal = new GregorianCalendar();
    cal.setTimeInMillis(epocMs);

    switch (format) {
      case DAY:
        return DAY_FORMAT.format(cal.getTime());
      case DATE:
        return getDateFormat(cal.getTime());
      case DATE1:
        return getDateFormat1(cal.getTime());
      case DATE3:
        return getDateFormat3(cal.getTime());
      case TIME:
        return TIME_FORMAT.format(cal.getTime());
      case DATE_TIME:
        return DATE_TIME_FORMAT.format(cal.getTime());
      case DATE_TIME2:
        return DATE_TIME_FORMAT2.format(cal.getTime());
      case DATE_TIME3:
        return DATE_TIME_FORMAT3.format(cal.getTime());
      case DATE_TIME4:
        return DATE_TIME_FORMAT4.format(cal.getTime());
      case DAY_TIME:
        return DAY_TIME_FORMAT.format(cal.getTime());
      case AUTO_DAY_TIME: {
        final long curTimeMs = System.currentTimeMillis();
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("JST"));
        cal2.setTimeInMillis(curTimeMs);

        if (cal.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
            && cal.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {
          //same day.
          return DAY_TIME_FORMAT.format(cal.getTime());
        } else {
          return DAY_FORMAT.format(cal.getTime());
        }
      }
      default:
        throw new IllegalArgumentException();
    }
  }
  private static String getDateFormat3(Date dateTime) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(dateTime);
    Calendar today = Calendar.getInstance();
    Calendar yesterday = Calendar.getInstance();
    yesterday.add(Calendar.DATE, -1);

    if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
      return TIME_FORMAT.format(dateTime);
    } else if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
      return "昨天";
    } else {
      return DATE_FORMAT.format(dateTime);
    }
  }
  private static String getDateFormat(Date dateTime) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(dateTime);
      Calendar today = Calendar.getInstance();
      Calendar yesterday = Calendar.getInstance();
      yesterday.add(Calendar.DATE, -1);

      if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
        return "今日";
      } else if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
        return "昨天";
      } else {
        return DATE_TIME_FORMAT5.format(dateTime);
      }
  }

  private static String getDateFormat1(Date dateTime) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(dateTime);
      Calendar today = Calendar.getInstance();
      Calendar yesterday = Calendar.getInstance();
      yesterday.add(Calendar.DATE, -1);

      if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
        return calendar.get(Calendar.HOUR_OF_DAY)+":"+AddZero(calendar.get(Calendar.MINUTE));
      } else if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
        return "昨天";
      } else {
        return DATE_TIME_FORMAT5.format(dateTime);
      }
  }

  public static String AddZero(int str1){
    DecimalFormat df=new DecimalFormat("00");
   return df.format(str1);
  }
  /**
   * parse datetime string to ms.
   */
  public static long fromDateToEpocMs(String dateString) {
    try {
      Calendar cal = new GregorianCalendar();
      cal.setTime(DATE_FORMAT.parse(dateString));
      return cal.getTimeInMillis();
    } catch (ParseException exception) {
      RCLog.w(exception, "failed to parse date: %s", dateString);
    }
    return 0;
  }

  public static Date fromStringToDate(String dateString) {
    try {
      Calendar cal = new GregorianCalendar();
      cal.setTime(DATE_TIME_FORMAT2.parse(dateString));
      return cal.getTime();
    } catch (ParseException exception) {
      RCLog.w(exception, "failed to parse date: %s", dateString);
    }
    return null;
  }

  public static long fromStringToLong(String dateString) {
    try {
      Calendar cal = new GregorianCalendar();
      cal.setTime(DATE_TIME_FORMAT2.parse(dateString));
      return cal.getTimeInMillis();
    } catch (ParseException exception) {
      RCLog.w(exception, "failed to parse date: %s", dateString);
    }
    return 0;
  }

  /**
   * format.
   */
  public enum Format {
    DATE,DATE3,DATE1, DAY, TIME, DATE_TIME, DATE_TIME2, DATE_TIME3,DATE_TIME4,DAY_TIME, AUTO_DAY_TIME
  }
}