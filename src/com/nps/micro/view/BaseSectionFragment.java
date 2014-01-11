package com.nps.micro.view;

import com.nps.micro.R;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public abstract class BaseSectionFragment extends Fragment {

    protected int layout;
    private String status;
    private boolean busy;

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

    public void updateStatus(String status, boolean busy) {
        this.status = status;
        this.busy = busy;
        updateStatus(getView());
    }

    protected void updateStatus(View rootView) {
        if(rootView == null) {
            return;
        }
        TextView statusTextView = (TextView) rootView.findViewById(R.id.statusText);
        if (statusTextView != null) {
            statusTextView.setText(status);
        }
        if (busy) {
            rootView.findViewById(R.id.statusProgress).setVisibility(View.VISIBLE);
        } else {
            rootView.findViewById(R.id.statusProgress).setVisibility(View.GONE);
        }
    }
}
