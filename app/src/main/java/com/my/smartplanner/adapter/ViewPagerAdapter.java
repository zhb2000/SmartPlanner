package com.my.smartplanner.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

/*为ViewPager准备的适配器类*/
public class ViewPagerAdapter extends FragmentPagerAdapter {
    public List<Fragment> fragments;//存放碎片的列表
    private List<String> titles;//存放tab栏标题的列表

    /*构造方法*/
    public ViewPagerAdapter(FragmentManager fm, List<Fragment> fragments, List<String> titles) {
        super(fm, BEHAVIOR_SET_USER_VISIBLE_HINT);
        this.fragments = fragments;
        this.titles = titles;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}
