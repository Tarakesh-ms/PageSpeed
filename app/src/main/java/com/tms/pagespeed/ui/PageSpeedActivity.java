/*
 * 
 * Copyright (C) 2014 Tarakeshwar Sriram <tarakesh.sriram@gmail.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tms.pagespeed.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpIOExceptionHandler;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.services.pagespeedonline.Pagespeedonline;
import com.google.api.services.pagespeedonline.Pagespeedonline.Pagespeedapi.Runpagespeed;
import com.google.api.services.pagespeedonline.PagespeedonlineRequestInitializer;
import com.google.api.services.pagespeedonline.model.Result;
import com.google.api.services.pagespeedonline.model.Result.FormattedResults;
import com.google.api.services.pagespeedonline.model.Result.FormattedResults.RuleResultsElement;
import com.google.api.services.pagespeedonline.model.Result.PageStats;
import com.tms.pagespeed.R;
import com.tms.pagespeed.analytics.AnalyticsHelper;
import com.tms.pagespeed.data.Preference;
import com.tms.pagespeed.data.ResourceStats;
import com.tms.pagespeed.util.HelpUtils;

/**
 * The main activity for the dictionary. Displays search results triggered by
 * the search dialog and handles actions from search suggestions.
 */
public class PageSpeedActivity extends Activity implements IResponse, OnClickListener {

	private final java.util.regex.Pattern URL_PATTERN = java.util.regex.Pattern
			.compile("http(s)?://.*");
	

	private static String TAG = "pagespeedactivity";
	private WebView mWebView;
	private TextView mTextView;
	String mUrlToAnalyze = null;
	// The SearchView for doing filtering.
	SearchView mSearchView;
	private ProgressBar mProgressBar;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		mTextView = (TextView)findViewById(R.id.text);
		mWebView = (WebView) findViewById(R.id.results_webview);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.setWebViewClient(new PageWebViewClient());

