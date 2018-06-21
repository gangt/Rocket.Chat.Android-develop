package chat.rocket.android.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by ${xiugao} on 2017/12/30 0030.
 */

public class MySlidingPaneLayout extends SlidingPaneLayout {

    private float mInitialMotionX;
    private float mEdgeSlop = 300;

    public MySlidingPaneLayout(@NonNull Context context) {
        super(context);
    }

    public MySlidingPaneLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MySlidingPaneLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN:
                mInitialMotionX = ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mInitialMotionX > mEdgeSlop && !isOpen()) {
                    MotionEvent cancelEvent = MotionEvent.obtain(ev);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                    return super.onInterceptTouchEvent(cancelEvent);
                }
        }
        return super.onInterceptTouchEvent(ev);
    }
}
