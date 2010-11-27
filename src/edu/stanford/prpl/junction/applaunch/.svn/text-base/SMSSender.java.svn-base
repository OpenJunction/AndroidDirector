package edu.stanford.prpl.junction.applaunch;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SMSSender extends Activity {
	public static final int	PICK_CONTACT	= 1;
	private Button			btnContacts;
	private Button 			btnInvite;
	private TextView		txtContacts;
	private String msg;
	
	private List<String> phoneNumbers = new ArrayList<String>();

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts);
		if(getIntent().hasExtra("invitation")) {
			msg = getIntent().getExtras().getString("invitation");
		} else {
			Log.w("junction", "No invitation URI found");
			finish();
			return;
		}
		
		if (getIntent().hasExtra("phoneNumber")) {
			String number = getIntent().getStringExtra("phoneNumber");
			
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage(number, null, msg, null, null);  
			finish();
			return;
		}
		
		Log.d("junction", "GOT INVITATION: " + msg);
		btnContacts = (Button) findViewById(R.id.btn_contacts);
		btnInvite = (Button) findViewById(R.id.btn_invite);
		txtContacts = (TextView) findViewById(R.id.txt_contacts);

		btnContacts.setOnClickListener(new OnClickListener() {
			
	
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_PICK, People.CONTENT_URI);
				startActivityForResult(intent, PICK_CONTACT);
			}
		});
		
		btnInvite.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				sendInvites();
			}
		});
	}

	public void sendInvites() {
		for(int i=0; i<phoneNumbers.size(); i++) {
			String number = phoneNumbers.get(i);
			PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, SMSSender.class), 0);                
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage(number, null, msg, null, null);  
		}
		phoneNumbers.clear();
		txtContacts.setText("");
		Toast.makeText(getApplicationContext(), "Invitations sent!", Toast.LENGTH_LONG).show();
    	
		
	}
	
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
			case (PICK_CONTACT):
				if (resultCode == Activity.RESULT_OK) {
					Uri contactData = data.getData();
					Cursor c = managedQuery(contactData, null, null, null, null);
					if (c.moveToFirst()) {
						String name = c.getString(c.getColumnIndexOrThrow(People.NAME));
						String phoneNumber = c.getString(c.getColumnIndexOrThrow(People.NUMBER));
						if (phoneNumber == null) {
							Toast.makeText(this, "Could not add contact", Toast.LENGTH_SHORT).show();
							return;
						}
						txtContacts.append(name + "\n");
						phoneNumbers.add(phoneNumber);
					}
				}
				break;
		}
	}
}