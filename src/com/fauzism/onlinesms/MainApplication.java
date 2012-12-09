package com.fauzism.onlinesms;

import android.app.Application;
import android.util.Log;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.PushPreferences;

public class MainApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();

        AirshipConfigOptions options = AirshipConfigOptions.loadDefaultOptions(this);

        // Optionally, customize your config at runtime:
        //
        // options.inProduction = false;
        // options.developmentAppKey = "Your Development App Key";
        // options.developmentAppSecret "Your Development App Secret";

        UAirship.takeOff(this, options);
        PushManager.enablePush();

		PushPreferences prefs = PushManager.shared().getPreferences();
		prefs.setSoundEnabled(false); // enable/disable sound when a push is received
		prefs.setVibrateEnabled(false); // enable/disable vibrate on receive

        PushManager.shared().setIntentReceiver(IntentReceiver.class);
        Logger.logLevel = Log.VERBOSE;
        


    }
}