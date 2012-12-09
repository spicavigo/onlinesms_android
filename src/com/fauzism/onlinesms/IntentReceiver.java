package com.fauzism.onlinesms;
 
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
 
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
 
import com.urbanairship.push.PushManager;
 
public class IntentReceiver extends BroadcastReceiver {
 
	public class MyThread implements Runnable {
			Intent intent;
			Context context;
			public MyThread(Context context1, Intent intent1) {
		       // store parameter for later user
				this.intent = intent1;
				this.context = context1;
			}

		    public void run() {
		    	try {
    	        	SharedPreferences prefs = PreferenceManager
            				.getDefaultSharedPreferences(context);
            		String email = prefs.getString("email", "");
            		while(email.length()==0){
            			Thread.sleep(1000);
            			email = prefs.getString("email", "");
            		}
    				//MainActivity.engine.loadUrl("javascript: register_device_token('"+ intent.getStringExtra(PushManager.EXTRA_APID) +"')");
    				FetchURL task = new FetchURL();
                    task.execute(new String[] { "http://fzsmsonline.appspot.com/push_register/?email="+email+"&apid="+intent.getStringExtra(PushManager.EXTRA_APID) });
                    Log.d(logTag, "http://fzsmsonline.appspot.com/push_register/?email="+email+"&apid="+intent.getStringExtra(PushManager.EXTRA_APID));
    	        } catch (InterruptedException e) {
    	            e.printStackTrace();
    	        }
		    }
	}
		
    private static final String logTag = "OnlineSMS";
 
        @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(logTag, "Received intent: " + intent.toString());
    	String action = intent.getAction();

        if (action.equals(PushManager.ACTION_PUSH_RECEIVED)) {
            int id = intent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID, 0);
 
            Log.i(logTag, "Received push notification. Alert: "
                    + intent.getStringExtra(PushManager.EXTRA_ALERT)
                    + " [NotificationID="+id+"]");
 
            send_sms(intent);
 
        } else if (action.equals(PushManager.ACTION_REGISTRATION_FINISHED)) {
            Log.i(logTag, "Registration complete. APID:" + intent.getStringExtra(PushManager.EXTRA_APID)
            + ". Valid: " + intent.getBooleanExtra(PushManager.EXTRA_REGISTRATION_VALID, false));
            Runnable r = new MyThread(context, intent);
            new Thread(r).start();
        }
 
    }
 
        /**
     * Log the values sent in the payload's "extra" dictionary.
     *
     * @param intent A PushManager.ACTION_NOTIFICATION_OPENED or ACTION_PUSH_RECEIVED intent.
     */
    private void send_sms(Intent intent) {
    		String phone = intent.getStringExtra("phone");
    		String msg = intent.getStringExtra("msg");
        		SmsManager sms = SmsManager.getDefault();
        	    sms.sendTextMessage(phone, null, msg, null, null);
        	    
                Set<String> keys = intent.getExtras().keySet();
                for (String key : keys) {
 
                        //ignore standard extra keys (GCM + UA)
                    List<String> ignoredKeys = (List<String>)Arrays.asList(
                                    "collapse_key",//GCM collapse key
                                    "from",//GCM sender
                                    PushManager.EXTRA_NOTIFICATION_ID,//int id of generated notification (ACTION_PUSH_RECEIVED only)
                                    PushManager.EXTRA_PUSH_ID,//internal UA push id
                                    PushManager.EXTRA_ALERT);//ignore alert
                    if (ignoredKeys.contains(key)) {
                            continue;
                    }
                    Log.i(logTag, "Push Notification Extra: ["+key+" : " + intent.getStringExtra(key) + "]");
            }
    }
    
    private class FetchURL extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
          String response = "";
          for (String url : urls) {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            try {
              HttpResponse execute = client.execute(httpGet);
              InputStream content = execute.getEntity().getContent();

              BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
              String s = "";
              while ((s = buffer.readLine()) != null) {
                response += s;
              }

            } catch (Exception e) {
              e.printStackTrace();
            }
          }
          return response;
        }
      }
}