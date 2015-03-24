package com.example.webmail;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.NetworkOnMainThreadException;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class Fetch extends IntentService{

	Message[] message;
	MessageParcel[] messageParcelsTemp;
	Handler mHandler;

	public Fetch() {
		super("Fetch");
		mHandler = new Handler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		String protocol = intent.getStringExtra("PROTOCOL");
		String imap_address = intent.getStringExtra("IMAP_ADDRESS");
		String imap_port = intent.getStringExtra("IMAP_PORT");
		String username = intent.getStringExtra("USERNAME");
		String password = intent.getStringExtra("PASSWORD");
		String folder = intent.getStringExtra("FOLDER");
		int count = intent.getIntExtra("COUNT", 0);

		Log.e("FETCH SERVICE STARTED", "true");
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", protocol);
		try {
			Session session = Session.getInstance(props, null);
			Store store = session.getStore(protocol);
			if(imap_port.compareTo("0") == 0)
				store.connect(imap_address, username, password);
			else
				store.connect(imap_address, Integer.parseInt(imap_port), username, password);
			Log.e("Message", "Connected");

			Folder inbox = store.getFolder(folder);
			inbox.open(Folder.READ_ONLY);

			int messageCount = inbox.getMessageCount();
			int deletedCount = inbox.getDeletedMessageCount();
			Log.e("Deleted Count", String.valueOf(deletedCount));

			int startCount = messageCount - (count + 4);
			if(startCount < 1)
				startCount = 1;
			int endCount = messageCount-count;
			message = inbox.getMessages(startCount, messageCount);
			messageParcelsTemp = new MessageParcel[message.length];

			for(int i=0;i<message.length;i++)
			{
				Message msg=message[i];
				MessageParcel mp = new MessageParcel(msg);
				Log.e("index", String.valueOf(i));
				messageParcelsTemp[i] = mp;
			}

			int end_index = message.length - 1;
			for(int i=0;i<message.length;i++){
				if(messageParcelsTemp[i].getFrom() == null || messageParcelsTemp[i].getDate() == null || messageParcelsTemp[i].getSub() == null || messageParcelsTemp[i].getTo() == null || messageParcelsTemp[i].getBody() == null){
					end_index = i-1;
					break;
				}
			}
			Log.e("end_index", String.valueOf(end_index));
			int new_length = end_index+1;
			Log.e("message.length", String.valueOf(message.length));
			if(new_length != message.length){
				messageParcelsTemp = null;
			}
			store.close();
			Object arr[] = new Object[2];
			arr[0] = true;
			arr[1] = "";
			onComplete(arr, intent.getStringExtra("FILE_NAME"));

		} catch(AuthenticationFailedException e){
			Log.e("ERROR", e.getMessage());
			Object arr[] = new Object[2];
			arr[0] = false;
			arr[1] = "Authentication Failure";
			onComplete(arr, intent.getStringExtra("FILE_NAME"));
		}
		
		catch (MessagingException mex) {
			Log.e("ERROR", mex.getMessage());
			Object arr[] = new Object[2];
			arr[0] = false;
			arr[1] = "No Connection";
			onComplete(arr, intent.getStringExtra("FILE_NAME"));
		}

	}
	public void onComplete(Object[] obj, String file_name){
		
		final Context context = getApplicationContext();

		boolean val = (Boolean)obj[0];
		final String msg = (String)obj[1];
		if(!val){
			mHandler.post(new Runnable() {            
				@Override
				public void run() {
					Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();                
				}
			});
		}
		else if(messageParcelsTemp != null && messageParcelsTemp.length != 0){

			Log.e("onPostExceute", "true");
			if(message != null){
				Log.e("Total", String.valueOf(message.length));
				String path = context.getFilesDir().getPath();
				try{
					FileOutputStream fs = new FileOutputStream(path+"/"+file_name+"_updated.ser") ;
					ObjectOutputStream os = new ObjectOutputStream(fs);
					int new_length = messageParcelsTemp.length;

					for(int i=messageParcelsTemp.length - 1;i>=0;i--)
					{
						MessageParcel mp = messageParcelsTemp[i];

						// writing serialized objects
						os.writeObject(mp);
						Log.e("Writing...", "true");
					}
					os.close();
				}
				catch(NetworkOnMainThreadException e){
					Log.e("Exception1", "NetworkOnMainThread");
				} catch (FileNotFoundException e1) {
					Log.e("Exception1", "FileNotFound");
				} catch (EOFException e2) {
					Log.e("Exception1", "EOF");
				} catch (IOException e2) {
					Log.e("Exception1", "IOException");
				}
				finally{
					ArrayList<MessageParcel> messageParcelsTotal = new ArrayList<MessageParcel>();
					try {
						FileOutputStream fs = new FileOutputStream(path+"/" +file_name+".ser");
						ObjectOutputStream os = new ObjectOutputStream(fs);

						FileInputStream fs2 = new FileInputStream(path+"/"+file_name+"_updated.ser");
						ObjectInputStream os2 = new ObjectInputStream(fs2);

						MessageParcel msgPar;
						while((msgPar = (MessageParcel)os2.readObject()) != null){
							os.writeObject(msgPar);
						}
						os.close();
						os2.close();

					} catch (FileNotFoundException e) {
						Log.e("Exception2", "FileNotFound");
					} catch (EOFException e) {
						Log.e("Exception2", "EOF");
					} catch (IOException e) {
						Log.e("Exception2", "IOException");
					} catch (ClassNotFoundException e) {
						Log.e("Exception2", "ClassNotFound");
					}
					finally{
						Intent i = new Intent("com.example.webmail."+file_name);
						context.sendBroadcast(i);
					}
				}
			}
		}
	
	}

}
