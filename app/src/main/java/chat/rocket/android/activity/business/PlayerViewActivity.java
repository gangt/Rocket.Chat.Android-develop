package chat.rocket.android.activity.business;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.shuyu.gsyvideoplayer.GSYVideoManager;

import java.util.ArrayList;
import java.util.Collections;

import chat.rocket.android.R;
import chat.rocket.android.adapter.PlayerViewAdapter;
import chat.rocket.android.entity.VideoBean;
import chat.rocket.android.log.RCLog;
import cn.jzvd.JZVideoPlayer;

/**
 * Created by helloworld on 2018/3/13
 */

public class PlayerViewActivity extends AppCompatActivity {

    private ViewPager vp_player;
    private ArrayList<VideoBean> video_url;

    public void startActivity(){

    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.player_viewpager);

        vp_player = findViewById(R.id.vp_player);
        video_url = getIntent().getParcelableArrayListExtra("video_url");
//        Collections.reverse(video_url);
        int selectPosition = getIntent().getIntExtra("selectPosition", 0);
        PlayerViewAdapter viewAdapter = new PlayerViewAdapter(video_url, this);
        vp_player.setAdapter(viewAdapter);
        vp_player.setCurrentItem(selectPosition);
        vp_player.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                RCLog.d("onPageSelected", true);
                GSYVideoManager.releaseAllVideos();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


    }

    public int getCurrentItem(){
        return vp_player.getCurrentItem();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GSYVideoManager.releaseAllVideos();
    }

}
