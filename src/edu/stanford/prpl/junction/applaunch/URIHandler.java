package edu.stanford.prpl.junction.applaunch;

import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class URIHandler extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		NotificationManager mgr = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE); 
		mgr.cancel(SMSReceiver.JUNCTION_NOTIFY_ID);
		
		
		Log.d("junction","got URI: " + getIntent().getData());
		//Toast.makeText(getApplicationContext(), "URI: " + getIntent().getData().toString(), Toast.LENGTH_LONG).show();
		try {
			URI uri = new URI(getIntent().getData().toString().trim());
			ActivityDirector.createJunction(this, uri);
			
			finish();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
