package com.nps.micro.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nps.micro.R;

public class GraphSectionFragment extends BaseSectionFragment {

    public GraphSectionFragment() {
        this.layout = R.layout.graph;
    }

    @Override
    protected View buildRootView(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(layout, container, false);
        return rootView;
    }
}
