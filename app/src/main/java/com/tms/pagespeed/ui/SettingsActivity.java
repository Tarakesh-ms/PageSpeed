/**
 * 
 */
package com.tms.pagespeed.ui;

import android.os.Bundle;

import com.google.analytics.tracking.android.Fields;
import com.tms.pagespeed.app.PageSpeedApp;

/**
 * @author Tarak
 *
 */
public class SettingsActivity extends BaseActivity {
	
	private static final String SCREEN_LABEL = "/settings_screen/";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		 // Fields set on a tracker persist for all hits, until they are
	    // overridden or cleared by assignment to null.
	    PageSpeedApp.getGaTracker().set(Fields.SCREEN_NAME, SCREEN_LABEL);
		
		getFragmentManager().beginTransaction()
							.replace(android.R.id.content, new StrategyDialogFragment())
							.commit();
	}

}
