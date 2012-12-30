package com.fauzism.onlinesms;

import java.net.URLEncoder;
import java.util.ArrayList;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.urbanairship.push.PushManager;

public class IntentReceiver extends BroadcastReceiver {
	
	private boolean isConnected(Context context) {
	    boolean haveConnectedWifi = false;
	    boolean haveConnectedMobile = false;

	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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
	
	public static String URL = "http://fzsmsonline.appspot.com";
	public static ArrayList<String> UnsendMsgs = new ArrayList<String>();
	
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
				FetchURL task = new FetchURL();
				task.execute(new String[] { URL + "/push_register/?version=2&email="+email+"&apid="+intent.getStringExtra(PushManager.EXTRA_APID) });
				Log.d(logTag, URL + "/push_register/?email="+email+"&apid="+intent.getStringExtra(PushManager.EXTRA_APID));
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
			on_push(context, intent);
		} else if (action.equals(PushManager.ACTION_REGISTRATION_FINISHED)) {
			on_registration(context, intent);
		} else if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
			on_sms(context, intent);
		} else if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
			on_boot(context, intent);
		}
	}

	private void on_push(Context context, Intent intent){
		send_sms(context, intent);
	}
	private void on_registration(Context context, Intent intent){
		Runnable r = new MyThread(context, intent);
		new Thread(r).start();	
	}
	private void on_sms(Context context, Intent intent){
		Bundle bundle = intent.getExtras(); 
		SmsMessage[] msgs = null;
		String msg_from;
		if (bundle != null){
			try{
				Object[] pdus = (Object[]) bundle.get("pdus");
				msgs = new SmsMessage[pdus.length];
				for(int i=0; i<msgs.length; i++){
					msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
					msg_from = msgs[i].getOriginatingAddress();
					String msgBody = msgs[i].getMessageBody();
					String contact_name = get_contact_name(context, msg_from);
					SharedPreferences prefs = PreferenceManager
							.getDefaultSharedPreferences(context);
					String email = prefs.getString("email", "");
					if(prefs.contains(contact_name) && email.length()>0){
						if(isConnected(context)){
							FetchURL task = new FetchURL();
							task.execute(new String[] { 
									URL + "/sms_received/?contact_name="+URLEncoder.encode(contact_name)+
									"&phone=" + URLEncoder.encode(msg_from) + "&msg="+URLEncoder.encode(msgBody) + 
									"&email=" + URLEncoder.encode(email)});
							String[] temp = new String[UnsendMsgs.size()];
							task.execute(UnsendMsgs.toArray(temp));
							UnsendMsgs.clear();
						} else {
							UnsendMsgs.add(URL + "/sms_received/?contact_name="+URLEncoder.encode(contact_name)+
									"&phone=" + URLEncoder.encode(msg_from) + "&msg="+URLEncoder.encode(msgBody) + 
									"&email=" + URLEncoder.encode(email));
						}
						
					}
				}
			}catch(Exception e){
				//Log.d("Exception caught",e.getMessage());
			}
		}
	}
	private void on_boot(Context context, Intent intent){
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String email = prefs.getString("email", "");
		Intent new_intent = new Intent(context, ContactFetcher.class);
		new_intent.putExtra("email", email);
		context.startService(new_intent);
	}
	/**
	 * Log the values sent in the payload's "extra" dictionary.
	 *
	 * @param intent A PushManager.ACTION_NOTIFICATION_OPENED or ACTION_PUSH_RECEIVED intent.
	 */
	private void send_sms(Context context, Intent intent) {
		try{
			String phone = intent.getStringExtra("phone");
			String msg = intent.getStringExtra("msg");
			SmsManager sms = SmsManager.getDefault();
			if(msg.length()>160){
				sms.sendMultipartTextMessage(phone, null, sms.divideMessage(msg), null, null);
			} else {
				sms.sendTextMessage(phone, null, msg, null, null);
			}

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(context);
			Editor edit = prefs.edit();
			String contact_name = get_contact_name(context, phone);
			edit.putInt(contact_name, 1);
			edit.commit();

			String msgid = intent.getStringExtra("msgid");
			if(isConnected(context)){
				FetchURL task = new FetchURL();
				task.execute(new String[] { URL + "/delivery/?msgid="+msgid+"&sent=true" });
				String[] temp = new String[UnsendMsgs.size()];
				task.execute(UnsendMsgs.toArray(temp));
				UnsendMsgs.clear();
			} else {
				UnsendMsgs.add(URL+"/delivery/?msgid="+msgid+"&sent=true");
			}
			

			ContentValues sentSms = new ContentValues();
			sentSms.put("address", phone);
			sentSms.put("body", msg);

			ContentResolver contentResolver = context.getContentResolver();
			contentResolver.insert(Uri.parse("content://sms/sent"), sentSms);


		} catch (Exception e) {
			e.printStackTrace();
		}


	}
	private String get_contact_name(Context context, String number) {

		String name = null;

		// define the columns I want the query to return
		String[] projection = new String[] {
				ContactsContract.PhoneLookup.DISPLAY_NAME,
				ContactsContract.PhoneLookup._ID};

		// encode the phone number and build the filter URI
		Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

		// query time
		Cursor cursor = context.getContentResolver().query(contactUri, projection, null, null, null);

		if (cursor.moveToFirst()) {
			name =      cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
			return name;

		} else {
			return number; // contact not found

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
					client.execute(httpGet);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return response;
		}
	}
}