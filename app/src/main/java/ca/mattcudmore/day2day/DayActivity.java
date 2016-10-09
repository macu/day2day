package ca.mattcudmore.day2day;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DayActivity extends AppCompatActivity {

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
		editText_newEntry = (EditText) findViewById(R.id.editText_newEntry);
		button_editNewEntry = (Button) findViewById(R.id.button_editNewEntry);
		button_addNewEntry = (Button) findViewById(R.id.button_addNewEntry);
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
						observeAddEntryState();
					}
				}

		);

		button_editNewEntry.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				View dialogView = LayoutInflater.from(DayActivity.this).inflate(R.layout.event_edit, null);
				final EditText editText_eventTitle = (EditText) dialogView.findViewById(R.id.editText_eventTitle);
				final EditText editText_eventComment = (EditText) dialogView.findViewById(R.id.editText_eventComment);

				editText_eventTitle.setText(DayActivity.this.editText_newEntry.getText());
				editText_eventTitle.setSelection(editText_eventTitle.getText().length());

				AlertDialog.Builder dialog = new AlertDialog.Builder(DayActivity.this)
						.setTitle("Add Event")
						.setView(dialogView)
						.setPositiveButton("Add", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								String title = editText_eventTitle.getText().toString();
								String comment = editText_eventComment.getText().toString();
								addEvent(title, comment);
							}
						})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								DayActivity.this.editText_newEntry.setText(editText_eventTitle.getText());
								DayActivity.this.editText_newEntry.setSelection(DayActivity.this.editText_newEntry.getText().length());
							}
						});
				dialog.show();
			}
		});

		button_addNewEntry.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				addEvent(editText_newEntry.getText().toString(), null);
			}
		});

		dayEventsAdapter = new DayEventsAdapter(this);
		listView_events.setAdapter(dayEventsAdapter);
	}

	private void addEvent(@NonNull String title, @Nullable String comment) {
		if (title.trim().isEmpty()) {
			Toast.makeText(this, "Title was empty. No event added.", Toast.LENGTH_SHORT).show();
			return;
		}
		D2dDatabase db = new D2dDatabase(DayActivity.this);
		D2dDatabase.EventEntry entry = db.addEntry(currentDate, title, comment);
		db.close();
		dayEventsAdapter.prependEvent(entry);
		editText_newEntry.setText("");
	}

	@Override
	protected void onResume() {
		super.onResume();

		currentDate = new Date();
		textView_currentDate.setText(SimpleDateFormat.getDateInstance().format(currentDate));

		observeAddEntryState();

		D2dDatabase db = new D2dDatabase(this);
		dayEventsAdapter.showEvents(db.getEntries(currentDate));
		db.close();
	}

	void observeAddEntryState() {
		boolean textEntered = !editText_newEntry.getText().toString().trim().isEmpty();
		button_addNewEntry.setEnabled(textEntered);
	}

}
