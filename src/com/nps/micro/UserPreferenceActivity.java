package com.nps.micro;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.nps.micro.view.UserPreferenceFragment;

public class UserPreferenceActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);

     getFragmentManager().beginTransaction().replace(android.R.id.content,
                   new UserPreferenceFragment()).commit();
    }

    @Override
    public void onBackPressed() {
        this.setResult(RESULT_OK, getIntent());
        this.finish();
    }
}
