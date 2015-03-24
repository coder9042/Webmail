package com.example.webmail;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;

public class Flag extends IntentService{
	static String server = null;
	static String imap_address = null;
	static String imap_port = null;
	static String username = null;
	static String password = null;
	static String protocol = null;
	static ArrayList<String> folderArray = new ArrayList<String>();
	static String folder = null;

	public Flag() {
		super("Flag");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		server = prefs.getString(getString(R.string.pref_server_settings_key), getString(R.string.pref_server_settings_default));
		if(server.equalsIgnoreCase("gmail")){
			imap_address = "imap.gmail.com";
			imap_port = "0";
			protocol = "imaps";
			folderArray.add("Inbox");
			folderArray.add("[Gmail]/Drafts");
			folderArray.add("[Gmail]/Sent Mail");
			folderArray.add("[Gmail]/Trash");
		}
		else{
			imap_address = "172.16.1.11";
			imap_port = "143";
			protocol = "imap";
			folderArray.add("Inbox");
			folderArray.add("INBOX.Drafts");
			folderArray.add("INBOX.Sent");
			folderArray.add("INBOX.Trash");
		}
		username = prefs.getString(getString(R.string.pref_username_key), getString(R.string.pref_username_default));
		password = prefs.getString(getString(R.string.pref_password_key), getString(R.string.pref_password_default));

		Properties props = new Properties();
		props.setProperty("mail.store.protocol", protocol);
		
		boolean taskDone = false;
		int i = 0;
		HashMap<Integer, String> file_name = new HashMap<Integer, String>();
		file_name.put(0, "inbox");
		file_name.put(1, "drafts");
		file_name.put(2, "sent");
		file_name.put(3, "trash");
		while(!taskDone){
			folder = folderArray.get(i++);
			try{

				Session session = Session.getInstance(props, null);
				Store store = session.getStore(protocol);
				if(imap_port.compareTo("0") == 0)
					store.connect(imap_address, username, password);
				else
					store.connect(imap_address, Integer.parseInt(imap_port), username, password);
				Log.e("Message", "Connected");
				
				Folder inbox = store.getFolder(folder);
				inbox.open(Folder.READ_WRITE);
				int index = intent.getIntExtra("MY_INDEX", -1);
				index = inbox.getMessageCount() - index;
				Log.e("index", String.valueOf(index));
				Message msg = inbox.getMessage(index);
				String subject = msg.getSubject();
				if(subject == null || subject.equals("")){
					subject = "no subject";
				}
				String date = msg.getSentDate().toString();
				String date_parts[] = date.split(" ");
				String dt = date_parts[0] + "\n" + date_parts[1]+" "+date_parts[2] + "\n" +date_parts[3].substring(0, date_parts[3].lastIndexOf(":"));
				
				String dt2 = intent.getStringExtra("DATE");
				if(dt.compareTo(dt2) == 0){
					Log.e("CORRECT", "TRUE");
					inbox.setFlags(new Message[]{msg}, new Flags(Flags.Flag.FLAGGED), intent.getBooleanExtra("FLAG_VALUE", false));
					taskDone = true;
					change(index, file_name.get(i), intent.getBooleanExtra("FLAG_VALUE", false));
				}
				store.close();
			}
			catch(MessagingException e){
				e.printStackTrace();
				Log.e("Problem", e.getMessage());
				break;
			}
		}
		
	}
	public void change(int index, String folder, boolean val){

		ObjectInputStream os = null;
		ObjectOutputStream os2 = null;
		try{
			String path = getApplicationContext().getFilesDir().getPath();
			String name = path + "/" + folder.toLowerCase() + ".ser";
			FileInputStream fs = new FileInputStream(name);
			os = new ObjectInputStream(fs);
			String name2 = path + "/" + folder.toLowerCase() + "_temp.ser";
			FileOutputStream fs2 = new FileOutputStream(name2);
			os2 = new ObjectOutputStream(fs2);
			MessageParcel mp;
			int c = -1;
			while((mp = (MessageParcel)os.readObject()) != null){
				c++;
				if(c == index){
					if(val)
						mp.setFlag(1);
					else
						mp.setFlag(0);
				}
				os2.writeObject(mp);
				Log.e("Copying","true");
			}
		} catch (FileNotFoundException e) {
			Log.e("Exception", "FileNotFound");
		} catch (EOFException e) {
			Log.e("Exception", "EOF");
		} catch(IOException e){
			Log.e("Exception", "IOException");
		} catch (ClassNotFoundException e) {
			Log.e("Exception", "ClassNotFound");
		}
		finally{
			try {
				if(os != null)
					os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(os2 != null)
					os2.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try{
			String path = getApplicationContext().getFilesDir().getPath();
			String name = path + "/" + folder.toLowerCase() + "_temp.ser";
			FileInputStream fs = new FileInputStream(name);
			os = new ObjectInputStream(fs);
			String name2 = path + "/" + folder.toLowerCase() + ".ser";
			FileOutputStream fs2 = new FileOutputStream(name2);
			os2 = new ObjectOutputStream(fs2);
			MessageParcel mp;
			while((mp = (MessageParcel)os.readObject()) != null){
				os2.writeObject(mp);
				Log.e("Overwriting","true");
			}
			
			Log.e("Here","true");
		} catch (FileNotFoundException e) {
			Log.e("Exception", "FileNotFound");
		} catch (EOFException e) {
			Log.e("Exception", "EOF");
		} catch(IOException e){
			Log.e("Exception", "IOException");
		} catch (ClassNotFoundException e) {
			Log.e("Exception", "ClassNotFound");
		}
		finally{
			try {
				if(os != null)
					os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(os2 != null)
					os2.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
