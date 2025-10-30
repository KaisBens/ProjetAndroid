package com.example.sae41;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ConfigActivity extends Activity {
    private CheckBox monochromeCheckBox;
    private SharedPreferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        
        monochromeCheckBox = (CheckBox) findViewById(R.id.checkboxMonochrome);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isMonochrome = prefs.getBoolean("monochrome", false);
        monochromeCheckBox.setChecked(isMonochrome);
        
        monochromeCheckBox.setOnCheckedChangeListener(new MonochromeChangeListener());
    }
    
    private class MonochromeChangeListener implements CompoundButton.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("monochrome", isChecked);
            editor.commit();
        }
    }
    
    @Override
    public void onBackPressed() {
        finish();
    }
}
