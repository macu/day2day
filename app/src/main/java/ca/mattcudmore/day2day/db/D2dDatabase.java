package ca.mattcudmore.day2day.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * Created by macu on 2016-10-08.
 */
public class D2dDatabase extends SQLiteOpenHelper {

	public static final String DB_NAME = "d2d.db";
	private static final int DB_VERSION = 1;

	public D2dDatabase(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(D2dEvent.STMT_CreateTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
	}

	public Date getMinDate() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(
				D2dEvent.TABLE_NAME,
				new String[]{D2dEvent.COL_NAME_Date},
				null,
				null,
				null,
				null,
				D2dEvent.COL_NAME_Date
		);
		c.moveToFirst();
		if (!c.isAfterLast()) {
			try {
				return D2dEvent.dateFormat.parse(c.getString(c.getColumnIndexOrThrow(D2dEvent.COL_NAME_Date)));
			} catch (ParseException e) {
				Timber.e(e);
			} finally {
				c.close();
			}
		}
		c.close();
		// else return current date
		return new Date();
	}

	@NonNull
	public D2dEvent addEvent(@NonNull Date date, @NonNull String title, @Nullable String comment) {
		return new D2dEvent(getWritableDatabase(), date, title, comment);
	}

	@NonNull
	public List<D2dEvent> getEntries(@NonNull Date date) {
		SQLiteDatabase db = getReadableDatabase();

		String dateString = D2dEvent.dateFormat.format(date);

		Cursor c = db.query(
				D2dEvent.TABLE_NAME,
				D2dEvent.PROJECTION_AllColumns,
				D2dEvent.COL_NAME_Date + "=?",
				new String[]{dateString},
				null,
				null,
				D2dEvent._ID + " DESC"
		);

		List<D2dEvent> entries = new ArrayList<>();
		c.moveToFirst();
		while (!c.isAfterLast()) {
			entries.add(new D2dEvent(c));
			c.moveToNext();
		}

		return entries;
	}

	public void save(@NonNull D2dEvent event) {
		event.save(this.getWritableDatabase());
	}

}
