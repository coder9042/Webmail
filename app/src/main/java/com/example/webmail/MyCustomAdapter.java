package com.example.webmail;

import java.util.ArrayList;

import com.example.webmail.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyCustomAdapter extends ArrayAdapter<String>{
	private final Context context;
	private final ArrayList<String> values;
	static String flagString = "";
	private SparseBooleanArray mSelectedItemsIds=new SparseBooleanArray();

	public MyCustomAdapter(Context context, ArrayList<String> values){
		super(context, R.layout.list_view_item, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent){
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.list_view_item, parent, false);
		LinearLayout layout = (LinearLayout) rowView.findViewById(R.id.list_item_layout);
		//int rnd = (int)(Math.random() * 10);
		//String rn = "bg"+String.valueOf(rnd);
		//layout.setBackgroundResource(rowView.getResources().getIdentifier(rn, "color", "com.example.webmail"));

		final TextView from = (TextView)rowView.findViewById(R.id.list_item_from);
		final TextView subject = (TextView)rowView.findViewById(R.id.list_item_subject);
		final TextView date = (TextView)rowView.findViewById(R.id.list_item_date);
		final ImageView flag = (ImageView)rowView.findViewById(R.id.list_item_flag);
		ImageView icon = (ImageView)rowView.findViewById(R.id.list_item_icon);

		String vals[] = values.get(position).split("///");
		//String date_parts[] = vals[1].split(" ");
		//String dt = date_parts[0] + "\n" + date_parts[1]+" "+date_parts[2];
		date.setText(vals[1]);
		from.setText(vals[0]);
		subject.setText(vals[2].toUpperCase() + " -- " + vals[4]);

		if(vals[3].compareTo("unread") == 0){
			icon.setImageResource(R.drawable.mail_unread);
			from.setTypeface(null, Typeface.BOLD);
			subject.setTypeface(null, Typeface.BOLD);
		}
		else{
			icon.setImageResource(R.drawable.mail_read);
		}

		if(vals[5].compareTo("flagged") == 0){
			flag.setImageResource(R.drawable.flagged);
			from.setTextColor(Color.parseColor("#8B2323"));
			subject.setTextColor(Color.parseColor("#8B2323"));
			//from.setTypeface(null, Typeface.BOLD);
			//subject.setTypeface(null, Typeface.BOLD);
		}
		else{
			flag.setImageResource(R.drawable.unflagged);
		}
 
		flagString = vals[5];
		final int pos = position;
		flag.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				String f = values.get(pos);
				int i = f.lastIndexOf("///");
				f = f.substring(i + 3);
				Log.e("f", f);
				if(f.compareTo("flagged") == 0){
					flag.setImageResource(R.drawable.unflagged);
					from.setTextColor(Color.BLACK);
					subject.setTextColor(Color.BLACK);
					from.setTypeface(null, Typeface.NORMAL);
					subject.setTypeface(null, Typeface.NORMAL);
					
					String val = values.get(pos);
					int ind = val.lastIndexOf("///");
					val = val.substring(0, ind) + "///" + "unflagged";
					values.set(pos, val);
					notifyDataSetChanged();
					
					Intent setFlag = new Intent(context, Flag.class);
					setFlag.putExtra("MY_INDEX", pos);
					setFlag.putExtra("DATE", date.getText().toString());
					setFlag.putExtra("FLAG_VALUE", false);
					context.startService(setFlag);
				}
				else{
					flag.setImageResource(R.drawable.flagged);
					from.setTextColor(Color.parseColor("#8B2323"));
					subject.setTextColor(Color.parseColor("#8B2323"));
					//from.setTypeface(null, Typeface.BOLD);
					//subject.setTypeface(null, Typeface.BOLD);
					
					String val = values.get(pos);
					int ind = val.lastIndexOf("///");
					val = val.substring(0, ind) + "///" + "flagged";
					values.set(pos, val);
					notifyDataSetChanged();
					
					Intent setFlag = new Intent(context, Flag.class);
					setFlag.putExtra("MY_INDEX", pos);
					setFlag.putExtra("DATE", date.getText().toString());
					setFlag.putExtra("FLAG_VALUE", true);
					context.startService(setFlag);
				}
			}
		});

		return rowView;
	}
	@Override
	public void remove(String object) {
		values.remove(object);
		notifyDataSetChanged();
	}

	public ArrayList<String> getString() {
		return values;
	}

	public void toggleSelection(int position) {
		Log.e("positon3", Integer.toString(position));
		selectView(position, !mSelectedItemsIds.get(position));
		Log.e("positon4", Integer.toString(position));
	}

	public void removeSelection() {
		mSelectedItemsIds = new SparseBooleanArray();
		notifyDataSetChanged();
	}

	public void selectView(int position, boolean value) {
		if (value)
		{
			mSelectedItemsIds.put(position, value);

		}else
			mSelectedItemsIds.delete(position);
		notifyDataSetChanged();
	}

	public int getSelectedCount() {
		return mSelectedItemsIds.size();
	}

	public SparseBooleanArray getSelectedIds() {
		return mSelectedItemsIds;
	}
}
