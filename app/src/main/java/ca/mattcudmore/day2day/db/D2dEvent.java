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

	static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	static final String TABLE_NAME = "event";
	static final String COL_NAME_Created = "created"; // seconds since Unix epoch
	static final String COL_NAME_Date = "date"; // yyyy-MM-dd
	static final String COL_NAME_Title = "title";
	static final String COL_NAME_Comment = "comment";

	public static final String STMT_CreateTable
			= "CREATE TABLE " + TABLE_NAME + "("
			+ _ID + " INTEGER PRIMARY KEY,"
			+ COL_NAME_Created + " DATETIME NOT NULL,"
			+ COL_NAME_Date + " TEXT NOT NULL,"
			+ COL_NAME_Title + " TEXT NOT NULL,"
			+ COL_NAME_Comment + " TEXT"
			+ ")";

	static final String[] PROJECTION_AllColumns = {
			_ID,
			COL_NAME_Created,
			COL_NAME_Date,
			COL_NAME_Title,
			COL_NAME_Comment
	};

	public final long _id;
	public final Date created;
	public Date date;
	public String title, comment;

	/**
	 * Constructs a new D2dEvent by reading the current row from the given cursor.
	 */
	D2dEvent(Cursor c) {
		_id = c.getLong(c.getColumnIndexOrThrow(_ID));
		created = new Date(1000L * c.getLong(c.getColumnIndexOrThrow(COL_NAME_Created))); // read seconds
		try {
			date = dateFormat.parse(c.getString(c.getColumnIndexOrThrow(COL_NAME_Date)));
		} catch (ParseException e) {
			Timber.e(e);
		}
		title = c.getString(c.getColumnIndexOrThrow(COL_NAME_Title));
		comment = c.getString(c.getColumnIndexOrThrow(COL_NAME_Comment));
	}

	/**
	 * Constructs a new D2dEvent and inserts it in the given database.
	 */
	D2dEvent(@NonNull SQLiteDatabase db, @NonNull Date date, @NonNull String title, @Nullable String comment) {
		this.created = new Date();
		this.date = date;
		this.title = title;
		this.comment = comment;
		this._id = db.insert(TABLE_NAME, null, getValues());
	}

	private ContentValues getValues() {
		ContentValues values = new ContentValues();
		values.put(D2dEvent.COL_NAME_Created, created.getTime() / 1000L); // write seconds
		values.put(D2dEvent.COL_NAME_Date, dateFormat.format(date));
		values.put(D2dEvent.COL_NAME_Title, title);
		values.put(D2dEvent.COL_NAME_Comment, comment);
		return values;
	}

	void save(SQLiteDatabase db) {
		db.update(TABLE_NAME, getValues(), _ID + "=?", new String[]{Long.toString(_id)});
	}

}
