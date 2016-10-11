package ca.mattcudmore.day2day;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 * Created by macu on 2016-10-08.
 */
public class D2dDatabase extends SQLiteOpenHelper {

	private static final String DB_NAME = "d2d.db";
	private static final int DB_VERSION = 1;

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	static class EventEntry implements BaseColumns, Serializable {
		static final String TABLE_NAME = "event";
		static final String COL_NAME_Title = "title";
		static final String COL_NAME_Comment = "comment";
		static final String COL_NAME_StartDay = "start_day"; // yyyy-mm-dd
		static final String COL_NAME_EndDay = "end_day"; // yyyy-mm-dd

		static final String STMT_CreateTable
				= "CREATE TABLE " + TABLE_NAME + "("
				+ _ID + " INTEGER PRIMARY KEY,"
				+ COL_NAME_Title + " TEXT NOT NULL,"
				+ COL_NAME_Comment + " TEXT,"
				+ COL_NAME_StartDay + " TEXT DEFAULT CURRENT_DATE,"
				+ COL_NAME_EndDay + " TEXT DEFAULT CURRENT_DATE"
				+ ")";

		static final String[] PROJECTION_AllColumns = {
				_ID,
				COL_NAME_Title,
				COL_NAME_Comment,
				COL_NAME_StartDay,
				COL_NAME_EndDay
		};

		long _id;
		String title, comment;
		Date startDay, endDay;

		private EventEntry(Cursor c) {
			_id = c.getLong(c.getColumnIndexOrThrow(_ID));
			title = c.getString(c.getColumnIndexOrThrow(COL_NAME_Title));
			comment = c.getString(c.getColumnIndexOrThrow(COL_NAME_Comment));
			try {
				startDay = dateFormat.parse(c.getString(c.getColumnIndexOrThrow(COL_NAME_StartDay)));
				endDay = dateFormat.parse(c.getString(c.getColumnIndexOrThrow(COL_NAME_EndDay)));
			} catch (ParseException e) {
				Timber.e(e);
			}
		}

		private EventEntry(@NonNull SQLiteDatabase db, @NonNull Date date, @NonNull String title, @Nullable String comment) {
			this.title = title;
			this.comment = comment;
			this.startDay = date;
			this.endDay = date;
			this._id = db.insert(TABLE_NAME, null, getValues());
		}

		private ContentValues getValues() {
			ContentValues values = new ContentValues();
			values.put(EventEntry.COL_NAME_Title, title);
			values.put(EventEntry.COL_NAME_Comment, comment);
			values.put(EventEntry.COL_NAME_StartDay, dateFormat.format(startDay));
			values.put(EventEntry.COL_NAME_EndDay, dateFormat.format(endDay));
			return values;
		}

		private void save(SQLiteDatabase db) {
			db.update(TABLE_NAME, getValues(), _ID + "=?", new String[]{Long.toString(_id)});
		}
	}

	public D2dDatabase(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(EventEntry.STMT_CreateTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
	}

	public Date getMinDate() {
		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.query(
				EventEntry.TABLE_NAME,
				new String[]{EventEntry.COL_NAME_StartDay},
				null,
				null,
				null,
				null,
				EventEntry.COL_NAME_StartDay
		);
		c.moveToFirst();
		if (!c.isAfterLast()) {
			try {
				return dateFormat.parse(c.getString(c.getColumnIndexOrThrow(EventEntry.COL_NAME_StartDay)));
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
	public EventEntry addEvent(@NonNull Date date, @NonNull String title, @Nullable String comment) {
		return new EventEntry(getWritableDatabase(), date, title, comment);
	}

	@NonNull
	public List<EventEntry> getEntries(@NonNull Date date) {
		SQLiteDatabase db = getReadableDatabase();

		String dateString = dateFormat.format(date);

		Cursor c = db.query(
				EventEntry.TABLE_NAME,
				EventEntry.PROJECTION_AllColumns,
				EventEntry.COL_NAME_StartDay + "<=? AND " + EventEntry.COL_NAME_EndDay + ">=?",
				new String[]{dateString, dateString},
				null,
				null,
				EventEntry._ID + " DESC"
		);

		List<EventEntry> entries = new ArrayList<>();
		c.moveToFirst();
		while (!c.isAfterLast()) {
			entries.add(new EventEntry(c));
			c.moveToNext();
		}

		return entries;
	}

	public void save(@NonNull EventEntry event) {
		event.save(this.getWritableDatabase());
	}

}
