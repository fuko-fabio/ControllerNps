/*******************************************************************************
 * Copyright 2014 Norbert Pabian.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * imitations under the License.
 ******************************************************************************/
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

/**
 * @author Norbert Pabian
 * www.npsoft.clanteam.com
 */
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
