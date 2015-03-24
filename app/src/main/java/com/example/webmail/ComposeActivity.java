package com.example.webmail;

import java.util.ArrayList;

import com.example.filechooser.FileUtils;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import android.preference.PreferenceManager;

public class ComposeActivity extends ActionBarActivity {

	private static final String TAG = "FileChooserExampleActivity";

	private static final int REQUEST_CODE = 6384; // onActivityResult request
	// code
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose);

		getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#A4C639")));

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new ComposeFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.compose, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class ComposeFragment extends Fragment {

		private static final String TAG = "FileChooserExampleActivity";
		static ArrayList<String> path = null;

		private static final int REQUEST_CODE = 6384; // onActivityResult request
		// code

		public ComposeFragment() {
			setHasOptionsMenu(true);
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
			inflater.inflate(R.menu.compose_menu, menu);
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item){
			int id = item.getItemId();
			if(id == R.id.action_send){
				send_message();
				return true;
			}
			if(id == R.id.action_attachment){
				// Upload code here
				showChooser();
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
		private void showChooser() {
			// Use the GET_CONTENT intent from the utility class
			Intent target = FileUtils.createGetContentIntent();
			// Create the chooser Intent
			Intent intent = Intent.createChooser(
					target, getString(R.string.chooser_title));
			try {
				startActivityForResult(intent, REQUEST_CODE);
			} catch (ActivityNotFoundException e) {
				// The reason for the existence of aFileChooser
			}
		}

		private void showAttachments(String name){
			if(name != null){
				LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.attachlayout);

				LinearLayout attach_layout = new LinearLayout(getActivity());
				attach_layout.setOrientation(LinearLayout.HORIZONTAL);
				attach_layout.setPadding(0, 10, 0, 10);
				attach_layout.setBackgroundColor(Color.LTGRAY);
				
				TextView heading = new TextView(getActivity());
				heading.setLayoutParams(new LinearLayout.LayoutParams(
						0,
						ViewGroup.LayoutParams.MATCH_PARENT, 3.0f));
				heading.setGravity(Gravity.CENTER);
				heading.setText(name.substring(name.lastIndexOf("/")+1));
				
				ImageView delete = new ImageView(getActivity());
				delete.setLayoutParams(new LinearLayout.LayoutParams(
						0,
						ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
				delete.setImageResource(R.drawable.ic_action_remove);
				
				final String delname = name;
				final LinearLayout rem = attach_layout;
				delete.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						Log.e("REMOVE", delname);
						path.remove(path.indexOf(delname));
						rem.setVisibility(View.GONE);
					}
				});
				
				attach_layout.addView(heading);
				attach_layout.addView(delete);
				
				layout.addView(attach_layout);
			}
		}

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			switch (requestCode) {
			case REQUEST_CODE:
				// If the file selection was successful
				if (resultCode == RESULT_OK) {
					if (data != null) {
						// Get the URI of the selected file
						final Uri uri = data.getData();
						Log.i(TAG, "Uri = " + uri.toString());
						try {
							// Get the file path from the URI
							if(path == null)
								path = new ArrayList<String>();
							path.add(FileUtils.getPath(MainActivity.getAppContext(), uri));
							Log.e("PATHHHHHHHHHHHHHHHh", path.get(path.size()-1));
							showAttachments(path.get(path.size()-1));

						} catch (Exception e) {
							Log.e("FileSelectorTestActivity", "File select error", e);
						}
					}
				}
				break;
			}
			super.onActivityResult(requestCode, resultCode, data);
		}




		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_compose,
					container, false);
			Intent intent = getActivity().getIntent();
			if(intent != null){
				String sub = intent.getStringExtra("SUBJECT");
				if(sub != null)
					((EditText) rootView.findViewById(R.id.compose_subject)).setText(sub);
				String to = intent.getStringExtra("FROM");
				if(to != null)
					((EditText) rootView.findViewById(R.id.compose_to)).setText(to);
				String body = intent.getStringExtra("BODY");
				if(body != null)
					((EditText) rootView.findViewById(R.id.compose_body)).setText(body);
			}
			return rootView;
		}
		
		public String append(String x){
			if(x.equals(""))
				return x;
			String y = "";
			String vals[] = x.split(",");
			for(int i=0;i<vals.length;i++){
				if(vals[i].indexOf("@") == -1){
					vals[i] += "@iitp.ac.in";
				}
				y += vals[i];
				if(i < vals.length - 1)
					y += ",";
			}
			return y;
		}

		public void send_message(){
			String subject = ((EditText) getActivity().findViewById(R.id.compose_subject)).getText().toString();
			String to = ((EditText) getActivity().findViewById(R.id.compose_to)).getText().toString();
			String cc = ((EditText) getActivity().findViewById(R.id.compose_cc)).getText().toString();
			String bcc = ((EditText) getActivity().findViewById(R.id.compose_bcc)).getText().toString();
			String body = ((EditText) getActivity().findViewById(R.id.compose_body)).getText().toString();
			
			if(cc == null){
				cc = "";
			}
			if(bcc == null){
				bcc = "";
			}
			
			to = append(to);
			cc = append(cc);
			bcc = append(bcc);
		

			if(to == null || to.equals("")){
				Toast.makeText(getActivity(), "To field cannot be empty.", Toast.LENGTH_SHORT).show();
			}
			else if(to.contains(" ") || cc.contains(" ") || bcc.contains(" ")){
				Toast.makeText(getActivity(), "TO/CC/BCC cannot contain spaces.", Toast.LENGTH_SHORT).show();
			}
			else{
				String vals[] = {subject, to, cc, bcc, body};
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
				String server = prefs.getString(getString(R.string.pref_server_settings_key), getString(R.string.pref_server_settings_default));
				if(server.equalsIgnoreCase("gmail")){
					Intent send = new Intent(getActivity(), SendMessage2.class);
					send.putExtra("MESSAGE_DETAILS", vals);
					send.putExtra("ATTACHMENT_DETAILS", path);
					getActivity().startService(send);
				}
				else{
					Intent send = new Intent(getActivity(), SendMessage.class);
					send.putExtra("MESSAGE_DETAILS", vals);
					send.putExtra("ATTACHMENT_DETAILS", path);
					getActivity().startService(send);
				}
				path=null;
				LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.attachlayout);
				layout.removeAllViews();
			}
		}
	}

}
