package ca.mattcudmore.day2day;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import ca.mattcudmore.day2day.db.D2dEvent;

/**
 * Created by macu on 2016-10-08.
 */
public class DayEventsAdapter extends BaseAdapter {
	private final Context context;
	private LayoutInflater inflater;

	private List<D2dEvent> events;

	public DayEventsAdapter(Context context) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.events = Collections.emptyList();
	}

	public void showEvents(List<D2dEvent> events) {
		this.events = events;
		notifyDataSetChanged();
	}

	public void prependEvent(D2dEvent event) {
		this.events.add(0, event);
		notifyDataSetChanged();
	}

	public void updateEvent(D2dEvent event) {
		for (int i = 0; i < events.size(); i++) {
			if (events.get(i)._id == event._id) {
				events.set(i, event);
				notifyDataSetChanged();
				break;
			}
		}
	}

	@Override
	public int getCount() {
		return events.size();
	}

	@Override
	public D2dEvent getItem(int i) {
		return events.get(i);
	}

	@Override
	public long getItemId(int i) {
		return events.get(i)._id;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		if (view == null) {
			view = inflater.inflate(R.layout.cell_event, null);
		}

		D2dEvent event = events.get(i);

		TextView textView_eventTitle = ((TextView) view.findViewById(R.id.textView_eventTitle));
		TextView textView_eventComment = ((TextView) view.findViewById(R.id.textView_eventComment));

		textView_eventTitle.setText(event.title);
		if (event.comment == null || event.comment.trim().isEmpty()) {
			textView_eventComment.setVisibility(View.GONE);
		} else {
			textView_eventComment.setText(event.comment);
			textView_eventComment.setVisibility(View.VISIBLE);
		}

		return view;
	}

}
