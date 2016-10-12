package ca.mattcudmore.day2day;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ca.mattcudmore.day2day.db.D2dDatabase;
import ca.mattcudmore.day2day.db.D2dEvent;

public class MainActivity extends AppCompatActivity implements
		DatePickerDialog.OnDateSetListener,
		EventEditDialog.OnDismissListener {

	private Button button_currentDate, button_prevDay, button_nextDay;
	private ViewGroup layout_newEvent;
	private EditText editText_newEntry;
	private Button button_editNewEvent, button_addNewEvent;
	private ListView listView_events;

	private D2dDatabase db;

	private DayEventsAdapter dayEventsAdapter;

	private Date displayedDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState != null) {
			displayedDate = (Date) savedInstanceState.getSerializable("displayedDate");
		}
		if (displayedDate == null) {
			displayedDate = new Date();
		}

		button_currentDate = (Button) findViewById(R.id.button_currentDate);
		button_prevDay = (Button) findViewById(R.id.button_prevDay);
		button_nextDay = (Button) findViewById(R.id.button_nextDay);
		layout_newEvent = (ViewGroup) findViewById(R.id.layout_newEvent);
		editText_newEntry = (EditText) findViewById(R.id.editText_newEvent);
		button_editNewEvent = (Button) findViewById(R.id.button_editNewEvent);
		button_addNewEvent = (Button) findViewById(R.id.button_addNewEvent);
		listView_events = (ListView) findViewById(R.id.listView_events);

		button_currentDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(displayedDate);
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
				cal.setTime(displayedDate);
				cal.add(Calendar.DATE, -1);
				showDate(cal.getTime());
			}
		});

		button_nextDay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(displayedDate);
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
						button_addNewEvent.setEnabled(textEntered);
					}
				}
		);

		editText_newEntry.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					if (!editText_newEntry.getText().toString().trim().isEmpty()) {
						submitNewEvent();
						return true;
					} else {
						Toast.makeText(MainActivity.this, R.string.toast_enterEventName, Toast.LENGTH_SHORT).show();
					}
				}
				return false;
			}
		});

		button_editNewEvent.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				EventEditDialog.newInstance(editText_newEntry.getText().toString(), displayedDate)
						.show(getSupportFragmentManager(), "createEvent");
			}
		});

		button_addNewEvent.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				submitNewEvent();
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

		showDate(displayedDate);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("displayedDate", displayedDate);

		super.onSaveInstanceState(outState);
	}

	private void showDate(@NonNull Date date) {
		displayedDate = date;

		Date today = new Date();

		button_currentDate.setText(SimpleDateFormat.getDateInstance().format(displayedDate));
		button_prevDay.setEnabled(isDateBefore(db.getMinDate(), displayedDate));
		button_nextDay.setEnabled(isDateBefore(displayedDate, today));

		// only allow adding events yesterday and today
		Calendar yesterday = Calendar.getInstance();
		yesterday.setTime(today);
		yesterday.add(Calendar.DATE, -1);
		layout_newEvent.setVisibility(isDateBefore(displayedDate, yesterday.getTime()) ? View.GONE : View.VISIBLE);

		dayEventsAdapter.showEvents(db.getEntries(displayedDate));
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

	private void submitNewEvent() {
		String title = editText_newEntry.getText().toString();
		if (title.trim().isEmpty()) {
			title = getString(R.string.blank_event_name);
		}
		D2dEvent event = db.addEvent(displayedDate, title, null);
		finishCreateEvent(event);
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
