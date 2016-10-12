package ca.mattcudmore.day2day;

import timber.log.Timber;

/**
 * Created by macu on 2016-10-08.
 */

public class Application extends android.app.Application {

	@Override
	public void onCreate() {
		super.onCreate();

		if (BuildConfig.DEBUG) {
			Timber.plant(new Timber.DebugTree());
		}
	}
}
