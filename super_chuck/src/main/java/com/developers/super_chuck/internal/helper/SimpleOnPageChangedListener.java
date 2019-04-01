package com.developers.super_chuck.internal.helper;

import android.support.v4.view.ViewPager;

/**
 * @Author yinzh
 * @Date 2019/4/1 17:57
 * @Description
 */
public abstract class SimpleOnPageChangedListener implements ViewPager.OnPageChangeListener {
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
    @Override
    public abstract void onPageSelected(int position);
    @Override
    public void onPageScrollStateChanged(int state) {}
}
