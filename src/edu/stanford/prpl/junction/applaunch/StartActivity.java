package edu.stanford.prpl.junction.applaunch;

import java.net.URI;
import java.util.List;
import java.util.Map;

import edu.stanford.junction.android.AndroidJunctionMaker;
import edu.stanford.junction.provider.bluetooth.BluetoothSwitchboardConfig;
import edu.stanford.junction.provider.xmpp.XMPPSwitchboardConfig;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

/**
 * Start a new Junction activity
 *
 */
public class StartActivity extends Activity {
	private static final int REQUEST_ENABLE_BT = 1;
	private List<Map<String,?>>  mAppList;
	private int mAppIndex  = -1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// connectivity options
		setContentView(R.layout.choose_carrier);
		findViewById(R.id.button_xmpp).setOnClickListener(mXmppConnector);
		findViewById(R.id.button_bluetooth).setOnClickListener(mBluetoothConnector);
		
		
		// Prompt for activity to launch
		mAppList = ActivityBootstrap.getAvailableActivities(this);
		
		final List<Map<String, ?>> available = mAppList;
		if (available.size() == 0) {
			Toast.makeText(this, "No p2p applications found.", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
		final ListAdapter adapter 
		    = new SimpleAdapter(this, available, R.layout.bootstrap_item,
		    		new String[] { "name", "icon" },
		    		new int[] { R.id.bsi_name, R.id.bsi_icon } );

		new AlertDialog.Builder(this)
			.setAdapter(adapter, new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface iface, int pos) {
					mAppIndex = pos;
				}
			})
			.setTitle("Start P2P session")
			.create().show();
	}
	
	private View.OnClickListener mXmppConnector = new View.OnClickListener() {
		public void onClick(View arg0) {
			if (mAppIndex == -1) return;
			
			ResolveInfo info = (ResolveInfo) mAppList.get(mAppIndex).get("resolveInfo");
			Intent launch = new Intent(AndroidJunctionMaker.Intents.ACTION_JOIN);
			launch.setPackage(info.activityInfo.packageName);
			
			XMPPSwitchboardConfig config = new XMPPSwitchboardConfig("sb.openjunction.org");
			AndroidJunctionMaker jm = AndroidJunctionMaker.getInstance(config);
			URI invitation = jm.generateSessionUri();
			
			Intent intentForActivity = AndroidJunctionMaker.getIntentForActivityJoin(invitation);
			intentForActivity.addCategory(Intents.CATEGORY_BOOTSTRAP);
			intentForActivity.setClassName(info.activityInfo.packageName, info.activityInfo.name);
			startActivity(intentForActivity);
			finish();
		}
	};
	
	
	/**
	 * The Bluetooth connectivity is currently bootstrapped with a QR code.
	 * This code is experimental.
	 */
	private View.OnClickListener mBluetoothConnector = new View.OnClickListener() {
		public void onClick(View arg0) {
			if (mAppIndex == -1) return;
			
			if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
	            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
	            return;
	        }
			
			startQRScan();
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_OK) {
				startQRScan();
			} else {
				
			}
			return;
		}

		if (intent != null && ActivityScan.SCAN_ACTION.equals(intent.getAction())) {
			if (resultCode == Activity.RESULT_OK) {
		        String contents = intent.getStringExtra("SCAN_RESULT");
		        try {
		        	
		        	ResolveInfo info = (ResolveInfo) mAppList.get(mAppIndex).get("resolveInfo");
					URI invitation = URI.create(contents);
					Intent intentForActivity = AndroidJunctionMaker.getIntentForActivityJoin(invitation);
					intentForActivity.addCategory(Intents.CATEGORY_BOOTSTRAP);
					intentForActivity.setClassName(info.activityInfo.packageName, info.activityInfo.name);
					startActivity(intentForActivity);
					finish();

		        } catch (Exception m) {
		        	Log.d("junction", "not a url ",m);
		        }
			}
		}
		// TODO: show another screen on error?
		finish();
	}
	
	private void startQRScan() {
		Intent intent = new Intent(ActivityScan.SCAN_ACTION);
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		IntentLauncher.launch(StartActivity.this, 
				  intent,
				  ActivityScan.SCAN_APP,
				  "Barcode Scanner");
	}
}
