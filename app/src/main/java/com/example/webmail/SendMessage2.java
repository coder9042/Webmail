package com.example.webmail;


import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class SendMessage2 extends IntentService {

	static String smtp_address;
	static String smtp_port;
	static String username;
	static String password;
	
	static Handler mHandler;

	public SendMessage2() {
		super("SendMessage2");
		mHandler = new Handler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.e("COMPOSEMSG", "STARTED");
		mHandler.post(new Runnable() {            
	        @Override
	        public void run() {
	            Toast.makeText(SendMessage2.this, "Sending Message", Toast.LENGTH_SHORT).show();                
	        }
	    });

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		smtp_address = "smtp.gmail.com";
		smtp_port = "0";
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
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", smtp_address);
		if(smtp_port.compareTo("0") == 0)
			smtp_port = "587";
		props.put("mail.smtp.port", smtp_port);

		// Get the Session object.
		Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		Log.e("SENTHALF", "TRUE");
		Message message = null;
		String error = null;
		try {
			// Create a default MimeMessage object.
			message = new MimeMessage(session);

			// Set To: header field of the header.
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
			
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

			message.setSubject(subject);
			//message.setText(body);

			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(body);
			Log.e("BODY", body);
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			// Send message
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
			//System.out.println("Sent message successfully....");
			
			try{
				Intent dialogIntent = new Intent(this, MainActivity.class);
				dialogIntent.setAction(Intent.ACTION_VIEW);
				dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				dialogIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				this.startActivity(dialogIntent);
				Transport.send(message);
				Log.e("sent", "true");
				path=null;
			}
			catch(Exception e)
			{
				path=null;
				Log.e("error", e.toString());
				error = "Sending Failed";
			}

		} catch (SendFailedException e) {
			error = "Sending failed";
		} catch (MessagingException e) {
			error = "Sending failed";
		}
		
		final String err = error;
		if(error != null){
			mHandler.post(new Runnable() {            
		        @Override
		        public void run() {
		            Toast.makeText(SendMessage2.this, err, Toast.LENGTH_SHORT).show();                
		        }
		    });
		}
		else{
			mHandler.post(new Runnable() {            
		        @Override
		        public void run() {
		            Toast.makeText(SendMessage2.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();                
		        }
		    });
		}
	}

}
