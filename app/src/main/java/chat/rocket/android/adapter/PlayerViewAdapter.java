package chat.rocket.android.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestFutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.NormalGSYVideoPlayer;

import java.util.ArrayList;

import chat.rocket.android.R;
import chat.rocket.android.RocketChatApplication;
import chat.rocket.android.activity.business.PlayerViewActivity;
import chat.rocket.android.entity.VideoBean;
import chat.rocket.android.widget.emotionkeyboard.utils.DisplayUtils;
import cn.jzvd.JZVideoPlayer;
import cn.jzvd.JZVideoPlayerStandard;

/**
 * Created by helloworld on 2018/3/13
 */

public class PlayerViewAdapter extends PagerAdapter {

    private ArrayList<VideoBean> urlLists;
    private PlayerViewActivity mContext;

    public PlayerViewAdapter(ArrayList<VideoBean> lists, PlayerViewActivity mContext) {
        urlLists = lists;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return urlLists.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        JZVideoPlayer.releaseAllVideos();
        container.removeView((View) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        VideoBean bean = urlLists.get(position);
        View view = null;
        if (VideoBean.VIDEO.equals(bean.type)) {
            view = View.inflate(mContext, R.layout.jc_videoplayer, null);

            NormalGSYVideoPlayer mJcVideoPlayerStandard = view.findViewById(R.id.videoplayer);
            mJcVideoPlayerStandard.getBackButton().setOnClickListener(v -> mContext.finish() );
            GSYVideoManager.instance().setVideoType(container.getContext() , GSYVideoType.IJKEXOPLAYER2);
            ImageView imageView = new ImageView(container.getContext());
            loadCover(imageView,bean.url,container.getContext());
            JZVideoPlayer.SAVE_PROGRESS = false;
            mJcVideoPlayerStandard.setThumbImageView(imageView);
            mJcVideoPlayerStandard.setUp(bean.url,true,"");
//            mJcVideoPlayerStandard.setUp(bean.url, JZVideoPlayerStandard.SCREEN_WINDOW_FULLSCREEN, "");
//
//            ViewGroup.LayoutParams params = mJcVideoPlayerStandard.backButton.getLayoutParams();
//            params.height = DisplayUtils.dp2px(mContext, 50);
//            params.width = DisplayUtils.dp2px(mContext, 45);
//            mJcVideoPlayerStandard.backButton.setLayoutParams(params);
//            mJcVideoPlayerStandard.backButton.setScaleType(ImageView.ScaleType.CENTER);
//            mJcVideoPlayerStandard.backButton.setOnClickListener(v -> mContext.finish());
//            mJcVideoPlayerStandard.batteryTimeLayout.setVisibility(View.GONE);
//            mJcVideoPlayerStandard.fullscreenButton.setVisibility(View.GONE);
//
//            if(position == mContext.getCurrentItem()){
//                mJcVideoPlayerStandard.startButton.performClick();
//            }
            container.addView(view);
        }
        else {
            view = View.inflate(mContext, R.layout.viewpager_image, null);
            ImageView iv_image = view.findViewById(R.id.iv_image);
            Glide.with(container.getContext()).load(bean.url).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            }).into(iv_image);
            view.findViewById(R.id.iv_back).setOnClickListener(v -> mContext.finish());
            container.addView(view);
        }
        return view;
    }
    private void loadCover(ImageView imageView, String url, Context context) {

        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        Glide.with(context)
                .setDefaultRequestOptions(
                        new RequestOptions()
                                .frame(1)
                                .fitCenter()
                                .error(R.drawable.send_message_default)
                                .placeholder(R.drawable.send_message_default)
                )
                .load(url)
                .into(imageView);
    }
}
