package com.my.smartplanner.fragment;

import android.os.Bundle;

import com.my.smartplanner.R;

public class BlankFragment extends BaseFragment {

    public static BlankFragment newInstance() {
        Bundle args = new Bundle();
        BlankFragment fragment = new BlankFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_blank;
    }

    @Override
    protected void createViewComplete() {

    }
}
