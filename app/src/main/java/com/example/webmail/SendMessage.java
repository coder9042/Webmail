package com.example.webmail;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class SendMessage extends IntentService {

	static String smtp_address;
	static String smtp_port;
	static String username;
	static String password;

	static Handler mHandler;


	public SendMessage() {
		super("SendMessage");
		mHandler = new Handler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.e("COMPOSEMSG", "STARTED");
		mHandler.post(new Runnable() {            
			@Override
			public void run() {
				Toast.makeText(SendMessage.this, "Sending Message", Toast.LENGTH_SHORT).show();                
			}
		});

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		//smtp_address = "smtp.gmail.com";
		smtp_address = "172.16.1.11";
		smtp_port = "25";
		username = prefs.getString(getString(R.string.pref_username_key), getString(R.string.pref_username_default));
		password = prefs.getString(getString(R.string.pref_password_key), getString(R.string.pref_password_default));

		String[] values = intent.getStringArrayExtra("MESSAGE_DETAILS");
		String subject = values[0];
		String to = values[1];
		String cc = values[2];
		String bcc = values[3];
		String body = values[4];

		ArrayList<String> path = intent.getStringArrayListExtra("ATTACHMENT_DETAILS");
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		//props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", smtp_address);
		//props.put("mail.smtp.ssl.enable", "true");
		props.put("mail.smtp.port", smtp_port);

		// Get the Session object.
		Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		Log.e("SENTHALF", "TRUE");
		String error = null;
		Message message = null;
		try {
			Log.e("reach", "1");
			message = new MimeMessage(session);
			message.setSentDate(new Date());
			Log.e("reach", "2");
			
			message.setFrom(new InternetAddress(username+"@iitp.ac.in"));
			Log.e("reach", "3");
			Log.e("to", to);
			Log.e("cc", cc);
			
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
			Log.e("Reached", "4");
			if(!(cc==null) || !(cc=="") )
			{
				message.setRecipients(Message.RecipientType.CC,
						InternetAddress.parse(cc));
			}
			//Log.e("Reached", "5");
			if(!(bcc==null) || !(bcc=="") )
			{	
				message.setRecipients(Message.RecipientType.BCC,
						InternetAddress.parse(bcc));
			}
			Log.e("Reached", "6");
			message.setSubject(subject);
			//message.setText(body);

			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(body);
			Log.e("BODY", body);
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			if(!(path==null))
			{
				for(String p: path){
					File f = new File(p);
					String fileName = f.getName();

					String file = p;
					Log.e("path", file);
					DataSource source = new FileDataSource(file);

					messageBodyPart = new MimeBodyPart();
					messageBodyPart.setDataHandler(new DataHandler(source));
					messageBodyPart.setFileName(fileName);
					multipart.addBodyPart(messageBodyPart);
				}
			}
			message.setContent(multipart);

			Log.e("sending", "true");

			try{
				Intent dialogIntent = new Intent(this, MainActivity.class);
				dialogIntent.setAction(Intent.ACTION_VIEW);
				dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				dialogIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				this.startActivity(dialogIntent);
				Transport.send(message);
				Log.e("sent", "true");
				path=null;
				
				Properties props2 = new Properties();
				props2.setProperty("mail.store.protocol", "imap");
				Session sent_session = Session.getInstance(props2, null);
				Store store = session.getStore("imap");
				String imap_address = "172.16.1.11";
				int imap_port = 143;
				store.connect(imap_address, imap_port, username, password);
				Folder sent = store.getFolder("INBOX.Sent");
				sent.open(Folder.READ_WRITE);
				sent.appendMessages(new Message[]{message});
			}
			catch(MessagingException e){
				path=null;
				Log.e("ERROR SENDING", e.getMessage());
				error = "Sending Failed";
			}
		} catch (SendFailedException e) {
			path=null;
			error = "Sending Failed";
		} catch (MessagingException e) {
			path=null;
			error = "Sending Failed";
		}

		final String err = error;
		path=null;
		if(error != null){
			mHandler.post(new Runnable() {            
				@Override
				public void run() {
					Toast.makeText(SendMessage.this, err, Toast.LENGTH_SHORT).show();                
				}
			});
		}
		else{
			mHandler.post(new Runnable() {            
				@Override
				public void run() {
					Toast.makeText(SendMessage.this, "Message Sent Succesfully", Toast.LENGTH_SHORT).show();                
				}
			});
		}
	}

}
