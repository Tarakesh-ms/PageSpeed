/**
 * 
 */
package com.tms.pagespeed.ui;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;

import com.google.analytics.tracking.android.EasyTracker;

/**
 * @author Tarak
 *
 */
public class BaseActivity extends Activity {
	
	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

}
