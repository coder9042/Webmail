package com.example.webmail;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class DeleteMail extends IntentService{
	static String server = null;
	static String imap_address = null;
	static String imap_port = null;
	static String username = null;
	static String password = null;
	static String protocol = null;
	static String folder = null;
	static String trash_folder = null;

	static Handler mHandler;

	public DeleteMail() {
		super("DeleteMail");
		mHandler = new Handler();
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
			trash_folder = "[Gmail]/Trash";
		}
		else{
			imap_address = "172.16.1.11";
			imap_port = "143";
			protocol = "imap";
			String extra = intent.getStringExtra("FOLDER");
			folder = "INBOX."+extra;
			if(extra.compareTo("Inbox") == 0)
				folder = "Inbox";
			trash_folder = "INBOX.Trash";
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
			Folder trash = store.getFolder(trash_folder);
			trash.open(Folder.READ_WRITE);

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
				Log.e("Found", "true");
				if(folder.compareTo(trash_folder) != 0){
					inbox.copyMessages(new Message[]{msg}, trash);
					Log.e("Copied", "true");
				}
				inbox.setFlags(new Message[]{msg}, new Flags(Flags.Flag.DELETED), true);
				Log.e("Removed", "true");
				mHandler.post(new Runnable() {            
					@Override
					public void run() {
						Toast.makeText(DeleteMail.this, "Message Deleted", Toast.LENGTH_SHORT).show();                
					}
				});
				delete(intent.getParcelableExtra("messageParcel"), intent.getStringExtra("FOLDER"));

				Intent dialogIntent = new Intent(this, MainActivity.class);
				dialogIntent.setAction(Intent.ACTION_VIEW);
				dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				dialogIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				this.startActivity(dialogIntent);
				Log.e("DELETED", "true");
			}
			else{
				mHandler.post(new Runnable() {            
					@Override
					public void run() {
						Toast.makeText(DeleteMail.this, "Error in deleting message. Resync and try again.", Toast.LENGTH_SHORT).show();                
					}
				});
			}
			boolean expunge = true;
			inbox.close(expunge);
			store.close();
		}
		catch(MessagingException e){
			e.printStackTrace();
		}
	}
	public void delete(Parcelable parcel, String folder){

		MessageParcel msg = (MessageParcel) parcel;
		try{
			String path = getApplicationContext().getFilesDir().getPath();
			String name = path + "/" + folder.toLowerCase() + ".ser";
			FileInputStream fs = new FileInputStream(name);
			ObjectInputStream os = new ObjectInputStream(fs);
			String name2 = path + "/" + folder.toLowerCase() + "_temp.ser";
			FileOutputStream fs2 = new FileOutputStream(name2);
			ObjectOutputStream os2 = new ObjectOutputStream(fs2);
			MessageParcel mp;
			while((mp = (MessageParcel)os.readObject()) != null){
				if(mp.equals(msg)){
					Log.e("Deleted","true");
					continue;
				}
				os2.writeObject(mp);
				Log.e("Copying","true");
			}
			os.close();
			os2.close();
		} catch (FileNotFoundException e) {
			Log.e("Exception", "FileNotFound");
		} catch (EOFException e) {
			Log.e("Exception", "EOF");
		} catch(IOException e){
			Log.e("Exception", "IOException");
		} catch (ClassNotFoundException e) {
			Log.e("Exception", "ClassNotFound");
		}
		try{
			String path = getApplicationContext().getFilesDir().getPath();
			String name = path + "/" + folder.toLowerCase() + "_temp.ser";
			FileInputStream fs = new FileInputStream(name);
			ObjectInputStream os = new ObjectInputStream(fs);
			String name2 = path + "/" + folder.toLowerCase() + ".ser";
			FileOutputStream fs2 = new FileOutputStream(name2);
			ObjectOutputStream os2 = new ObjectOutputStream(fs2);
			MessageParcel mp;
			while((mp = (MessageParcel)os.readObject()) != null){
				os2.writeObject(mp);
				Log.e("Overwriting","true");
			}
			os.close();
			os2.close();
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

	}

}
