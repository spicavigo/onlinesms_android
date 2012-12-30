package com.fauzism.onlinesms;

import org.json.JSONArray;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class MainActivity extends Activity {

	private boolean haveNetworkConnection() {
	    boolean haveConnectedWifi = false;
	    boolean haveConnectedMobile = false;

	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
	            if (ni.isConnected())
	                haveConnectedWifi = true;
	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
	            if (ni.isConnected())
	                haveConnectedMobile = true;
	    }
	    return haveConnectedWifi || haveConnectedMobile;
	}
	public final BroadcastReceiver network_state = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(haveNetworkConnection()){
				
			}
			//network state changes, you can process it, inforamtion in intent
		}
	};
	static WebView engine;
	TextView tview;
	Activity _current = null;
	static JSONArray googleAccounts = new JSONArray();
	Handler mHandler;
	ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		_current = this;

		Account[] accounts = AccountManager.get(this).getAccounts();
		for (Account account : accounts) {
			if (account.type.equals("com.google")) {
				googleAccounts.put(account.name);
			}
		}

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				try{
					if ((String)msg.obj == "show") pd = ProgressDialog.show(_current,"Registering...","",true,false,null);
					else if ((String)msg.obj == "dismiss") pd.dismiss();
					else if ((String)msg.obj == "setaccount")engine.loadUrl("javascript: setAccounts("+ googleAccounts.join(",")+")");
				} catch (Exception e){

				}
			}
		};

		engine = (WebView) findViewById(R.id.web_engine);

		engine.setWebViewClient(new WebViewClient());

		WebSettings webSettings = engine.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setBuiltInZoomControls(false);
		webSettings.setSupportZoom(false);
		webSettings.setDomStorageEnabled(true);
		webSettings.setSavePassword(false);
		webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
		webSettings.setDatabaseEnabled(true);
		webSettings.setDatabasePath(Environment.getDataDirectory() + "/data/com.fauzism.onlinesms/databases/");

		engine.addJavascriptInterface(new JSInterface(), "Device");
		engine.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		//engine.setHorizontalScrollBarEnabled(false);
		engine.setSoundEffectsEnabled(false);

		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		engine.loadUrl("file:///android_asset/index.html");

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(_current.getBaseContext());
		Log.d("OnlineSMS", prefs.getString("email", ""));

		//Intent intent = new Intent(_current, ContactFetcher.class);
		//startService(intent);

		IntentFilter network_filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(network_state , network_filter);
	}
	@Override
    protected void onStop() {
		unregisterReceiver(network_state);
        super.onStop();
    }
	

	final class JSInterface {

		long Last_Track = System.currentTimeMillis();
		long MAXSLEEP = 30 * 60 * 1000;
		JSInterface() {
		}

		public void log(final String str){
			Log.d("OnlineSMSDebugLog", str);
		}

		/**
		 * This is not called on the UI thread. Post a runnable to invoke
		 * loadUrl on the UI thread.
		 */
		public void syncContact(final String email) {
			Intent intent = new Intent(_current, ContactFetcher.class);
			intent.putExtra("email", email);
			startService(intent);
		}

		public void getAccounts(){
			Message msg = new Message();
			String message = "setaccount";
			msg.obj = message;
			mHandler.sendMessage(msg);

		}
		public void showSpinner(){
			Message msg = new Message();
			String message = "show";
			msg.obj = message;
			mHandler.sendMessage(msg);
			//pd = ProgressDialog.show(_current,"Registering...","",true,false,null);

		}
		public void removeSpinner(){
			Message msg = new Message();
			String message = "dismiss";
			msg.obj = message;
			mHandler.sendMessage(msg);
			//pd.dismiss();
		}
		public void saveEmail(final String email){
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(_current.getBaseContext());
			Editor edit = prefs.edit();
			edit.putString("email", email);
			edit.commit();
			syncContact(email);
		}
	}

}
