package com.my.smartplanner.fragment;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.my.smartplanner.util.LogUtil;

import java.lang.ref.WeakReference;

public abstract class LazyLoadFragment extends BaseFragment {
    /**
     * 是否已初始化
     */
    private boolean isCreateViewComplete = false;
    /**
     * 是否已加载数据
     */
    private boolean isLoadData = false;
    /**
     * 是否已加载控件
     */
    private boolean isLoadView = false;
    /**
     * 是否对用户可见
     */
    private boolean isVisible = false;


    /**
     * 在onCreateView方法中执行
     */
    @Override
    protected void createViewComplete() {
        isCreateViewComplete = true;
        LogUtil.d("LazyLoadFragment", "initView()");
    }

    /**
     * 如果可以便进行数据的加载
     */
    private void tryLoadData() {
        if (!isLoadData && isCreateViewComplete && isVisible) {
            isLoadData = true;
            loadData();
            LogUtil.d("LazyLoadFragment", "tryLoadData() success");
            LogUtil.d("LazyLoadFragment", "loadData()");
        } else {
            LogUtil.d("LazyLoadFragment", "tryLoadData() fail");
        }
    }

    /**
     * 如果可以便加载视图控件
     */
    private void tryLoadView() {
        if (!isLoadView && isCreateViewComplete && isVisible) {
            isLoadView = true;
            loadView();
            LogUtil.d("LazyLoadFragment", "tryLoadView() success");
            LogUtil.d("LazyLoadFragment", "loadView()");
        } else {
            LogUtil.d("LazyLoadFragment", "tryLoadView() success");
        }
    }

    /**
     * 当Fragment进入ViewPager的缓存区时该方法被调用
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LogUtil.d("LazyLoadFragment", "onActivityCreated()");
        //加载ViewPager中第一个Fragment的数据和控件
        //因为无法在setUserVisibleHint方法中进行它的加载工作
        //tryLoadData();
        //tryLoadView();
        new TryLoadDataAndViewTask(this).execute();
    }

    /**
     * 加载数据
     */
    public abstract void loadData();

    /**
     * 加载视图中的控件
     */
    public abstract void loadView();

    /**
     * 以下两种情况该方法会被自动调用：
     * <ul>
     *     <li>Fragment首次被ViewPager加载时，该方法会在onCreateView之前调用</li>
     *     <li>Fragment可见性发生变化时</li>
     * </ul>
     *
     * @param isVisibleToUser 当前Fragment是否可见
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        LogUtil.d("LazyLoadFragment", "setUserVisibleHint(" + isVisibleToUser + ")");
        if (isVisibleToUser) {
            isVisible = true;
            //tryLoadData();
            //tryLoadView();
            new TryLoadDataAndViewTask(this).execute();
        } else {
            isVisible = false;
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    /**
     * 当ViewPager缓存区的Fragment离开缓存区后，Fragment的视图被销毁，但并不会走到onDestroy这一步
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtil.d("LazyLoadFragment", "onDestroyView()");
        isLoadView = false;
    }

    /**
     * 尝试加载数据和视图
     */
    static class TryLoadDataAndViewTask extends AsyncTask<Void, Integer, Boolean> {

        WeakReference<LazyLoadFragment> weakFragment;

        TryLoadDataAndViewTask(LazyLoadFragment fragment) {
            weakFragment = new WeakReference<>(fragment);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            weakFragment.get().tryLoadData();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                weakFragment.get().tryLoadView();
            }
        }
    }

}
