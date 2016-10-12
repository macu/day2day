package ca.mattcudmore.day2day;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ca.mattcudmore.day2day.db.D2dDatabase;
import ca.mattcudmore.day2day.db.D2dEvent;

public class MainActivity extends AppCompatActivity implements
		DatePickerDialog.OnDateSetListener,
		EventEditDialog.OnDismissListener {

	private Button button_currentDate, button_prevDay, button_nextDay;
	private EditText editText_newEntry;
	private Button button_editNewEntry, button_addNewEntry;
	private ListView listView_events;

	private D2dDatabase db;

	private DayEventsAdapter dayEventsAdapter;

	private Date currentDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState != null) {
			currentDate = (Date) savedInstanceState.getSerializable("currentDate");
		}
		if (currentDate == null) {
			currentDate = new Date();
		}

		button_currentDate = (Button) findViewById(R.id.button_currentDate);
		button_prevDay = (Button) findViewById(R.id.button_prevDay);
		button_nextDay = (Button) findViewById(R.id.button_nextDay);
		editText_newEntry = (EditText) findViewById(R.id.editText_newEvent);
		button_editNewEntry = (Button) findViewById(R.id.button_editNewEvent);
		button_addNewEntry = (Button) findViewById(R.id.button_addNewEvent);
		listView_events = (ListView) findViewById(R.id.listView_events);

		button_currentDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(currentDate);
				int year = cal.get(Calendar.YEAR);
				int month = cal.get(Calendar.MONTH);
				int day = cal.get(Calendar.DAY_OF_MONTH);

				DatePickerDialog dialog = new DatePickerDialog(MainActivity.this, MainActivity.this, year, month, day);

				DatePicker datePicker = dialog.getDatePicker();
				datePicker.setMinDate(db.getMinDate().getTime());
				datePicker.setMaxDate(System.currentTimeMillis());

				dialog.show();
			}
		});

		button_currentDate.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				showDate(new Date()); // show today
				return true;
			}
		});

		button_prevDay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(currentDate);
				cal.add(Calendar.DATE, -1);
				showDate(cal.getTime());
			}
		});

		button_nextDay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(currentDate);
				cal.add(Calendar.DATE, 1);
				showDate(cal.getTime());
			}
		});

		editText_newEntry.addTextChangedListener(
				new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}

					@Override
					public void afterTextChanged(Editable s) {
						boolean textEntered = !editText_newEntry.getText().toString().trim().isEmpty();
						button_addNewEntry.setEnabled(textEntered);
					}
				}
		);

		button_editNewEntry.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EventEditDialog.newInstance(editText_newEntry.getText().toString(), currentDate)
						.show(getSupportFragmentManager(), "createEvent");
			}
		});

		button_addNewEntry.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String title = editText_newEntry.getText().toString();
				D2dEvent event = db.addEvent(currentDate, title, null);
				finishCreateEvent(event);
			}
		});

		dayEventsAdapter = new DayEventsAdapter(this);
		listView_events.setAdapter(dayEventsAdapter);

		listView_events.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long id) {
				EventEditDialog.newInstance(dayEventsAdapter.getItem(i))
						.show(getSupportFragmentManager(), "editEvent");
				return true;
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();

		db = new D2dDatabase(this);
	}

	@Override
	protected void onStop() {
		super.onStop();

		db.close();
		db = null;
	}

	@Override
	protected void onResume() {
		super.onResume();

		showDate(currentDate);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("currentDate", currentDate);

		super.onSaveInstanceState(outState);
	}

	private void showDate(@NonNull Date date) {
		currentDate = date;

		button_currentDate.setText(SimpleDateFormat.getDateInstance().format(currentDate));

		dayEventsAdapter.showEvents(db.getEntries(currentDate));

		button_prevDay.setEnabled(isDateBefore(db.getMinDate(), currentDate));
		button_nextDay.setEnabled(isDateBefore(currentDate, new Date()));
	}

	private boolean isDateBefore(Date d1, Date d2) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(d1);
		cal1.set(Calendar.HOUR, 0);
		cal1.set(Calendar.MINUTE, 0);
		cal1.set(Calendar.SECOND, 0);
		cal1.set(Calendar.MILLISECOND, 0);

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(d2);
		cal2.set(Calendar.HOUR, 0);
		cal2.set(Calendar.MINUTE, 0);
		cal2.set(Calendar.SECOND, 0);
		cal2.set(Calendar.MILLISECOND, 0);

		return cal1.before(cal2);
	}

	@Override
	public void onDateSet(DatePicker datePicker, int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month, day, 0, 0, 0);
		showDate(cal.getTime());
	}

	/* implements EventEditDialog.OnDismissListener ********************************** */

	@Override
	public void finishCreateEvent(D2dEvent event) {
		dayEventsAdapter.prependEvent(event);
		editText_newEntry.setText("");
	}

	@Override
	public void finishEditEvent(D2dEvent event) {
		dayEventsAdapter.updateEvent(event);
	}

	@Override
	public void cancelCreateEvent(String title) {
		// keep the title the user had entered
		editText_newEntry.setText(title);
		editText_newEntry.setSelection(editText_newEntry.getText().length());
	}

	@Override
	public void cancelEditEvent() {
		// do nothing
	}

}