		mProgressBar = (ProgressBar) findViewById(R.id.progress);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.options_menu, menu);
		MenuItem searchMenuItem = menu.findItem(R.id.search);

		if (searchMenuItem != null) {
			mSearchView = (SearchView) searchMenuItem.getActionView();
			if (mSearchView != null) {
				SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
				mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
				mSearchView.setIconifiedByDefault(false);
				 // You can use Display.getSize(Point) from API 13 onwards.
				 // For API 11 and 12, use Display.getWidth()
				
				 final Point p = new Point();
				
				 Display dp = getWindowManager().getDefaultDisplay();
				 int width = 0;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
					dp.getSize(p);
					width = p.x;
				} else {
					width = dp.getWidth();
				}
				
				 width = (width/3) + (width/3);
				 // Create LayoutParams with width set to screen's width
				 LayoutParams params = new LayoutParams(width,
				 LayoutParams.MATCH_PARENT);
				 mSearchView.setLayoutParams(params);
				setSearchViewOnClickListener(mSearchView, this);
			}
		}
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		
		super.onNewIntent(intent);
		// Because this activity has set launchMode="singleTop", the system
		// calls this method
		// to deliver the intent if this activity is currently the foreground
		// activity when
		// invoked again (when the user executes a search from this activity, we
		// don't create
		// a new instance of this activity, so the system delivers the search
		// intent here)
		handleIntent(intent);
	}
	

	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		mUrlToAnalyze = null;

		if (action != null) {
			if (Intent.ACTION_VIEW.equals(action)) {
				Uri uri = intent.getData();
				mUrlToAnalyze = uri.toString();
			} 
			else if (Intent.ACTION_SEARCH.equals(action)) {
				mUrlToAnalyze = intent.getStringExtra(SearchManager.QUERY);
			}
			
			setIntent(new Intent());

			if (mUrlToAnalyze != null) {

				try {
					Preconditions.checkArgument(URL_PATTERN.matcher(mUrlToAnalyze).matches());
				} catch (IllegalArgumentException ex) {
					mUrlToAnalyze = new StringBuilder("http://").append(mUrlToAnalyze).toString();
				}

				PageSpeedInitializer initializer = new PageSpeedInitializer();
				initializer.setErrorHandler(this);
				new PageSpeedInsightTask(initializer).execute(mUrlToAnalyze);
				updateUI(true);
			}
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings: {
			StrategyDialogFragment dialog = new StrategyDialogFragment();
			dialog.show(getFragmentManager(), "StrategyDialogFragment");
		}
			break;
		case R.id.menu_about:
			HelpUtils.showAbout(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public static void setSearchViewOnClickListener(View v, OnClickListener listener) {
		if (v instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) v;
			int count = group.getChildCount();
			for (int i = 0; i < count; i++) {
				View child = group.getChildAt(i);
				if (child instanceof LinearLayout || child instanceof RelativeLayout) {
					setSearchViewOnClickListener(child, listener);
				}

				if (child instanceof TextView) {
					TextView text = (TextView) child;
					text.setFocusable(false);
				}
				child.setOnClickListener(listener);
			}
		}
	}

	/**
	 * {@linkplain PageSpeedInitializer} is used to handle http error other than
	 * 2XX
	 */
	 class PageSpeedInitializer implements HttpRequestInitializer,
			HttpUnsuccessfulResponseHandler, HttpIOExceptionHandler {

		private IResponse mResponse;

		void setErrorHandler(IResponse response) {
			if (response != null) {
				mResponse = response;
			}
		}

		@Override
		public boolean handleResponse(HttpRequest request, HttpResponse response,
				boolean retrySupported) throws IOException {
			System.out.println(response.getStatusCode() + " " + response.getStatusMessage());
			GoogleJsonError errorResponse = GoogleJsonError.parse(new JacksonFactory(), response);
			String message = "Error " + errorResponse.getCode() + ": "+ errorResponse.getMessage();
			mResponse.onErrorResponse(message);
			
			AnalyticsHelper.getInstance().trackError(mUrlToAnalyze, errorResponse.getCode());
			return false;
		}

		@Override
		public void initialize(HttpRequest request) throws IOException {
			request.setIOExceptionHandler(this);
			request.setUnsuccessfulResponseHandler(this);
		}

		@Override
		public boolean handleIOException(HttpRequest request, boolean supportsRetry)
				throws IOException {
			
			mResponse.onErrorResponse(getString(R.string.err_default));
			AnalyticsHelper.getInstance().trackIOError();
			return false;
		}
	}

	/**
	 * {@linkplain PageSpeedInsightTask}
	 */
	private class PageSpeedInsightTask extends AsyncTask<String, Void, Result> {

		private final PageSpeedInitializer mInitializer;

		public PageSpeedInsightTask(PageSpeedInitializer intializer) {
			mInitializer = intializer;
		}

		/**
		 * @param url
		 * @return {@linkplain Result}
		 * @throws IOException
		 */
		private Result analyzePageSpeed(String url) throws IOException {

			if (url != null) {
				NetHttpTransport.Builder builder = new NetHttpTransport.Builder();
				try {
					builder.doNotValidateCertificate();
				} catch (GeneralSecurityException e) {
					e.printStackTrace();
				}
				NetHttpTransport httpTransport = builder.build();
				String API_KEY = "AIzaSyDYzSsxdvXdfTiFUE3Q70bN-vCRyoR8A7I";
				// String API_KEY = "AIzaSyAMhaqHOIIMTVE635nR-Hmb6-0xapxrx7E";
				PagespeedonlineRequestInitializer requestInitializer = new PagespeedonlineRequestInitializer(
						API_KEY);
				Pagespeedonline.Builder pagespeedbuilder = new Pagespeedonline.Builder(
						httpTransport, new JacksonFactory(), mInitializer);
				pagespeedbuilder.setApplicationName(getString(R.string.app_name));
				pagespeedbuilder.setPagespeedonlineRequestInitializer(requestInitializer);
				Pagespeedonline pageSpeedOnline = pagespeedbuilder.build();

				Runpagespeed pageSpeed = pageSpeedOnline.pagespeedapi().runpagespeed(url);
				String strategy = Preference.getInstance(getApplicationContext()).getStrategy();
				pageSpeed.setStrategy(strategy);
				pageSpeed.setPrettyPrint(true);
				return pageSpeed.execute();
			}
			return null;
		}

		@Override
		protected Result doInBackground(String... params) {

			String urlToAnalyze = params[0];
			try {
				return analyzePageSpeed(urlToAnalyze);
			} catch (IOException ex) {
				if (ex != null) {
					Log.e(TAG, ex.getMessage());
				}
			}
			catch(Exception ex){
				Log.e(TAG, ex.getMessage());
				onErrorResponse(getString(R.string.err_default));
			}

			return null;
		}

		@Override
		protected void onPostExecute(Result result) {
			super.onPostExecute(result);
			showPageSpeedResult(result);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
	}

	private class PageWebViewClient extends WebViewClient {

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			updateUI(false);
		}
	}

	private String getHtml(String filename) {
		AssetManager assetMgr = getAssets();
		String html = null;
		InputStream input = null;

		try {
			input = assetMgr.open(filename);
			int size = input.available();
			byte[] buffer = new byte[size];
			input.read(buffer);
			input.close();
			html = new String(buffer);
		} catch (IOException e) {
			Log.e(TAG, "Exception: " + e.getMessage());
		}
		return html;
	}


	private void showPageSpeedResult(Result result) {

		if (result == null) {
			Log.e(TAG, "failed to get result");
		} 
		else {
			int responseCode = result.getResponseCode();

			if (responseCode == HttpURLConnection.HTTP_OK) {
				PageStats stats = result.getPageStats();
				if (stats != null) {
					Long flashBytes = stats.getFlashResponseBytes();
					Long jsBytes = stats.getJavascriptResponseBytes();
					Long imageBytes = stats.getImageResponseBytes();
					Long cssBytes = stats.getCssResponseBytes();
					Long htmlBytes = stats.getHtmlResponseBytes();
					Long otherBytes = stats.getOtherResponseBytes();
					Long textBytes = stats.getTextResponseBytes();

					StringBuilder suggestionsSection = null;
					StringBuilder suggestions = new StringBuilder();

					FormattedResults formattedResult = result.getFormattedResults();
					Map<String, RuleResultsElement> resultMap = formattedResult.getRuleResults();
					for (Map.Entry<String, RuleResultsElement> entry : resultMap.entrySet()) {
						RuleResultsElement ruleResult = entry.getValue();
						Double ruleimpact = ruleResult.getRuleImpact();
						if (ruleimpact > 3.0) {
							String ruleName = ruleResult.getLocalizedRuleName();
							suggestions.append("<li>").append(ruleName).append("</li>");
						}
					}
					
					if(suggestions.length() > 0){
						suggestionsSection = new StringBuilder("<h4>Top Page Speed Suggestions</h4>");
						suggestionsSection.append("<ul>");
						suggestionsSection.append(suggestions);
						suggestions.append("</ul>");
					}

					ResourceStats pageResource = new ResourceStats(flashBytes, jsBytes, imageBytes,
							cssBytes, htmlBytes, textBytes, otherBytes);

					String title = result.getTitle();
					if(title != null){
						mTextView.setText(title);
					}

					String strategy = Preference.getInstance(getApplicationContext()).getStrategy();
					String html = getHtml("score.html");
					String webScore = html.replace("$TITLE", "")
							.replace("$STRATEGY", strategy)
							.replace("$SCORE", result.getScore().toString())
							.replace("$RESOURCES", pageResource.toString())
							.replace("$SUGGESTIONS", (suggestionsSection==null)?"":suggestionsSection);
					mWebView.clearHistory();
					mWebView.loadDataWithBaseURL("file:///android_asset/", webScore, "text/html",
							"utf-8", null);
					
					AnalyticsHelper.getInstance().trackSuccess(mUrlToAnalyze, result.getScore().toString());
				}
			}
		}
	}

	/**
	 * @param start
	 */
	private void updateUI(boolean start) {
		// if (mSearchView != null) {
		// if (start) {
		// mSearchView.setVisibility(View.INVISIBLE);
		// } else {
		// mSearchView.setVisibility(View.VISIBLE);
		// mSearchView.clearFocus();
		// }
		// }
		if (mWebView != null) {
			if (start) {
				mWebView.setVisibility(View.INVISIBLE);
			} else {
				mWebView.setVisibility(View.VISIBLE);
			}
		}
		
		if(mTextView != null){
			if(start){
				mTextView.setText(R.string.analyzing);
			}
		}
		
		setRefreshProgressState(start);
	}

	void setRefreshProgressState(boolean visible) {
		if (mProgressBar == null) {
			return;
		}
		
		mSearchView.setIconifiedByDefault(visible);
		setProgressBarIndeterminateVisibility(visible);
	}

	@Override
	public void onErrorResponse(final String message) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (message != null) {
					
					mTextView.setText("");
					mWebView.loadData("<html><body><br><h1>" + message
							+ "</h1></br></body</html>", "text/html", "UTF-8");
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		onSearchRequested();
	}
}
