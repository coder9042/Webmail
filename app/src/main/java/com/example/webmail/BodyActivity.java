package com.example.webmail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ShareActionProvider;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import android.preference.PreferenceManager;

public class BodyActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_body);

		getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#A4C639")));

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new BodyFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.body, menu);
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
	public static class BodyFragment extends Fragment {

		static Context context;
		static int staticPosition;
		static String staticFolder;
		static MessageParcel staticMessageParcel;

		static String staticBody;

		public BodyFragment() {
			setHasOptionsMenu(true);
			context = getActivity();
		}

		@Override
		public void onStart(){
			super.onStart();
			context = getActivity();
		}

		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
			inflater.inflate(R.menu.body_menu, menu);

			MenuItem shareMenu = menu.findItem(R.id.action_share);

			ShareActionProvider provider = (ShareActionProvider)MenuItemCompat.getActionProvider(shareMenu);
			if(provider != null){
				provider.setShareIntent(shareIntent());
			}
			else{
				Log.d("Error", "Share Action Provider is null.");
			}
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item){
			int id = item.getItemId();
			if (id == R.id.action_delete) {
				AlertDialog ConfirmDel =new AlertDialog.Builder(getActivity()) 
				//set message, title, and icon
				.setTitle("Delete") 
				.setMessage("Are you sure?")
				.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int whichButton) { 
						//your deleting code
						Intent intent = new Intent(getActivity(), DeleteMail.class);
						intent.putExtra("MY_INDEX", staticPosition);
						intent.putExtra("FOLDER", staticFolder);
						intent.putExtra("messageParcel", (Parcelable)staticMessageParcel);
						getActivity().startService(intent);

						//Intent goBack = new Intent(getActivity(), MainActivity.class);
						//startActivity(goBack);

						dialog.dismiss();
					}   

				})



				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						dialog.dismiss();

					}
				})
				.show();

				return true;

			}
			if (id == R.id.action_forward) {
				Intent intent = new Intent(getActivity(), ComposeActivity.class);
				TextView subject = (TextView) getActivity().findViewById(R.id.subject_view);
				intent.putExtra("SUBJECT", "[Fwd: " + subject.getText() + " ]");
				TextView body = (TextView) getActivity().findViewById(R.id.body_view);
				intent.putExtra("BODY", body.getText().toString());
				startActivity(intent);
				return true;
			}
			if (id == R.id.action_reply) {
				Log.e("REPLY", "TRUE");
				Intent intent = new Intent(getActivity(), ComposeActivity.class);
				TextView from = (TextView) getActivity().findViewById(R.id.from_view);
				TextView subject = (TextView) getActivity().findViewById(R.id.subject_view);
				intent.putExtra("SUBJECT", "[Re: " + subject.getText() + " ]");
				Log.e("FROM", from.getText().toString());
				intent.putExtra("FROM", from.getText().toString());
				startActivity(intent);
				return true;
			}
			if(id == R.id.action_share){
				Intent share = shareIntent();
				startActivity(share);
				return true;
			}
			return super.onOptionsItemSelected(item);
		}

		private Intent shareIntent(){
			Intent share = new Intent(Intent.ACTION_SEND);
			share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			share.setType("text/plain");
			share.putExtra(Intent.EXTRA_TEXT, staticBody+"\n\n#Webmail App");
			return share;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			Intent intent = getActivity().getIntent();
			final int msg_index = intent.getIntExtra("MY_INDEX", -1);
			staticPosition = msg_index;
			final String folder_name = intent.getStringExtra("FOLDER");
			staticFolder = folder_name;
			Log.e("index", String.valueOf(msg_index));
			MessageParcel msg = intent.getParcelableExtra("messageParcel");
			staticMessageParcel = msg;

			View rootView = inflater.inflate(R.layout.fragment_body, container,
					false);
			TextView from = (TextView)rootView.findViewById(R.id.from_view);
			TextView to = (TextView)rootView.findViewById(R.id.to_view);
			TextView date = (TextView)rootView.findViewById(R.id.date_view);
			TextView subject = (TextView)rootView.findViewById(R.id.subject_view);
			TextView body = (TextView)rootView.findViewById(R.id.body_view);


			from.setText(msg.getFrom());

			String to_text = msg.getTo();
			int ind = to_text.indexOf(","); 
			if(ind != -1){
				to_text = to_text.substring(0, ind) + "\n" + to_text.substring(ind);
			}
			to.setText(to_text);

			subject.setText(msg.getSub());
			body.setText(msg.getBody());
			staticBody = msg.getBody();

			date.setText(msg.getDate());

			String cc = msg.getCc();
			String bcc = msg.getBcc();
			if(cc != null){
				TextView cc2 = (TextView)rootView.findViewById(R.id.cc_view);
				cc2.setText(cc);
			}
			if(bcc != null){
				TextView bcc2 = (TextView)rootView.findViewById(R.id.bcc_view);
				bcc2.setText(bcc);
			}

			ArrayList<String> attachments = msg.getAttachmentNames();
			ArrayList<String> sizes = msg.getAttachmentSizes();
			if(attachments.size() != 0){
				LinearLayout attachmentLayout = (LinearLayout) rootView.findViewById(R.id.attachments);

				Log.e("NO.OF ATTACHMENTS", String.valueOf(attachments.size()));
				for(int i=0;i<attachments.size();i++){
					ImageView image = new ImageView(getActivity());
					image.setLayoutParams(new ViewGroup.LayoutParams(
							ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.MATCH_PARENT));
					image.setImageResource(R.drawable.ic_action_attachment);
					image.setBackgroundColor(Color.GRAY);
					image.setPadding(20, 20, 20, 20);

					ImageView download = new ImageView(getActivity());
					download.setLayoutParams(new ViewGroup.LayoutParams(
							ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.MATCH_PARENT));
					download.setImageResource(R.drawable.ic_action_download);
					download.setBackgroundColor(Color.GRAY);
					download.setPadding(20, 20, 20, 20);

					TextView attachment_name = new TextView(getActivity());
					attachment_name.setLayoutParams(new LinearLayout.LayoutParams(
							ViewGroup.LayoutParams.FILL_PARENT,
							0, 1.0f));
					attachment_name.setPadding(10, 0, 10, 0);
					attachment_name.setSingleLine();
					attachment_name.setGravity(Gravity.CENTER);


					TextView attachment_size = new TextView(getActivity());
					attachment_size.setLayoutParams(new LinearLayout.LayoutParams(
							ViewGroup.LayoutParams.FILL_PARENT,
							0, 1.0f));
					attachment_size.setPadding(10, 0, 10, 0);
					attachment_size.setSingleLine();
					attachment_size.setGravity(Gravity.CENTER);

					final int val = i;
					final String my_name = attachment_name.getText().toString();
					download.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View arg0) {
							Intent download_service = new Intent(getActivity(), DownloadAttachment.class);
							download_service.putExtra("MY_INDEX", String.valueOf(msg_index + 1));
							download_service.putExtra("ATTACHMENT_INDEX", String.valueOf(val));
							download_service.putExtra("ATTACHMENT_NAME", my_name);
							download_service.putExtra("FOLDER", folder_name);
							getActivity().startService(download_service);
						}
					});

					String name = attachments.get(i);
					String size = sizes.get(i);
					double s = Double.parseDouble(size);
					int c = 0;
					while(s > 1024){
						s /= 1024;
						c++;
					}
					s = Math.round(s * 100.0) / 100.0;
					if(c == 0)
						size = s + " bytes";
					if(c == 1)
						size = s + " KB ";
					if(c == 2)
						size = s + " MB ";
					attachment_name.setText(name);
					attachment_size.setText(size);

					LinearLayout innerLayout = new LinearLayout(getActivity());
					innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
							0,
							ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
					innerLayout.setOrientation(LinearLayout.VERTICAL);
					innerLayout.addView(attachment_name);
					innerLayout.addView(attachment_size);

					LinearLayout layout = new LinearLayout(getActivity());
					layout.setOrientation(LinearLayout.HORIZONTAL);
					layout.setBackgroundColor(Color.LTGRAY);
					layout.addView(image);
					layout.addView(innerLayout);
					layout.addView(download);

					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					layoutParams.setMargins(0, 5, 0, 5);
					attachmentLayout.addView(layout, i, layoutParams);
				}
			}

			return rootView;
		}

	}

}
