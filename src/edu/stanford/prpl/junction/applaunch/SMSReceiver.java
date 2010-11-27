package edu.stanford.prpl.junction.applaunch;

import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * The class is called when SMS is received.
 */
public class SMSReceiver extends BroadcastReceiver {
	public static final int JUNCTION_NOTIFY_ID = 989898;
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Bundle bundle = intent.getExtras();
	
			Object messages[] = (Object[]) bundle.get("pdus");
			SmsMessage smsMessage[] = new SmsMessage[messages.length];
			for (int n = 0; n < messages.length; n++) {
				smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
			}
			String msg = smsMessage[0].getMessageBody();
			
			if(msg.length() > 11 && msg.substring(0, 11).equals("junction://")) {
				Log.d("junction", "Found Junction URI in SMS");
				String URI = msg;
				Intent intent2 = new Intent("android.intent.action.VIEW");
				intent2.setData(Uri.parse(URI));
				
				
				NotificationManager mgr = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE); 
				
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent2, 0);
				Notification notification = new Notification(R.drawable.jicon,
		                	"New Junction Invite", System.currentTimeMillis());
		        	notification.setLatestEventInfo(context,
		                	"New Junction Invite","You've been invited to join a Junction activity!",
			                contentIntent);
			        mgr.notify(JUNCTION_NOTIFY_ID, notification);
	
			}
		} catch (Exception e) {
			Log.w("junction","Error handling SMS",e);
		}
	}

	
}