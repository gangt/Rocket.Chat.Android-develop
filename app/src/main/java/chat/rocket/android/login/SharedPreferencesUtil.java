package chat.rocket.android.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;

/**
 * 配置文件工具�?
 * Created by 20150604 on 2015/10/22.
 */
public class SharedPreferencesUtil {

    private static String CONFIG = "config_communication";
    private static SharedPreferences sharedPreferences;

    /**
     * putObject()方法中，我们以对象的类名字作为key，以对象的json字符串作为value保存到SharePreference中。
     getObject()方法，我们先获取类的名字，再将它作为key，然后从SharePreference中获取对应的字符串，
     然后通过Gson将json字符串转化为对象。
     * @param context
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getObject(Context context, Class<T> clazz) {
        String key = getKey(clazz);
        String json = getStringData(context, key, null);
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        try {
            Gson gson = new Gson();
            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    public static void putObject(Context context, Object object) {
        String key = getKey(object.getClass());
        Gson gson = new Gson();
        String json = gson.toJson(object);
        putStringData(context, key, json);
    }

    public static void removeObject(Context context, Class<?> clazz){
        remove(context, getKey(clazz));
    }

    public static String getKey(Class<?> clazz) {
        return clazz.getName();
    }

    public static void remove(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.remove(key);
        edit.commit();
    }

    /**
     * 保存字符
     */
    public static void putStringData(Context context, String key, String value) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        sharedPreferences.edit().putString(key, value).commit();
    }

    /**
     * 获取字符
     */
    public static String getStringData(Context context, String key, String defValue) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        return sharedPreferences.getString(key, defValue);
    }

    /**
     * 保存数�?�型数据
     */
    public static void saveintData(Context context, String key, int value) {
        if (sharedPreferences == null && context != null) {
            sharedPreferences = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        sharedPreferences.edit().putInt(key, value).commit();
    }

    /**
     * 获取数�?�型数据
     */
    public static int getIntData(Context context, String key, int defValue) {
        if (sharedPreferences == null && context != null) {
            sharedPreferences = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        return sharedPreferences.getInt(key, defValue);
    }

    /**
     * 保存布尔值数�?
     */
    public static boolean getBooleanData(Context context, String key, boolean defValue) {
        if (sharedPreferences == null && context != null) {
            sharedPreferences = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        return sharedPreferences.getBoolean(key, defValue);
    }

    /**
     * 获取布尔值数�?
     */
    public static void saveBooleanData(Context context, String key, boolean value) {
        if (sharedPreferences == null && context != null) {
            sharedPreferences = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        sharedPreferences.edit().putBoolean(key, value).commit();
    }

    public static void clearAllPreferences(Context context) {
        if (sharedPreferences == null && context != null) {
            sharedPreferences = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
        }
        sharedPreferences.edit().clear();
        sharedPreferences.edit().clear().commit();
    }
}
