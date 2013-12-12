package com.nps.micro.view;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseSectionFragment extends Fragment {

    protected int layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return buildRootView(inflater, container);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        ViewGroup rootView = (ViewGroup) getView();
        rootView.removeAllViews();
        rootView.addView(buildRootView(inflater, rootView));
    }

    protected abstract View buildRootView(LayoutInflater inflater, ViewGroup container);
}
