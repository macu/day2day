package ca.mattcudmore.day2day;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Date;

/**
 * Created by macu on 2016-10-10.
 */
public class EventEntryDialog extends DialogFragment {

	public static EventEntryDialog newInstance(@NonNull String title, @NonNull Date date) {
		EventEntryDialog frag = new EventEntryDialog();
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putSerializable("date", date);
		frag.setArguments(args);
		return frag;
	}

	public static EventEntryDialog newInstance(@NonNull D2dDatabase.EventEntry event) {
		EventEntryDialog frag = new EventEntryDialog();
		Bundle args = new Bundle();
		args.putSerializable("event", event);
		frag.setArguments(args);
		return frag;
	}

	public interface OnDismissListener {
		void finishCreateEvent(D2dDatabase.EventEntry event);

		void finishEditEvent(D2dDatabase.EventEntry event);

		void cancelCreateEvent(String title);

		void cancelEditEvent();
	}

	private D2dDatabase.EventEntry event;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final View view = getActivity().getLayoutInflater().inflate(R.layout.event_edit, null);
		final EditText editText_eventTitle = (EditText) view.findViewById(R.id.editText_eventTitle);
		final EditText editText_eventComment = (EditText) view.findViewById(R.id.editText_eventComment);

		event = (D2dDatabase.EventEntry) getArguments().getSerializable("event");
		if (event != null) {
			editText_eventTitle.setText(event.title);
			editText_eventComment.setText(event.comment);
		} else {
			editText_eventTitle.setText(getArguments().getString("title"));
		}
		editText_eventTitle.setSelection(editText_eventTitle.getText().length());

		AlertDialog d = new AlertDialog.Builder(getActivity())
				.setTitle(event == null ?  R.string.dialog_addEvent_title : R.string.dialog_editEvent_title)
				.setView(view)
				.setPositiveButton(event == null ? R.string.dialog_addEvent_done : R.string.dialog_editEvent_done, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						String title = editText_eventTitle.getText().toString();
						String comment = editText_eventComment.getText().toString();

						D2dDatabase db = new D2dDatabase(getActivity());
						if (event != null) {
							event.title = title;
							event.comment = comment;
							db.save(event);
							((OnDismissListener) getActivity()).finishEditEvent(event);
						} else {
							Date date = (Date) getArguments().getSerializable("date");
							event = db.addEvent(date, title, comment);
							((OnDismissListener) getActivity()).finishCreateEvent(event);
						}
						db.close();

						dismissKeypad();
					}
				})
				.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						if (event != null) {
							((OnDismissListener) getActivity()).cancelEditEvent();
						} else {
							((OnDismissListener) getActivity()).cancelCreateEvent(editText_eventTitle.getText().toString());
						}

						dismissKeypad();
					}
				})
				.create();

		// show keypad when dialog appears
		d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		return d;
	}

	public void dismissKeypad() {
		View currentFocus = getDialog().getCurrentFocus();
		if (currentFocus != null) {
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
		}
	}

}
