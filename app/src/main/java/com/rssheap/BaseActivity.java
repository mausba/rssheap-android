package com.rssheap;

import java.lang.reflect.Field;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.rssheap.controls.MyProgressDialog;
import com.rssheap.utilities.TypefaceUtil;
import com.rssheap.utilities.TypefaceUtil.Fonts;
import com.rssheap.utilities.Utilities;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ViewConfiguration;

public class BaseActivity extends FragmentActivity {
	
	public MyProgressDialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
						
	        ViewConfiguration config = ViewConfiguration.get(this);
	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
	        if(menuKeyField != null) {
	            menuKeyField.setAccessible(true);
	            menuKeyField.setBoolean(config, false);
	        }
	        TypefaceUtil.overrideFont(getApplicationContext(), Fonts.getRobotoRegular(getApplicationContext())); // font from assets: "assets/fonts/Roboto-Regular.ttf
	    } catch (Exception ex) {
	        // Ignore
	    }
	}
		
	@Override
	protected void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}
	
	@Override
	protected void onDestroy() {
		if(dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}
		dialog = null;
		super.onDestroy();
	}

	public void showLoadingDialog() {
		dialog = MyProgressDialog.show(BaseActivity.this);
	}

	public void hideLoadingDialog() {
		if(dialog != null)
			dialog.dismiss();
	}

    public boolean isDeviceOnline() {
        return Utilities.isDeviceOnline(BaseActivity.this);
    }
}
