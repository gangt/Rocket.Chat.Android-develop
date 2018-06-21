package chat.rocket.android.video.view;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import butterknife.ButterKnife;
import chat.rocket.android.R;


public class VideoFloatingView extends LinearLayout {
    public static final String TAG = "FloatingView";

    private final Context mContext;
    private ViewManager viewManager ;

    public VideoFloatingView(ViewManager viewManager , Context context) {
        super(context);
        this.viewManager = viewManager ;
        mContext = context;
        initView();
    }

    private void initView() {
        View view = inflate(mContext, R.layout.video_small_view, this);
        ButterKnife.bind(view);

       /* findViewById(R.id.closeDialog).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener !=null) {
                    listener.small();
                }
            }
        });*/
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
