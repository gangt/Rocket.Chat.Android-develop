package chat.rocket.android.video.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by Administrator on 2018/5/29/029.
 */

public class FloatingVideoActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gotoInviteView();
    }

    private void gotoInviteView() {
        Intent intent = new Intent(this, VideoService.class);
        intent.putExtra("showView",getIntent().getExtras());
        startService(intent);
    }
}
