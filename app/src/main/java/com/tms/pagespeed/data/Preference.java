/**
 * 
 */
package com.tms.pagespeed.data;

import java.util.Locale;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tms.pagespeed.R;
import com.tms.pagespeed.analytics.AnalyticsHelper;

/**
 * @author Tarak
 *
 */
public class Preference {
	
	private static Preference mPref;
	private final Context mContext;
	public static String PREF_STRATEGY = "pagespeed_pref";
	private static String KEY_STRATEGY = "strategy";
	private static String KEY_UUID = "uuid";
	
	private String mStrategyVal ;
	private String mIdentityPref ;
	
	/**
	 * @param context
	 * @return
	 */
	public static Preference getInstance(Context context) {
		if (mPref == null) {
			mPref = new Preference(context);
		}
		return mPref;
	}
	
	Preference(Context context) {
		mContext = context;
	}
	
	public void initialize() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		String def = mContext.getResources().getString(R.string.mobile).toLowerCase(Locale.ENGLISH);
		mStrategyVal = sp.getString(KEY_STRATEGY, def);
		mIdentityPref = sp.getString(KEY_UUID, null);
		if(mIdentityPref == null){
			mIdentityPref = getUuid();
		}
	}
	
	/**
	 * @return strategy
	 */
	public String getStrategy(){
		return mStrategyVal;
	}
	
	
   private String getUuid() {
	   
		if (mIdentityPref == null) {
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
			SharedPreferences.Editor ed = sp.edit();
			String uuid = UUID.randomUUID().toString();
			ed.putString(KEY_UUID, UUID.randomUUID().toString());
			ed.commit();
			mIdentityPref = uuid;
			AnalyticsHelper.getInstance().trackUuid(mIdentityPref);
		}
		return mIdentityPref;
	}
	
	public void setStrategy(String strategy){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor ed = sp.edit();
		mStrategyVal = strategy.toLowerCase(Locale.ENGLISH);
		ed.putString(KEY_STRATEGY, mStrategyVal);
		ed.commit();
	}
}
