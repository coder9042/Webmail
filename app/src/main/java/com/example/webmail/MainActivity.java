package com.example.webmail;

import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import android.preference.PreferenceManager;

import javax.mail.*;
import javax.mail.Flags.Flag;
import javax.mail.Message.RecipientType;

import com.example.webmail.R;

public class MainActivity extends ActionBarActivity {

	DrawerLayout mDrawerLayout;
	ListView mDrawerList;
	ActionBarDrawerToggle mDrawerToggle;
	TextView userField;
	TextView settingsField;
	TextView aboutField;
	TextView feedbackField;
	LinearLayout lDrawer;
	static ActionBarActivity staticContext;
	
	static String fragmentName = "Inbox";
	
	static private Context context;
	public static Context getAppContext() {
        return MainActivity.context;
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		staticContext = this;
		MainActivity.context=getApplicationContext();
		
		Intent shareIntent = getIntent();
		if ((shareIntent != null && shareIntent.getType() != null) && (shareIntent.getType().indexOf("text/plain") != -1 || shareIntent.getType().indexOf("message/rfc822") != -1)) {
	        Intent transfer = new Intent(staticContext, ComposeActivity.class);
	        
	        String body = "";
	        if(shareIntent.hasExtra("android.intent.extra.TEXT"))
	        	body = shareIntent.getStringExtra("android.intent.extra.TEXT");
	        else if(shareIntent.hasExtra("android.intent.extra.EMAIL"))
	        	body = shareIntent.getStringExtra("android.intent.extra.EMAIL");
	        
	        transfer.putExtra("BODY", body);
	        startActivity(transfer);
	    }
		
		getSupportActionBar().setTitle("Webmail");
		getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#A4C639")));

		userField = (TextView) findViewById(R.id.menu_drawer_username);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String name = prefs.getString(getString(R.string.pref_username_key), getString(R.string.pref_username_default));
		String pwd = prefs.getString(getString(R.string.pref_password_key), getString(R.string.pref_password_default));
		
		if(name.equals("") || pwd.equals("")){
			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
		}
		
		userField.setText(name);

		settingsField = (TextView) findViewById(R.id.menu_drawer_settings);
		aboutField = (TextView) findViewById(R.id.menu_drawer_about);
		feedbackField = (TextView) findViewById(R.id.menu_drawer_feedback);
		
		userField.setOnClickListener(new TextView.OnClickListener() {

			@Override
			public void onClick(View v) {
			}
		});

		settingsField.setOnClickListener(new TextView.OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.e("CLICKED", "SETTINGS");
				Intent intent = new Intent(staticContext, SettingsActivity.class);
				startActivity(intent);
			}
		});

		aboutField.setOnClickListener(new TextView.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(staticContext, AboutActivity.class);
				startActivity(intent);
			}
		});

		feedbackField.setOnClickListener(new TextView.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(staticContext, ComposeActivity.class);
				TextView subject = (TextView) staticContext.findViewById(R.id.subject_view);
				intent.putExtra("SUBJECT", "FeedBack WEBMAIL App");
				intent.putExtra("FROM", "anubhav9042@gmail.com,shashank.iitp@gmail.com");
				startActivity(intent);
			}
		});
		
		View filler = findViewById(R.id.filler);
		filler.setOnClickListener(new TextView.OnClickListener() {

			@Override
			public void onClick(View v) {
				// do nothing
			}
		});

		lDrawer = (LinearLayout) findViewById(R.id.linear_drawer);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
		mDrawerList = (ListView) findViewById(R.id.drawer_list);

		// Set the adapter for the list view
		final ArrayList<String> dummy = new ArrayList<String>();
		dummy.add("INBOX");
		dummy.add("DRAFTS");
		dummy.add("SENT");
		dummy.add("TRASH");
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.menu_drawer_list_item, R.id.menu_drawer_list_item_textview, dummy));
		

		mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long l){
				if(position == 0){
					fragmentName = "Inbox";
					getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, new InboxFragment(), fragmentName).commit();
				}
				else if(position == 1){
					fragmentName = "Drafts";
					getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, new DraftsFragment(), fragmentName).commit();
				}
				else if(position == 2){
					fragmentName = "Sent";
					getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, new SentFragment(), fragmentName).commit();
				}
				else{
					fragmentName = "Trash";
					getSupportFragmentManager().beginTransaction()
					.replace(R.id.container, new TrashFragment(), fragmentName).commit();
				}
				
				mDrawerLayout.closeDrawer(lDrawer);
			}
		});

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.app_name, R.string.app_name) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				getSupportActionBar().setTitle(fragmentName);
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getSupportActionBar().setTitle("Webmail");
			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		boolean notif_open = false;
		Intent intent = getIntent();
		if(intent != null && intent.getExtras() != null && intent.getExtras().containsKey("NOTIF")){
			notif_open = true;
		}
		Log.e("NOTIF", String.valueOf(notif_open));
		if(notif_open)
			fragmentName = "Inbox";
		getSupportActionBar().setTitle(fragmentName);
		
		Log.e("FRAGMENT-NAME", fragmentName);

		if (savedInstanceState == null || notif_open) {
			if(fragmentName.compareTo("Inbox") == 0){
				getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, new InboxFragment(), fragmentName).commit();
			}
			else if(fragmentName.compareTo("Drafts") == 0){
				getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, new DraftsFragment(), fragmentName).commit();
			}
			else if(fragmentName.compareTo("Sent") == 0){
				getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, new SentFragment(), fragmentName).commit();
			}
			else{
				getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, new TrashFragment(), fragmentName).commit();
			}
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// For Drawer
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		Log.e("BACK PRESSED", "TRUE");
	    fragmentName = "Inbox";
	    moveTaskToBack(true);
	}

}
