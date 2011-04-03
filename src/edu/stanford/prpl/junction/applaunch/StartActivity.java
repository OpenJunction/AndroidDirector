package edu.stanford.prpl.junction.applaunch;

import java.net.URI;
import java.util.List;
import java.util.Map;

import edu.stanford.junction.SwitchboardConfig;
import edu.stanford.junction.android.AndroidJunctionMaker;
import edu.stanford.junction.provider.bluetooth.BluetoothSwitchboardConfig;
import edu.stanford.junction.provider.jx.JXSwitchboardConfig;
import edu.stanford.junction.provider.xmpp.XMPPSwitchboardConfig;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
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
	private static final int REQUEST_USE_HUB = 1;
	private static final int REQUEST_ENABLE_BT_DIRECT = 2;
	private static final int REQUEST_ENABLE_BT_DISCO = 3;
	private List<Map<String,?>>  mAppList;
	private int mAppIndex  = -1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// connectivity options
		setContentView(R.layout.choose_carrier);
		findViewById(R.id.button_xmpp).setOnClickListener(mXmppConnector);
		findViewById(R.id.button_lan).setOnClickListener(mLanConnector);
		findViewById(R.id.button_bluetooth_hub).setOnClickListener(mQRConnector);
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
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				
				public void onCancel(DialogInterface arg0) {
					finish();
					return;
				}
			})
			.create().show();
	}
	
	private View.OnClickListener mXmppConnector = new View.OnClickListener() {
		public void onClick(View arg0) {
			if (mAppIndex == -1) return;
			String name = (String)mAppList.get(mAppIndex).get("name");
			ResolveInfo info = (ResolveInfo) mAppList.get(mAppIndex).get("resolveInfo");
			Intent launch = new Intent(AndroidJunctionMaker.Intents.ACTION_JOIN);
			launch.setPackage(info.activityInfo.packageName);
			
			SwitchboardConfig config = new XMPPSwitchboardConfig("sb.openjunction.org");
			AndroidJunctionMaker jm = AndroidJunctionMaker.getInstance(config);
			URI invitation = jm.generateSessionUri();
			startJunctionActivity(name, info, invitation);
		}
	};
	
	private View.OnClickListener mLanConnector = new View.OnClickListener() {
		public void onClick(View arg0) {
			if (mAppIndex == -1) return;
			String name = (String)mAppList.get(mAppIndex).get("name");
			ResolveInfo info = (ResolveInfo) mAppList.get(mAppIndex).get("resolveInfo");
			Intent launch = new Intent(AndroidJunctionMaker.Intents.ACTION_JOIN);
			launch.setPackage(info.activityInfo.packageName);
			
			SwitchboardConfig config = new JXSwitchboardConfig();
			AndroidJunctionMaker jm = AndroidJunctionMaker.getInstance(config);
			URI invitation = jm.generateSessionUri();
			startJunctionActivity(name, info, invitation);
		}
	};
	
	/**
	 * Bluetooth connectivity with this device as a hub.
	 * Prompts the user to put the phone in discoverable
	 * mode, which is optional.
	 */
	private View.OnClickListener mBluetoothConnector = new View.OnClickListener() {
		public void onClick(View arg0) {
			if (mAppIndex == -1) return;
			
			if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
	            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableIntent, REQUEST_ENABLE_BT_DIRECT);
	            return;
	        }
			
			doBtDisco();
		}
	};
	
	/**
	 * The Bluetooth hub connectivity is currently bootstrapped with a QR code.
	 * This code is experimental.
	 */
	private View.OnClickListener mQRConnector = new View.OnClickListener() {
		public void onClick(View arg0) {
			if (mAppIndex == -1) return;

			startQRScan();
		}
	};
	
	private void doBtDisco() {
		 Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	     startActivityForResult(i, REQUEST_ENABLE_BT_DISCO);
	}
	
	private void doBtHub() {
		String name = (String)mAppList.get(mAppIndex).get("name");
		ResolveInfo info = (ResolveInfo) mAppList.get(mAppIndex).get("resolveInfo");
		Intent launch = new Intent(AndroidJunctionMaker.Intents.ACTION_JOIN);
		launch.setPackage(info.activityInfo.packageName);
		
		BluetoothSwitchboardConfig config = new BluetoothSwitchboardConfig();
		AndroidJunctionMaker jm = AndroidJunctionMaker.getInstance(config);
		URI invitation = jm.generateSessionUri(); // switchboard set to this device's mac.		
		startJunctionActivity(name, info, invitation);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		if (requestCode == REQUEST_USE_HUB) {
			if (resultCode == Activity.RESULT_OK) {
				startQRScan();
			} else {
				
			}
			return;
		}

		if (requestCode == REQUEST_ENABLE_BT_DIRECT) {
			if (resultCode == Activity.RESULT_OK) {
				doBtDisco();
			} else {
				
			}
			return;
		}
		
		if (requestCode == REQUEST_ENABLE_BT_DISCO) {
			if (resultCode > 0) {
				Toast.makeText(StartActivity.this,
						"Discoverable for " + resultCode + " seconds.", Toast.LENGTH_SHORT).show();
			} else {
				
			}
			
			doBtHub();
			return;
		}
		
		if (intent != null && ActivityScan.SCAN_ACTION.equals(intent.getAction())) {
			if (resultCode == Activity.RESULT_OK) {
		        String contents = intent.getStringExtra("SCAN_RESULT");
		        try {
		        	String name = (String)mAppList.get(mAppIndex).get("name");
		        	ResolveInfo info = (ResolveInfo) mAppList.get(mAppIndex).get("resolveInfo");
					URI invitation = URI.create(contents);
					startJunctionActivity(name, info, invitation);
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
	
	private static int nCount = 0;
	private void startJunctionActivity(String name, ResolveInfo info, URI invitation) {
		/*
		// Notification to interact with activity
		Intent intentForInvitation = new Intent(this, Invitation.class);
		intentForInvitation.putExtra("invitationURI", invitation.toString());
		PendingIntent pending = PendingIntent.getActivity(this, -1, intentForInvitation, Intent.FLAG_ACTIVITY_NEW_TASK);
		long when = System.currentTimeMillis();
		Notification notification = new Notification(android.R.drawable.ic_menu_share, "Junction session", when);
		notification.setLatestEventInfo(this, name + " session", "Invite others to join", pending);
		
		NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE); 
		nm.notify(nCount++, notification);
		*/
		
		Intent intentForActivity = AndroidJunctionMaker.getIntentForActivityJoin(invitation);
		intentForActivity.addCategory(Intents.CATEGORY_BOOTSTRAP);
		intentForActivity.setClassName(info.activityInfo.packageName, info.activityInfo.name);
		intentForActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		startActivity(intentForActivity);
		finish();
	}
}
