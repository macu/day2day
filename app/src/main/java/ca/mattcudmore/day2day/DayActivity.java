package ca.mattcudmore.day2day;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DayActivity extends AppCompatActivity implements EventEntryDialog.OnDismissListener {

	private TextView textView_currentDate;
	private EditText editText_newEntry;
	private Button button_editNewEntry, button_addNewEntry;
	private ListView listView_events;

	private DayEventsAdapter dayEventsAdapter;

	private Date currentDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_day);

		textView_currentDate = (TextView) findViewById(R.id.textView_currentDate);
		editText_newEntry = (EditText) findViewById(R.id.editText_newEvent);
		button_editNewEntry = (Button) findViewById(R.id.button_editNewEvent);
		button_addNewEntry = (Button) findViewById(R.id.button_addNewEvent);
		listView_events = (ListView) findViewById(R.id.listView_events);

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
				EventEntryDialog.newInstance(editText_newEntry.getText().toString(), currentDate)
						.show(getSupportFragmentManager(), "createEvent");
			}
		});

		button_addNewEntry.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String title = editText_newEntry.getText().toString();

				if (title.trim().isEmpty()) {
					Toast.makeText(DayActivity.this, "Title was empty. No event added.", Toast.LENGTH_SHORT).show();
					return;
				}

				D2dDatabase db = new D2dDatabase(DayActivity.this);
				D2dDatabase.EventEntry event = db.addEvent(currentDate, title, null);
				db.close();

				finishCreateEvent(event);
			}
		});

		dayEventsAdapter = new DayEventsAdapter(this);
		listView_events.setAdapter(dayEventsAdapter);

		listView_events.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long id) {
				EventEntryDialog.newInstance(dayEventsAdapter.getItem(i))
						.show(getSupportFragmentManager(), "editEvent");
				return true;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		currentDate = new Date();
		textView_currentDate.setText(SimpleDateFormat.getDateInstance().format(currentDate));

		D2dDatabase db = new D2dDatabase(this);
		dayEventsAdapter.showEvents(db.getEntries(currentDate));
		db.close();
	}

	/* implements EventEntryDialog.OnDismissListener ********************************** */

	@Override
	public void finishCreateEvent(D2dDatabase.EventEntry event) {
		dayEventsAdapter.prependEvent(event);
		editText_newEntry.setText("");
	}

	@Override
	public void finishEditEvent(D2dDatabase.EventEntry event) {
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
