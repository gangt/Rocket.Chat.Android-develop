package chat.rocket.android.widget.helper;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.util.Locale;

/**
 * Created by helloworld on 2018/3/20
 */

public class FileUtils {

    public static boolean isVideo(String link){
        if(TextUtils.isEmpty(link)) return false;
        link = link.toLowerCase();
        return !TextUtils.isEmpty(link) && link.endsWith(".mp4");
    }

    public static boolean isPhoto(String link){
        if(TextUtils.isEmpty(link)) return false;
        link = link.toLowerCase();
        return link.endsWith(".bmp") || link.endsWith(".jpg") || link.endsWith(".png") || link.endsWith(".jpeg")||link.endsWith(".webp")||link.endsWith(".gif");
    }

    public static boolean isAudio(String link){
        if(TextUtils.isEmpty(link)) return false;
        link = link.toLowerCase();
        return link.endsWith(".m4a") ;
    }

    public static boolean isExcel(String link){
        if(TextUtils.isEmpty(link)) return false;
        link = link.toLowerCase();
        return link.endsWith(".xlsx") || link.endsWith(".xls");
    }

    public static boolean isWord(String link){
        if(TextUtils.isEmpty(link)) return false;
        link = link.toLowerCase();
        return link.endsWith(".docx") || link.endsWith(".doc");
    }

    public static boolean isPPT(String link){
        if(TextUtils.isEmpty(link)) return false;
        link = link.toLowerCase();
        return link.endsWith(".ppt") || link.endsWith(".pptx");
    }

    public static boolean isPDF(String link){
        if(TextUtils.isEmpty(link)) return false;
        link = link.toLowerCase();
        return link.endsWith(".pdf") ;
    }
    public static boolean isTxt(String link){
        if(TextUtils.isEmpty(link)) return false;
        link = link.toLowerCase();
        return link.endsWith(".txt") || link.endsWith(".log");
    }

    public static boolean isMp3(String link){
        if(TextUtils.isEmpty(link)) return false;
        link = link.toLowerCase();
        return link.endsWith(".mp3") ;
    }
    public static boolean isZip(String link){
        if(TextUtils.isEmpty(link)) return false;
        link = link.toLowerCase();
        return link.endsWith(".zip") ;
    }

    public static String getAppDownloadDir(Context mContext){
        return Environment.getExternalStorageDirectory()+"/"+mContext.getPackageName() + "/download/";
    }

    public static String getAppDownloadPhotoDir(Context mContext){
        return Environment.getExternalStorageDirectory()+"/"+mContext.getPackageName() + "/download/photo/";
    }

    public static String getAppDownloadCacheDir(Context mContext){
        return Environment.getExternalStorageDirectory()+"/"+mContext.getPackageName() + "/download/cache/";
    }

    public static String getFileExtensionFromUrl(String url) {
        String str="";
        if (!TextUtils.isEmpty(url)) {
            int fragment = url.lastIndexOf('#');
            if (fragment > 0) {
                url = url.substring(0, fragment);
            }

            int query = url.lastIndexOf('?');
            if (query > 0) {
                url = url.substring(0, query);
            }

            int filenamePos = url.lastIndexOf('/');
            String filename =
                    0 <= filenamePos ? url.substring(filenamePos + 1) : url;

            // if the filename contains special characters, we don't
            // consider it valid for our matching purposes:
            if (!filename.isEmpty() && filename.contains(".")) {
                int dotPos = filename.lastIndexOf('.');
                if (0 <= dotPos) {
                    str= filename.substring(dotPos + 1);
                }
            }
        }
        return str;
    }

    public static String getSuffix(File file) {
        if (file == null || !file.exists() || file.isDirectory()) {
            return null;
        }
        String fileName = file.getName();
        if (fileName.equals("") || fileName.endsWith(".")) {
            return null;
        }
        int index = fileName.lastIndexOf(".");
        if (index != -1) {
            return fileName.substring(index + 1).toLowerCase(Locale.US);
        } else {
            return null;
        }
    }

}
