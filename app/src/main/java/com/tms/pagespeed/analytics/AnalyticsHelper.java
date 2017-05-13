/**
 * 
 */
package com.tms.pagespeed.analytics;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.tms.pagespeed.app.PageSpeedApp;

/**
 * @author Tarak
 *
 */
public class AnalyticsHelper {
	
	private static AnalyticsHelper mAnalyticsHelper;
	private static final String CATEGORY_REQUEST = "user_request";
	
	private static final String ACTION_ANALYZE = "page_analyze";
	
	public static final String SCREEN_PAGESPEED  = "/pagespeed_screen/";
	public static final String SCREEN_STATISTICS = "/pagespeed_stats/";
	
	
	/**
	 * @return singleton instance of  {@linkplain AnalyticsHelper}
	 */
	public static AnalyticsHelper getInstance(){
		if(mAnalyticsHelper == null){
			mAnalyticsHelper = new AnalyticsHelper();
		}
		return mAnalyticsHelper;
	}
	
	
	/**
	 * tracks unique identity
	 * @param uuid
	 */
	public void trackUuid(String uuid) {
		Tracker tracker = PageSpeedApp.getGaTracker();
		tracker.set(Fields.customDimension(1), uuid);
		tracker.send(MapBuilder.createAppView()
			   .set(Fields.CLIENT_ID, uuid)
			   .set(Fields.SCREEN_NAME, SCREEN_PAGESPEED)
			   .build()
			   );
	}
	
	
	/**
	 * tracks if a success page was being displayed
	 * @param score
	 * @param name
	 */
	public void trackSuccess(String url, String score) {
		Tracker tracker = PageSpeedApp.getGaTracker();
		//create label
		StringBuilder urlwithScore = new StringBuilder(url);
		urlwithScore.append(" (").append(score).append(")");
		
		tracker.send(MapBuilder.createEvent(CATEGORY_REQUEST, ACTION_ANALYZE,
				urlwithScore.toString(), (long) 1).build());
		tracker.send(MapBuilder.createAppView().set(Fields.SCREEN_NAME, SCREEN_STATISTICS).build());
	}
	
	/**
	 * tracks if an error page is displayed
	 * @param error
	 * @param name
	 */
	public  void trackError(String url, int errorCode) {
		Tracker tracker = PageSpeedApp.getGaTracker();
		
		//create label
		StringBuilder urlwithErr = new StringBuilder(url);
		urlwithErr.append(" (err:").append(errorCode).append(")");
		
		tracker.send(MapBuilder.createEvent(CATEGORY_REQUEST, ACTION_ANALYZE, urlwithErr.toString(), (long) 1)
				.build());
		tracker.send(MapBuilder.createAppView().set(Fields.SCREEN_NAME, SCREEN_STATISTICS).build());
	}
	
	/**
	 * tracks if an error page is displayed
	 * @param error
	 * @param name
	 */
	public  void trackIOError() {
		Tracker tracker = PageSpeedApp.getGaTracker();
		
		tracker.send(MapBuilder.createEvent(CATEGORY_REQUEST, ACTION_ANALYZE, "IOException", (long) 1)
				.build());
		tracker.send(MapBuilder.createAppView().set(Fields.SCREEN_NAME, SCREEN_STATISTICS).build());
	}
}
