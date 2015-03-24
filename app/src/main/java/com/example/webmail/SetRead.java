package com.example.webmail;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
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

public class SetRead extends IntentService{
	static String server = null;
	static String imap_address = null;
	static String imap_port = null;
	static String username = null;
	static String password = null;
	static String protocol = null;
	static String folder = null;

	public SetRead() {
		super("SetRead");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		server = prefs.getString(getString(R.string.pref_server_settings_key), getString(R.string.pref_server_settings_default));
		if(server.equalsIgnoreCase("gmail")){
			imap_address = "imap.gmail.com";
			imap_port = "0";
			protocol = "imaps";
			String extra = intent.getStringExtra("FOLDER");
			if(extra.compareTo("Sent")==0)
				extra = "Sent Mail";
			folder = "[Gmail]/"+extra;
			if(extra.compareTo("Inbox") == 0)
				folder = "Inbox";
		}
		else{
			imap_address = "172.16.1.11";
			imap_port = "143";
			protocol = "imap";
			String extra = intent.getStringExtra("FOLDER");
			folder = "INBOX."+extra;
			if(extra.compareTo("Inbox") == 0)
				folder = "Inbox";
		}
		username = prefs.getString(getString(R.string.pref_username_key), getString(R.string.pref_username_default));
		password = prefs.getString(getString(R.string.pref_password_key), getString(R.string.pref_password_default));

		Properties props = new Properties();
		props.setProperty("mail.store.protocol", protocol);
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
			MessageParcel msgParcel = intent.getParcelableExtra("messageParcel");
			if(subject.compareTo(msgParcel.getSub()) == 0 && dt.compareTo(msgParcel.getDate()) == 0){
				inbox.setFlags(new Message[]{msg}, new Flags(Flags.Flag.SEEN), true);
				change(intent.getParcelableExtra("messageParcel"), intent.getStringExtra("FOLDER"));
			}
			store.close();
		}
		catch(MessagingException e){
			e.printStackTrace();
			Log.e("Problem", e.getMessage());
		}
		finally{
			Log.e("READ", "true");
		}
	}

	public void change(Parcelable parcel, String folder){
		MessageParcel msg = (MessageParcel) parcel;
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
			while((mp = (MessageParcel)os.readObject()) != null){
				if(mp.equals(msg)){
					Log.e("Set to READ", "true");
					mp.setRead(1);
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
