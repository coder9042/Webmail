package com.example.webmail;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver
{
	public int read(Context context){

		int top_unread = 0;
		//reading from file
		Log.e("READING", "TRUE");
		ArrayList<MessageParcel> msgPs = new ArrayList<MessageParcel>();
		try{
			String path = context.getFilesDir().getPath();
			Log.e("PATH", path);
			FileInputStream fs = new FileInputStream(path+"/inbox.ser");
			ObjectInputStream os = new ObjectInputStream(fs);
			MessageParcel mp;
			while((mp = (MessageParcel)os.readObject()) != null){
				Log.e("Reading...", "true");
				msgPs.add(mp);
				String value="";

				value += mp.getFrom();
				Log.e("SUB", mp.getSub());
				value += "///" + mp.getDate();
				value += "///" + mp.getSub();

				if(mp.getRead() == 1)
					value += "///" + "read";
				else
					value += "///" + "unread";

				value += "///" + mp.getBody();

				if(mp.getFlagged() == 1)
					value += "///" + "flagged";
				else
					value += "///" + "unflagged";
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
			if(msgPs.size() > 0){
				MessageParcel[] messageParcels = new MessageParcel[msgPs.size()];
				for(int i=msgPs.size()-1, j=0;i>=0;i--, j++){
					messageParcels[i] = msgPs.get(j);
				}
				for(int i=msgPs.size()-1;i>=0;i--){
					if(messageParcels[i].getRead() == 1){
						break;
					}
					else{
						top_unread +=1;
					}
				}
			}
		}
		return top_unread;
	
	}
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.e("IN RECEIVER", "TRUE");
		int unread = read(context);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String needNotification = prefs.getString(context.getString(R.string.pref_notification_toggle_key), context.getString(R.string.pref_notification_toggle_default));
		String needNotificationTone = prefs.getString(context.getString(R.string.pref_notification_sound_key), context.getString(R.string.pref_notification_sound_default));

		if(unread != 0){
			String notif_text;
			if(unread == 1){

				notif_text = "";
				String path = context.getFilesDir().getPath();
				try {
					FileInputStream fs = new FileInputStream(path+"/inbox.ser");
					ObjectInputStream os = new ObjectInputStream(fs);
					MessageParcel mp = (MessageParcel) os.readObject();
					notif_text = mp.getFrom() + " -- " + mp.getSub();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			else
				notif_text = unread + " new messages.";

			if(needNotification.equalsIgnoreCase("on")){
				Log.e("Notification", "true");
				Uri sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notify);

				NotificationCompat.Builder mBuilder = 
						new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.notification)
				.setContentTitle("Webmail")
				.setContentText(notif_text)
				.setAutoCancel(true)
				.setOnlyAlertOnce(true);

				if(needNotificationTone.equalsIgnoreCase("on"))
					mBuilder.setSound(sound);


				Intent resultIntent = new Intent(context, MainActivity.class);
				resultIntent.putExtra("NOTIF", "NOTIF");
				TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
				stackBuilder.addNextIntent(resultIntent);

				PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

				mBuilder.setContentIntent(resultPendingIntent);

				NotificationManager mNotificationManager =
						(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

				mNotificationManager.notify(3004, mBuilder.build());
			}

		}
	}
}
