package chat.rocket.android.video.helper;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import chat.rocket.android.video.model.VideoRequestModel;

/**
 * Created by Administrator on 2018/5/9/009.
 */

public class TempFileUtils {

    private String time ;
    private boolean talkingStatus ;

    private static TempFileUtils instance ;
    private Bundle bundle ;
    private  String currentKey ;
    private  String currentMediaId ;
    private  VideoRequestModel model ;
    private HashMap<String,String> hashMap = new HashMap<>();
//    Stack<String> stack = new Stack<>();
    List<String> stack = new ArrayList<>();
    private TempFileUtils(){}

    public static TempFileUtils getInstance(){
        if(instance == null){
            instance = new TempFileUtils();
        }
        return instance ;
    }

    public  String getTime() {
        return time;
    }

    public TempFileUtils setTime(String time) {
        this.time = time;
        return instance ;
    }

    public boolean getTalkingStatus() {
        return talkingStatus;
    }

    public TempFileUtils setTalkingStatus(boolean talkingStatus) {
        this.talkingStatus = talkingStatus;
        return instance ;
    }

    public void saveRecentVideoByMediaId(String mediaId,String status){
        hashMap.clear();
        hashMap.put(mediaId,status);
    }

    public String getMsgByMediaId(String mediaId){
        if(mediaId == null){
            return null;
        }
        return hashMap.get(mediaId);
    }

    public void saveCallingUserId(String userId){
        stack.clear();
        stack.add(userId);
    }

    public void setBundleData(Bundle bundle){
        this.bundle = bundle ;
    }

    public Bundle getBundleData(){
        return bundle ;
    }


    public  String getCallingUserId(){
        return stack.size()>0 ? stack.get(0)  :"";
    }

    public  void saveCurrentKey(String currenttKey){
        this.currentKey = currenttKey ;
    }

    public String getCurrentKey(){
        return currentKey ;
    }

    public void saveCurrentMediaId(String mediaId){
        this.currentMediaId = mediaId ;
    }

    public  String getMediaId(){
        return currentMediaId ;
    }

    public void saveCallingRequest(VideoRequestModel model){
        this.model = model ;
    }

    public VideoRequestModel getCallingRequest(){
        return  model ;
    }

}
