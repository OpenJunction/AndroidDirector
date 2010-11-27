package edu.stanford.prpl.junction.applaunch;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.stanford.junction.JunctionMaker;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Invite an actor to join an activity by scanning
 * the URI of a listening service.
 * 
 * @author bdodson
 *
 */
public class InviteScan extends Activity {
	private static String SCAN_APP = "com.google.zxing.client.android";
	private static String SCAN_ACTION = "com.google.zxing.client.android.SCAN";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = new Intent(SCAN_ACTION);
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		IntentLauncher.launch(this, 
				  intent,
				  SCAN_APP,
				  "Barcode Scanner");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (intent != null && SCAN_ACTION.equals(intent.getAction())) {
			if (resultCode == Activity.RESULT_OK) {
		        String contents = intent.getStringExtra("SCAN_RESULT");
		        //String formatName = intent.getStringExtra("SCAN_RESULT_FORMAT");
		        
		        
		        try {
		        	URI act = new URI(contents);
		        	Log.d("junction","sending invitation to service at uri " + act);
		        	
		        	URI uri = new URI(getIntent().getExtras().getString("uri"));
		        	//JunctionMaker.getInstance().inviteActorByListenerService(uri, act);
		        	// TODO: implement castActor() in JAVA.
		        	
		        	finish();
		        	return;
		        } catch (Exception m) {
		        	Log.d("junction", "not a url ",m);
		        }
		        
			}
		}
		// TODO: show another screen on error?
		finish();
	}

}