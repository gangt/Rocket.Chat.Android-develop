package chat.rocket.android.helper;

import org.json.JSONArray;
import org.json.JSONException;

import chat.rocket.android.widget.helper.AudioHelper;

/**
 * Created by jumper_C on 2018/6/11.
 */

public class JsonHelper {
    private static JsonHelper mInstance;
    /**
     * 获取单例引用
     *
     * @return
     */
    public static JsonHelper getInstance() {
        if (mInstance == null) {
            synchronized (AudioHelper.class) {
                if (mInstance == null) {
                    mInstance = new JsonHelper();
                }
            }
        }
        return mInstance;
    }
    public  JSONArray  remove(JSONArray jsonArray, int index){
        JSONArray mJsonArray  = new JSONArray();

        if(index<0)    return mJsonArray;
        if(index>jsonArray.length())   return mJsonArray;

        for( int i=0;i<index;i++){
            try {
                mJsonArray.put(jsonArray.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for( int i=index+1;i< jsonArray.length();i++){
            try {
                mJsonArray.put(jsonArray.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return mJsonArray;
    }
}
