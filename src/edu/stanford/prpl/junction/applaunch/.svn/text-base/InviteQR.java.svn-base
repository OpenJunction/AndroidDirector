package edu.stanford.prpl.junction.applaunch;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class InviteQR extends Activity {
	private static String SCAN_APP = "com.google.zxing.client.android";
	private static String SCAN_ACTION = "com.google.zxing.client.android.ENCODE";
	
	boolean shownCode = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String uri = null;
		
		Bundle extras = getIntent().getExtras();
		uri = extras.getString("uri");
		// TODO: do we want this?
		//if (extras.containsKey("package")) {
			//uri += "apk=" + extras.getString("package") + "&";
		//}
		
		Intent qr = new Intent(SCAN_ACTION);
		qr.putExtra("ENCODE_DATA", uri);
		qr.putExtra("ENCODE_TYPE", "TEXT_TYPE");
		// Context context, Intent intent, String packageName, String downloadRef, String appName
		IntentLauncher.launch(this, 
				  qr,
				  SCAN_APP,
				  "market://search?q=pname:com.google.zxing.client.android",
				  "Barcode Scanner");
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if (shownCode) {
			finish();
		}
		shownCode = true;
	}
	

}
