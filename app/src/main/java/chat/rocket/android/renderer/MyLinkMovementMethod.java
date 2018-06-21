package chat.rocket.android.renderer;

/**
 * Created by jumper_C on 2018/5/28.
 */


import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by river on 2015/9/1.
 */
public class MyLinkMovementMethod extends LinkMovementMethod {
    private long lastClickTime;

    private static final long CLICK_DELAY = 300;

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            LongClickableSpan[] link = buffer.getSpans(off, off, LongClickableSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {

                    if (System.currentTimeMillis() - lastClickTime < CLICK_DELAY) {
//                        Log.e("test", "点击");
                        //点击事件
                        link[0].onClick(widget);

                    } else {
//                        Log.e("test", "长按");
                        //长按事件
                        link[0].onLongClick(widget);

                    }

                } else if (action == MotionEvent.ACTION_DOWN) {
//                    Selection.setSelection(buffer,
//                            buffer.getSpanStart(link[0]),
//                            buffer.getSpanEnd(link[0]));
                    lastClickTime = System.currentTimeMillis();
                }

                return true;
            } else {
                Selection.removeSelection(buffer);
            }
        }
        return super.onTouchEvent(widget, buffer, event);
    }

}
