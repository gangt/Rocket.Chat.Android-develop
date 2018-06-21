package chat.rocket.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import chat.rocket.android.R;

/**
 * Created by user on 2018/1/11.
 */

public class WeiNingAlertDialog extends Dialog {
    private Context context;
    private boolean cancelTouchout;
    private OnClickListener listener;
    private String tip;
    private String title;

    private WeiNingAlertDialog(Builder builder) {
        super(builder.context);
        context = builder.context;
        cancelTouchout = builder.cancelTouchout;
        this.listener = builder.listener;
        this.title = builder.title;
        this.tip = builder.tip;
    }


    private WeiNingAlertDialog(Builder builder, int resStyle) {
        super(builder.context, resStyle);
        context = builder.context;
        cancelTouchout = builder.cancelTouchout;
        this.listener = builder.listener;
        this.title = builder.title;
        this.tip = builder.tip;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_weining_alert);
        setCanceledOnTouchOutside(cancelTouchout);
        TextView tvTitle = findViewById(R.id.tv_title);
        TextView tvTip = findViewById(R.id.tv_tip);
        Button btnConfirm=findViewById(R.id.btn_confirm);
        Button btnCancel=findViewById(R.id.btn_cancel);
        if (tip != null) {
            tvTip.setText(tip);
            tvTip.setVisibility(View.VISIBLE);
        }
        if(title!=null){
        tvTitle.setText(title);
        }
        btnConfirm.setOnClickListener(v->
            listener.conFirmClick()
        );
        btnCancel.setOnClickListener(v->
            listener.cancelClick()
        );
    }

    public interface OnClickListener {
        void cancelClick();
        void conFirmClick();
    }

    public static final class Builder {

        private Context context;
        private boolean cancelTouchout;
        private int resStyle = R.style.WeiNingAlertDialog;
        private OnClickListener listener;
        private String title;
        private String tip;


        public Builder(Context context) {
            this.context = context;
        }

        public Builder style(int resStyle) {
            this.resStyle = resStyle;
            return this;
        }

        public Builder cancelTouchout(boolean val) {
            cancelTouchout = val;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setTip(String tip) {
            this.tip = tip;
            return this;
        }

        public Builder addViewOnclick( OnClickListener listener) {
            this.listener = listener;
            return this;
        }


        public WeiNingAlertDialog build() {
            if (resStyle != -1) {
                return new WeiNingAlertDialog(this, resStyle);
            } else {
                return new WeiNingAlertDialog(this);
            }
        }
    }
}

