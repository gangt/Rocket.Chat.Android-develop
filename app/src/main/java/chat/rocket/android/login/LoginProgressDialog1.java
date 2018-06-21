package chat.rocket.android.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import chat.rocket.android.R;


public class LoginProgressDialog1 extends ProgressDialog {

    public LoginProgressDialog1(Context context) {
        super(context);
    }
    String msg;
    public LoginProgressDialog1(Context context, int theme,String msg) {
        super(context, theme);
        this.msg=msg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init(getContext());
    }

    private void init(Context context) {
        //设置不可取消，点击其他区域不能取消，实际中可以抽出去封装供外包设置
//        setCancelable(false);
        setCanceledOnTouchOutside(false);

        setContentView(R.layout.load_dialog_login1);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);
       TextView text_msg=findViewById(R.id.tv_msg);
        text_msg.setText(this.msg);
    }

    @Override
    public void show() {
        super.show();
    }

}
