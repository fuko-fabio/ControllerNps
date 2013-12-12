package com.nps.micro.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nps.micro.R;

public class HomeSectionFragment extends BaseSectionFragment {

    public HomeSectionFragment() {
        this.layout = R.layout.home;
    }

    @Override
    protected View buildRootView(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(layout, container, false);
        return rootView;
    }
}
