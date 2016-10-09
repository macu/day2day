package ca.mattcudmore.day2day;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by macu on 2016-10-08.
 */

public class D2dApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		if (BuildConfig.DEBUG) {
			Timber.plant(new Timber.DebugTree());
		}
	}
}
