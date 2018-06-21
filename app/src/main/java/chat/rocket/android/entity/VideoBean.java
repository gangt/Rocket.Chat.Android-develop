package chat.rocket.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by helloworld on 2018/3/13
 */

public class VideoBean implements Parcelable {

    public static final String VIDEO = "VIDEO";
    public static final String PICTURE = "PICTURE";

    public String type;

    public String url;

    public VideoBean(String type, String from) {
        this.type = type;
        this.url = from;
    }

    private VideoBean(Parcel in) {
        type = in.readString();
        url = in.readString();
    }

    public static final Creator<VideoBean> CREATOR = new Creator<VideoBean>() {
        @Override
        public VideoBean createFromParcel(Parcel in) {
            return new VideoBean(in);
        }

        @Override
        public VideoBean[] newArray(int size) {
            return new VideoBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeString(url);
    }
}
