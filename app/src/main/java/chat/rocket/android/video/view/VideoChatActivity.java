package chat.rocket.android.video.view;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import chat.rocket.android.LaunchUtil;
import chat.rocket.android.R;
import chat.rocket.android.activity.ChatMainActivity;
import chat.rocket.android.video.helper.TempFileUtils;

/**
 * Created by Administrator on 2018/5/29/029.
 */

public class VideoChatActivity extends Activity {
    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1;
    ViewManager manager;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

//        TempFileUtils.getInstance().saveCallingRequest(null);
        if (TempFileUtils.getInstance().getTalkingStatus()) {
            finish();
            return;
        }
        boolean isFlag = checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)
                && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA);
        if (isFlag) {
            gotoInviteActivity();
        }
    }

    private void gotoInviteActivity() {
        Intent intent = new Intent(VideoChatActivity.this, VideoService.class);
        intent.putExtras(getIntent().getExtras());
        startService(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (!Settings.canDrawOverlays(this)) {
                    CustomToast.showToastInfo(R.string.permission_faild);
                    finish();
                } else {
                    gotoInviteActivity();
                }
            }
        }
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA);
                } else {
                    finish();
                }
                break;
            }
            case PERMISSION_REQ_ID_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    gotoInviteActivity();
//                    checkSystenPermission();
                    gotoInviteActivity();
                } else {
                    finish();
                }
                break;
            }
        }
    }
}
