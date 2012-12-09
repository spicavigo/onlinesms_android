package com.fauzism.onlinesms;

import org.json.JSONArray;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
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
    	public void syncContact() {
    		Intent intent = new Intent(_current, ContactFetcher.class);
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
    	 }
	}

}
