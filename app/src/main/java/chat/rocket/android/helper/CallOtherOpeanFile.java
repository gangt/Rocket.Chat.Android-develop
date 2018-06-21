package chat.rocket.android.helper;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.File;

import chat.rocket.android.ToastUtils;

/**
 * Created by helloworld on 2018/3/10
 */

public class CallOtherOpeanFile {

    private static String [][]  MIME_MapTable={
            //{后缀名，MIME类型}
            {".3gp",    "video/3gpp"},
            {".apk",    "application/vnd.android.package-archive"},
            {".asf",    "video/x-ms-asf"},
            {".avi",    "video/x-msvideo"},
            {".bmp",    "image/bmp"},
            {".c",  "text/plain"},
            {".conf",   "text/plain"},
            {".cpp",    "text/plain"},
            {".doc",    "application/msword"},
            {".docx",   "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls",    "application/vnd.ms-excel"},
            {".xlsx",   "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".gif",    "image/gif"},
            {".h",  "text/plain"},
            {".htm",    "text/html"},
            {".html",   "text/html"},
            {".jar",    "application/java-archive"},
            {".java",   "text/plain"},
            {".jpeg",   "image/jpeg"},
            {".jpg",    "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log",    "text/plain"},
            {".m4a",    "audio/mp4a-latm"},
            {".m4b",    "audio/mp4a-latm"},
            {".m4p",    "audio/mp4a-latm"},
            {".m4u",    "video/vnd.mpegurl"},
            {".m4v",    "video/x-m4v"},
            {".mov",    "video/quicktime"},
            {".mp3",    "audio/x-mpeg"},
            {".mp4",    "video/mp4"},
            {".mpe",    "video/mpeg"},
            {".mpeg",   "video/mpeg"},
            {".mpg",    "video/mpeg"},
            {".mpg4",   "video/mp4"},
            {".ogg",    "audio/ogg"},
            {".pdf",    "application/pdf"},
            {".png",    "image/png"},
            {".pps",    "application/vnd.ms-powerpoint"},
            {".ppt",    "application/vnd.ms-powerpoint"},
            {".pptx",    "application/vnd.ms-powerpoint"},
            {".prop",   "text/plain"},
            {".rmvb",   "audio/x-pn-realaudio"},
            {".txt",    "text/plain"},
            {".wav",    "audio/x-wav"},
            {".wma",    "audio/x-ms-wma"},
            {".wmv",    "audio/x-ms-wmv"},
            {".wps",    "application/vnd.ms-works"},
            {".xml",    "text/plain"},
            {".zip",    "application/x-zip-compressed"}
    };

    public static void openFile(Context context, File file){
        try{
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //设置intent的Action属性
            intent.setAction(Intent.ACTION_VIEW);
            //获取文件file的MIME类型
            String type = getMIMEType(file);
            if(TextUtils.isEmpty(type)){
                ToastUtils.showToast( "附件不能打开，请下载相关软件！");
            }else{
                //设置intent的data和Type属性。
                Uri uri=null;
                if (Build.VERSION.SDK_INT >= 24) {
                    uri = FileProvider.getUriForFile(context,
                            context.getApplicationContext().getPackageName() + ".provider", file);
                    context.grantUriPermission(context.getApplicationContext().getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//增加读权限
                } else {
                    uri = Uri.fromFile(file);
                }
                intent.setDataAndType(uri, type);
                context.startActivity(intent);
            }
        }catch (ActivityNotFoundException e) {
            ToastUtils.showToast( "附件不能打开，请下载相关软件！");
        }
    }

    private static String getMIMEType(File file) {
        String type="";
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if(dotIndex < 0){
            return type;
        }
        /* 获取文件的后缀名*/
        String end=fName.substring(dotIndex,fName.length()).toLowerCase();
        if(TextUtils.isEmpty(end)) return "text/plain";
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for(int i=0; i<MIME_MapTable.length;i++){
            if(end.equals(MIME_MapTable[i][0]))
            type = MIME_MapTable[i][1];
        }
        return type;
    }

}
