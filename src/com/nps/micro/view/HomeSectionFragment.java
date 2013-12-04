package com.nps.micro.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nps.micro.R;

public class HomeSectionFragment extends BaseSectionFragment {

    public HomeSectionFragment() {
        this.layout = R.layout.home;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(layout, container, false);
        return rootView;
    }
}
