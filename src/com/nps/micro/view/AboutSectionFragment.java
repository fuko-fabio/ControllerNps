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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nps.micro.R;

/**
 * @author Norbert Pabian
 * www.npsoft.clanteam.com
 */
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
