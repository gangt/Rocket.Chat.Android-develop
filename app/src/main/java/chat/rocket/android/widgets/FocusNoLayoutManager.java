package chat.rocket.android.widgets;


import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by cross on 2018/1/31.
 * <p>描述:简单的处理recyclerView莫名滑动bug
 * TODO 待观察
 */

public class FocusNoLayoutManager extends LinearLayoutManager {

    private static final String TAG = FocusNoLayoutManager.class.getName();

    public FocusNoLayoutManager(Context context) {
        super(context);
    }

    public FocusNoLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public FocusNoLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate) {
        return this.requestChildRectangleOnScreen(parent,child,rect,immediate,false);
    }

    public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate, boolean focusedChildVisible) {
        return false;
    }
}
