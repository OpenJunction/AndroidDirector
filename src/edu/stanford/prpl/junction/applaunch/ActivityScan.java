package edu.stanford.prpl.junction.applaunch;

import java.net.URI;

import edu.stanford.junction.android.WaitForInternet;
import edu.stanford.junction.android.WaitForInternetCallback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ActivityScan extends Activity {
	protected static String SCAN_APP = "com.google.zxing.client.android";
	protected static String SCAN_ACTION = "com.google.zxing.client.android.SCAN";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.blank);

		WaitForInternetCallback callback = 
			new WaitForInternetCallback(this) {
				public void onConnectionSuccess() {
					Intent intent = new Intent(SCAN_ACTION);
			        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
					IntentLauncher.launch(ActivityScan.this, 
							  intent,
							  SCAN_APP,
							  "Barcode Scanner");
					//ActivityScan.this.finish();
				}
				
				public void onConnectionFailure() {
					ActivityScan.this.finish();
					return;
				}
			};
			
		try {
			WaitForInternet.setCallback(callback);
		} catch (SecurityException e) {
			Log.w("junction","Could not check network state.", e);
			callback.onConnectionSuccess();
		}
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (intent != null && SCAN_ACTION.equals(intent.getAction())) {
			if (resultCode == Activity.RESULT_OK) {
		        String contents = intent.getStringExtra("SCAN_RESULT");
		        //String formatName = intent.getStringExtra("SCAN_RESULT_FORMAT");
		        
		        
		        try {
		        	URI act = new URI(contents);
		        	Log.d("junction","creating junction from uri " + act);
		        	
		        	// is it bootstrap activity?
		        	/*ActivityDescription desc = JunctionMaker.getInstance().getActivityDescription(act);
		        	if ("junction.launch".equals(desc.getActivityID())) {
		        		
		        	}*/
		        	
		        	
		        	ActivityDirector.createJunction(this, act);
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
