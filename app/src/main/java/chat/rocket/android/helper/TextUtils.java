package chat.rocket.android.helper;

import java.util.List;

/**
 * Text Utility class like android.text.TextUtils.
 */
public class TextUtils {

    /**
     * Returns true if the string is null or 0-length.
     *
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    public static boolean isEmpty(CharSequence str) {
        // same definition as android.text.TextUtils#isEmpty().
        return str == null || str.length() == 0;
    }

    /**
     * Returns str if it is not empty; otherwise defaultValue is returned.
     */
    @SuppressWarnings("PMD.ShortMethodName")
    public static CharSequence or(CharSequence str,
                                  CharSequence defaultValue) {
        if (isEmpty(str)) {
            return defaultValue;
        }
        return str;
    }

    public static boolean isEmpty(List list) {
        return list == null || list.size() == 0;
    }

    /**
     * 分割username，按照&切割
     *
     * @param text
     * @return
     */
    public static String splitUsername(String text) {
        if(isEmpty(text)) return "";
        int indexOf = text.lastIndexOf("&");
        if(indexOf == -1) return text;
        return text.substring(0, indexOf);
    }
}
