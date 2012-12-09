package com.fauzism.onlinesms;

import java.util.HashMap;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;

public class ContactFetcher extends Service {
 
   String tag="OnlineSMS";
   HashMap<String, String> phonebook = new HashMap<String, String>();
   @Override
   public void onCreate() {
       super.onCreate();    
       Log.i(tag, "Service created...");
   }
 
   @Override
   public void onStart(Intent intent, int startId) {      
       super.onStart(intent, startId);  
       Log.i(tag, "Service started...");
       new Thread(new Runnable() {
    	      public void run() {
    	    	  populateContact();
    	    	  stopSelf();
    	      }
    	    }).start();
    	  
       //populateContact();
   }
   
   @Override
   public void onDestroy() {
       super.onDestroy();
   }

   @Override
   public IBinder onBind(Intent intent) {
       return null;
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
		      while (phones.moveToNext()) { 
		         String phoneNumber = phones.getString(phones.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));
		         phonebook.put(name, phoneNumber);
		      } 
		      phones.close(); 
		   }
		   /*
		   Cursor emails = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId, null, null); 
		   while (emails.moveToNext()) { 
		      // This would allow you get several email addresses 
		      String emailAddress = emails.getString( 
		      emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)); 
		   } 
		   emails.close();
		   */
		}
		cursor.close();
		Log.d("OnlineSMS", phonebook.toString());
	}
}