package ca.mattcudmore.day2day.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;

/**
 * Created by macu on 2016-10-11.
 */
public class D2dEvent implements BaseColumns, Serializable {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

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

	public final long _id;
	public String title, comment;
	public Date startDay, endDay;

	/**
	 * Constructs a new D2dEvent by reading the current row from the given cursor.
	 */
	D2dEvent(Cursor c) {
		_id = c.getLong(c.getColumnIndexOrThrow(_ID));
		title = c.getString(c.getColumnIndexOrThrow(COL_NAME_Title));
		comment = c.getString(c.getColumnIndexOrThrow(COL_NAME_Comment));
		try {
			startDay = D2dDatabase.dateFormat.parse(c.getString(c.getColumnIndexOrThrow(COL_NAME_StartDay)));
			endDay = D2dDatabase.dateFormat.parse(c.getString(c.getColumnIndexOrThrow(COL_NAME_EndDay)));
		} catch (ParseException e) {
			Timber.e(e);
		}
	}

	/**
	 * Constructs a new D2dEvent and inserts it in the given database.
	 */
	D2dEvent(@NonNull SQLiteDatabase db, @NonNull Date date, @NonNull String title, @Nullable String comment) {
		this.title = title;
		this.comment = comment;
		this.startDay = date;
		this.endDay = date;
		this._id = db.insert(TABLE_NAME, null, getValues());
	}

	private ContentValues getValues() {
		ContentValues values = new ContentValues();
		values.put(D2dEvent.COL_NAME_Title, title);
		values.put(D2dEvent.COL_NAME_Comment, comment);
		values.put(D2dEvent.COL_NAME_StartDay, D2dDatabase.dateFormat.format(startDay));
		values.put(D2dEvent.COL_NAME_EndDay, D2dDatabase.dateFormat.format(endDay));
		return values;
	}

	void save(SQLiteDatabase db) {
		db.update(TABLE_NAME, getValues(), _ID + "=?", new String[]{Long.toString(_id)});
	}

}
