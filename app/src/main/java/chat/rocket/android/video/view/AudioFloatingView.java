package chat.rocket.android.video.view;

import android.content.Context;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import chat.rocket.android.R;


public class AudioFloatingView extends LinearLayout {
    public static final String TAG = "FloatingView";

    private final Context mContext;
    public ViewManager viewManager ;
    public TextView audioScaleTime ;

    public AudioFloatingView(ViewManager viewManager , Context context) {
        super(context);
        this.viewManager = viewManager ;
        mContext = context;
        initView();
    }

    private void initView() {
        inflate(mContext, R.layout.audio_small_view, this);
        findView();
        findViewById(R.id.audio_open_img).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                biggerImg();
            }
        });
    }

    private void findView() {
         audioScaleTime = findViewById(R.id.audio_scale_time);
    }


    public void biggerImg(){
        viewManager.isVideo = false;
        viewManager.addView();
        viewManager.presenter.scaleImg(false);
    }


    Point preP, curP;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                preP = new Point((int)event.getRawX(), (int)event.getRawY());
                break;

            case MotionEvent.ACTION_MOVE:
                curP = new Point((int)event.getRawX(), (int)event.getRawY());
                int dx = curP.x - preP.x,
                        dy = curP.y - preP.y;

                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) this.getLayoutParams();
                layoutParams.x -= dx;
                layoutParams.y += dy;
                viewManager.mWindowManager.updateViewLayout(this, layoutParams);

                preP = curP;
                break;
        }

        return false;
    }
}
