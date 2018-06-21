package chat.rocket.android.fragment.sidebar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Administrator on 2017/8/1.
 */

public class CustomViewpagerAdapter extends FragmentPagerAdapter {

    private List<BaseFragment> mFragments;

    public CustomViewpagerAdapter(FragmentManager fm, List<BaseFragment> fragments) {
        super(fm);
        this.mFragments=fragments;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if( getItem(position) instanceof BaseFragment)
            return ((BaseFragment) getItem(position)).getmTabName();
        return super.getPageTitle(position);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
