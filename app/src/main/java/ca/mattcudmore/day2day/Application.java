package ca.mattcudmore.day2day;

import android.database.sqlite.SQLiteDatabase;

import ca.mattcudmore.day2day.db.D2dDatabase;
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

//		try {
//			updateDatabase();
//		} catch (Exception e) {
//			Timber.e(e);
//		}
	}

	void updateDatabase() throws Exception {
		// upgrades during development
		D2dDatabase db = new D2dDatabase(this);
		SQLiteDatabase sdb = db.getWritableDatabase();
		sdb.beginTransaction();

//		// add new columns
//		sdb.execSQL("ALTER TABLE event RENAME TO event_old;");
//		sdb.execSQL(D2dEvent.STMT_CreateTable);
//		sdb.execSQL("INSERT INTO event (_id, created, date, title, comment) SELECT _id, 0, start_day, title, comment FROM event_old;");
//		sdb.execSQL("DROP TABLE event_old;");
//
//		// populate the created field
//		Cursor c = sdb.rawQuery("SELECT * FROM event", null);
//		c.moveToFirst();
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//		Map<Long, Date> dates = new HashMap<>();
//		while (!c.isAfterLast()) {
//			Long id = c.getLong(c.getColumnIndexOrThrow("_id"));
//			Date d = dateFormat.parse(c.getString(c.getColumnIndexOrThrow("date")));
//			dates.put(id, d);
//			c.moveToNext();
//		}
//		c.close();
//		for (Map.Entry<Long, Date> e : dates.entrySet()) {
//			sdb.execSQL("UPDATE event SET created = ? WHERE _id = ?;", new String[]{
//					Long.toString(e.getValue().getTime() / 1000L),
//					Long.toString(e.getKey())
//			});
//		}

		sdb.setTransactionSuccessful();
		sdb.endTransaction();
		db.close();
	}

}
