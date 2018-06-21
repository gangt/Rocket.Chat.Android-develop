package chat.rocket.android.login;

public class UserInfoBean {

    /**
     * code : 1 / 0
     * message : 处理成功
     * data : {"_id":"E62pBbreMxxgPJnks","createdAt":{"$date":1514945539968},....
     */
    private String code;
    private String msg;
    private UserInfo data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public UserInfo getData() {
        return data;
    }

    public void setData(UserInfo data) {
        this.data = data;
    }
}
