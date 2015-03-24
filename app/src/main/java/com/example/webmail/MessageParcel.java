package com.example.webmail;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Flags.Flag;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class MessageParcel implements Parcelable, Serializable{
	private String from;
	private String to;
	private String cc;
	private String bcc;
	private String date;
	private String subject;
	private String body;
	private int read;
	private int flagged;
	private ArrayList<String> attachments = new ArrayList<String>();
	private ArrayList<String> sizes = new ArrayList<String>();
	public boolean equals(MessageParcel msg){
		if(msg.from.equals(this.from)){
			if(msg.to.equals(this.to)){
				if(msg.cc.equals(this.cc)){
					if(msg.bcc.equals(this.bcc)){
						if(msg.date.equals(this.date)){
							if(msg.subject.equals(this.subject)){
								if(msg.body.equals(this.body)){
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
	public MessageParcel(Message msg){
		read = 0;
		flagged = 0;
		try{
			// adding from
			String value = "";
			Address[] in = msg.getFrom();
			for (Address address : in) {
				value += address.toString();
			}
			this.from = value;
			
			// adding date
			this.date = msg.getSentDate().toString();
			Log.e("DATE", date);
			String date_parts[] = this.date.split(" ");
			String dt = date_parts[0] + "\n" + date_parts[1]+" "+date_parts[2] + "\n" +date_parts[3].substring(0, date_parts[3].lastIndexOf(":"));
			this.date = dt;
			
			//adding subject
			String sub = msg.getSubject();
			if(sub != null && !sub.equals(""))
				this.subject = sub;
			else
				this.subject = "no subject";
			
			Log.e("SUBJECT", this.subject);
			//adding body
			this.body = " ";
			Object content = msg.getContent();
			if(content instanceof Multipart){
				Multipart mp = (Multipart) content;
				for(int i=0; i<mp.getCount();i++){
					Part part = mp.getBodyPart(i);
					//Log.e("MIME-TYPE" + String.valueOf(i), part.getContentType());
					if((part.getFileName() == null || part.getFileName() == "") && ((part.isMimeType("text/plain") || part.isMimeType("multipart/ALTERNATIVE")))){
						try{
							content = part.getContent();
						}
						catch(Exception e){
							content = "";
						}
						Log.e("PLAIN/TEXT", "true");
					}
					else if(part.getFileName() != null || part.getFileName() != ""){
						if(part.getDisposition() != null && part.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)){
							MimeBodyPart mbp = (MimeBodyPart)part;
							attachments.add(part.getFileName());
							sizes.add(String.valueOf(part.getSize()));
							Log.e("Attachment Found", "true");
						}
					}
				}
				if(content != null){
					if(content instanceof Multipart){
						for(int i=0;i<((Multipart)content).getCount();i++){
							Part part = ((Multipart)content).getBodyPart(i);
							if(part.isMimeType("text/plain")){
								content = part.getContent();
								Log.e("PLAIN/TEXT 2", "true");
								break;
							}
						}
					}
					this.body = content.toString();
					
				}
			}
			else if(content instanceof String){
				this.body = content.toString();
			}
			
			if(this.body == null || this.body.equals("")){
				this.body = " ";
			}
			
			// adding recipients
			
			value = "";
			String to="", cc="", bcc="";
			in = msg.getRecipients(RecipientType.TO);
			if(in == null){
				to = "";
				Log.e("in NOT NULL", "true");
			}
			else{
				Log.e("in NOT NULL", "true");
				for (Address address : in) {
					value += " " + address.toString();
				}
				to = value;
			}
			
			value = "";
			in = msg.getRecipients(RecipientType.CC);
			if(in == null){
				cc = "";
			}
			else{
				for (Address address : in) {
					value += " " + address.toString();
				}
				cc = value;
			}
			
			value = "";
			in = msg.getRecipients(RecipientType.BCC);
			if(in == null)
				bcc = "";
			else{
				for (Address address : in) {
					value += " " + address.toString();
				}
				bcc = value;
			}
			
			this.to = to;
			this.cc = cc;
			this.bcc = bcc;
			
			if(this.to == null || this.to.equals("")){
				this.to = " ";
			}
			if(this.cc == null || this.cc.equals("")){
				this.cc = " ";
			}
			if(this.bcc == null || this.bcc.equals("")){
				this.bcc = " ";
			}
			
			// adding read/unread status
			boolean read_check = msg.isSet(Flag.SEEN);
			if(read_check)
				this.read = 1;
			else
				this.read = 0;
			
			// adding flagged/unflagged status
			boolean flag_check = msg.isSet(Flag.FLAGGED);
			if(flag_check)
				this.flagged = 1;
			else
				this.flagged = 0;
		}
		catch(MessagingException e){
			Log.e("MESSAGINGEXCEPTION", e.getStackTrace().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.e("FROM", this.from);
		Log.e("TO", this.to);
	}
	public void setRead(int read){
		this.read = read;
	}
	public void setFlag(int f){
		this.flagged = f;
	}
	public void setRecipients(String arg[]){
		this.to = arg[0];
		this.cc = arg[1];
		this.bcc = arg[2];
	}
	public MessageParcel(Parcel in) {
		this.from = in.readString();
		this.to = in.readString();
		this.cc = in.readString();
		this.bcc = in.readString();
		this.date = in.readString();
		this.subject = in.readString();
		this.body = in.readString();
		this.read = in.readInt();
		this.flagged = in.readInt();
		in.readStringList(this.attachments);
		in.readStringList(this.sizes);
	}
	public String getFrom(){
		return this.from;
	}
	public String getSub(){
		return this.subject;
	}
	public String getDate(){
		return this.date;
	}
	public String getBody(){
		return this.body;
	}
	public int getRead(){
		return this.read;
	}
	public int getFlagged(){
		return this.flagged;
	}
	public String getTo(){
		return this.to;
	}
	public String getCc(){
		return this.cc;
	}
	public String getBcc(){
		return this.bcc;
	}
	
	public ArrayList<String> getAttachmentNames(){
		return this.attachments;
	}
	
	public ArrayList<String> getAttachmentSizes(){
		return this.sizes;
	}
	
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(from);
		dest.writeString(to);
		dest.writeString(cc);
		dest.writeString(bcc);
		dest.writeString(date);
		dest.writeString(subject);
		dest.writeString(body);
		dest.writeInt(read);
		dest.writeInt(flagged);
		dest.writeStringList(attachments);
		dest.writeStringList(sizes);
	}
	
	public static final Parcelable.Creator<MessageParcel> CREATOR = new Parcelable.Creator<MessageParcel>() {
        public MessageParcel createFromParcel(Parcel in) {
            return new MessageParcel(in);
        }

        public MessageParcel[] newArray(int size) {
            return new MessageParcel[size];
        }
    };
}
