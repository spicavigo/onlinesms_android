package com.fauzism.onlinesms;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.format.DateUtils;
import android.util.Log;

public class ContactFetcher extends IntentService {

	public static JSONObject resjson = new JSONObject();
	public static String URL = "http://fzsmsonline.appspot.com";
	String tag="OnlineSMS";

	public ContactFetcher(){
		super("ContactFetcher");
	}

	@Override
	protected void onHandleIntent(Intent intent)  {
		Log.i(tag, "Service started...");
		String email = intent.getStringExtra("email");

		if(email==null)return;
		if(email.length()==0)return;
		
		try {
			JSONArray temp = new JSONArray();
			temp.put(email);
			resjson.put("__email__online__sms", temp);
			populateContact();
			scheduleNextUpdate(email);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}


	public void populateContact(){
		Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null); 

		while (cursor.moveToNext()) { 
			String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

			String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
			String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

			if ( hasPhone.equalsIgnoreCase("1"))
				hasPhone = "true";
			else
				hasPhone = "false" ;
			if (Boolean.parseBoolean(hasPhone)) {
				// You know it has a number so now query it like this
				Cursor phones = getContentResolver().query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId, null, null); 
				JSONArray temp = new JSONArray();
				while (phones.moveToNext()) { 
					String phoneNumber = phones.getString(phones.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));
					temp.put(phoneNumber);		         
				} 
				phones.close();
				try {
					resjson.put(name, temp);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		cursor.close();
		Log.d("OnlineSMS", resjson.toString());
		HttpClient httpClient = new DefaultHttpClient();

		try {
			HttpPost request = new HttpPost(URL+"/save_contact/");
			StringEntity params =new StringEntity("contacts="+resjson.toString(), "utf-8");
			request.addHeader("content-type", "application/x-www-form-urlencoded");
			request.setEntity(params);
			httpClient.execute(request);

			// handle response here...
		}catch (Exception ex) {
			Log.d("OnlineSMS", ex.toString());
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
	}
	
	private void scheduleNextUpdate(String email){
	    Intent intent = new Intent(this, this.getClass());
	    intent.putExtra("email", email);
	    PendingIntent pendingIntent =
	        PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

	    long currentTimeMillis = System.currentTimeMillis();
	    long nextUpdateTimeMillis = currentTimeMillis + 12 * 60 * DateUtils.MINUTE_IN_MILLIS;

	    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	    alarmManager.set(AlarmManager.RTC, nextUpdateTimeMillis, pendingIntent);
	  }
}