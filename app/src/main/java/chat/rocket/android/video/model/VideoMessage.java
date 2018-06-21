package chat.rocket.android.video.model;

/**
 * Created by Administrator on 2018/5/14/014.
 */

public class VideoMessage {

    public String nType ;
    public String mediald;
    public String status ;
    public String receiveMsg;
    public String fromMsg ;

    private VideoMessage(Builder builder){
        this.nType = builder.nType ;
        this.mediald = builder.mediald ;
        this.status = builder.status ;
        this.receiveMsg = builder.receiveMsg ;
        this.fromMsg = builder.fromMsg ;
    }

    public static class Builder{
        private String nType ;
        private String mediald;
        private String status ;
        private String receiveMsg;
        private String fromMsg ;

        public String getnType() {
            return nType;
        }

        public Builder setnType(String nType) {
            this.nType = nType;
            return  this ;
        }

        public String getMediald() {
            return mediald;
        }

        public Builder setMediald(String mediald) {
            this.mediald = mediald;
            return  this ;
        }

        public String getStatus() {
            return status;
        }

        public Builder setStatus(String status) {
            this.status = status;
            return  this ;
        }

        public String getReceiveMsg() {
            return receiveMsg;
        }

        public Builder setReceiveMsg(String receiveMsg) {
            this.receiveMsg = receiveMsg;
            return  this ;
        }

        public String getFromMsg() {
            return fromMsg;
        }

        public Builder setFromMsg(String formMsg) {
            this.fromMsg = formMsg;
            return  this ;
        }

        public VideoMessage build(){
            return new VideoMessage(this);
        }
    }

}
