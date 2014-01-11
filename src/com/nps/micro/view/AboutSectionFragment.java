package com.nps.micro.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nps.micro.R;

public class AboutSectionFragment extends BaseSectionFragment {

    public AboutSectionFragment() {
        this.layout = R.layout.about;
    }

    @Override
    protected View buildRootView(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(layout, container, false);
        updateStatus(rootView);
        return rootView;
    }
}
