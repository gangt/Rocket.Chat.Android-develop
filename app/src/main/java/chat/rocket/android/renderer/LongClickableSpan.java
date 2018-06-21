package chat.rocket.android.renderer;

import android.text.style.ClickableSpan;
import android.view.View;

/**
 * 超链接点击和长按事件
 * Created by river on 2015/9/1.
 */
public abstract class LongClickableSpan extends ClickableSpan {
    /**
     * 长按事件
     * @param widget
     */
    public abstract void onLongClick(View widget);
}
