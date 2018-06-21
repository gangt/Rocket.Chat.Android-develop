package chat.rocket.android.widget.helper;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

/**
 * Created by helloworld on 2018/3/22
 */

public class GetNetVideoBitmap extends AsyncTask<Void, Void, Bitmap>{

    private String videoUrl;
    private ImageView thumbImageView;
    private String absFileName;

    public GetNetVideoBitmap(String videoUrl, ImageView thumbImageView, String absFileName) {
        this.videoUrl = videoUrl;
        this.thumbImageView = thumbImageView;
        this.absFileName = absFileName;
    }

    @Override
    protected Bitmap doInBackground(Void... v) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            //获取网络视频
            retriever.setDataSource(videoUrl, new HashMap<String, String>());
            //获取本地视频
            //retriever.setDataSource(url);
            Bitmap bitmap = retriever.getFrameAtTime();
            FileOutputStream outStream = null;
            File file = new File(absFileName);
            if(!file.exists()) {
                boolean newFile = file.createNewFile();
                Log.e("rocket chat", "-------->"+newFile);
            }
            outStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, outStream);

            return bitmap;
        } catch (Exception e) {
            Log.e("rocket chat", e.toString());
            e.printStackTrace();
        } finally {
            retriever.release();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        if(bitmap != null){
            thumbImageView.setImageBitmap(bitmap);
        }
    }

}
