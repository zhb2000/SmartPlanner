package com.my.smartplanner.fragment;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.my.smartplanner.util.LogUtil;

import java.lang.ref.WeakReference;

public abstract class LazyLoadFragment extends BaseFragment {
    private boolean isInit = false;//是否已初始化
    private boolean isLoadData = false;//是否已加载数据
    private boolean isLoadView = false;//是否已加载控件
    private boolean isVisible = false;//是否对用户可见


    /*在onCreateView方法中执行*/
    @Override
    protected void initView(Bundle savedInstanceState) {
        isInit = true;
        LogUtil.d("func", "initView()");
    }

    /*判断是否可以加载数据, 如果可以便进行数据的加载*/
    private void tryLoadData() {
        if (!isLoadData && isInit && isVisible) {
            isLoadData = true;
            LogUtil.d("func","load data");
            loadData();
        }
    }

    /*判断是否可以加载视图中的控件, 如果可以便加载视图控件*/
    private void tryLoadView() {
        if (!isLoadView && isInit && isVisible) {
            isLoadView = true;
            LogUtil.d("func","load view");
            loadView();
        }
    }

    /**
     * 当Fragment进入View Pager的缓存区时该方法被调用
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogUtil.d("func", "onActivityCreated()");
        /*加载ViewPager中第一个Fragment的数据和控件，
        因为无法在setUserVisibleHint方法中进行它的加载工作*/
        tryLoadData();
        tryLoadView();
        //new TryLoadDataAndViewTask(this).execute();
    }

    /*加载数据*/
    public abstract void loadData();

    /*加载视图中的控件*/
    public abstract void loadView();

    /*
     * 以下两种情况该方法会被自动调用：
     * 1.Fragment首次被ViewPager加载时，该方法会在onCreateView之前调用
     * 2.Fragment可见性发生变化时
     *
     * @param isVisibleToUser 当前Fragment是否可见
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        LogUtil.d("func", isVisibleToUser + " setUserVisibleHint");
        if (isVisibleToUser) {
            isVisible = true;
            tryLoadData();
            tryLoadView();
            //new TryLoadDataAndViewTask(this).execute();
        } else {
            isVisible = false;
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    /*当ViewPager缓存区的Fragment离开缓存区后，
    Fragment的视图被销毁，但并不会走到onDestroy这一步*/
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtil.d("func", "onDestroyView()");
        isLoadView = false;
    }

    //异步任务
    static class TryLoadDataAndViewTask extends AsyncTask<Void, Integer, Boolean> {

        WeakReference<LazyLoadFragment> fragmentReference;

        TryLoadDataAndViewTask(LazyLoadFragment fragment) {
            fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            LogUtil.d("func","task try load data");
            fragmentReference.get().tryLoadData();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                LogUtil.d("func","task try load view");
                fragmentReference.get().tryLoadView();
            }
        }
    }

}
