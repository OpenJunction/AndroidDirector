package edu.stanford.prpl.junction.applaunch;

import java.net.URI;
import java.util.Iterator;

import mobisocial.nfc.Nfc;
import mobisocial.nfc.Nfc.NdefHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.stanford.junction.JunctionException;
import edu.stanford.junction.JunctionMaker;
import edu.stanford.junction.SwitchboardConfig;
import edu.stanford.junction.android.AndroidJunctionMaker;
import edu.stanford.junction.api.activity.ActivityScript;
import edu.stanford.junction.provider.xmpp.XMPPSwitchboardConfig;
import edu.stanford.junction.remoteintents.RemoteIntentManager;


import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This is the main user-facing Director application.
 * It also starts background listeners, such as the
 * RemoteIntent listener.
 * 
 * @author bjdodson
 *
 */
public class ActivityDirector extends Activity {
	private Nfc mNfc;
	public static final String TYPE_APPMANIFEST = "application/vnd.mobisocial-appmanifest";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mNfc = new Nfc(this);
		mNfc.addNdefHandler(mNdefHandler);

		Intent rimIntent = new Intent(this,RemoteIntentManager.class);
		startService(rimIntent);
		
		setContentView(R.layout.join);
		
		findViewById(R.id.button_start).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View arg0) {
        		Intent intent = new Intent();
        		intent.setClass(ActivityDirector.this, StartActivity.class);
        		startActivity(intent);
        	}
		});
		
		findViewById(R.id.button_scan).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View arg0) {
        		Intent intent = new Intent("junction.intent.action.SCAN");
        		intent.setClassName(ActivityDirector.this.getPackageName(), "edu.stanford.prpl.junction.applaunch.ActivityScan");
        		startActivity(intent);
        	}
		});
		
		/*
		findViewById(R.id.button_history).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View arg0) {
        		Intent intent = new Intent("junction.intent.action.join.RECENT");
        		// TODO: check for package in getIntent().getExtras()
        		// write activity for HISTORY intent
        		intent.setClassName(ActivityDirector.this.getPackageName(), "edu.stanford.prpl.junction.applaunch.ActivityScan");
        		// startActivity(intent);
        	}
		});
		*/
	
		if (android.os.Build.VERSION.SDK_INT >= BluetoothWrapper.REQUIRED_SDK
				&& BluetoothWrapper.getInstance() != null
				&& BluetoothWrapper.getInstance().getAdapter() != null) {
			
			
				findViewById(R.id.button_local).setOnClickListener(new View.OnClickListener() {
		        	public void onClick(View arg0) {
		        		Intent intent = new Intent("junction.intent.action.join.LOCAL");
		        		// TODO: check for package in getIntent().getExtras()
		        		intent.setClassName(ActivityDirector.this.getPackageName(), "edu.stanford.prpl.junction.applaunch.JoinNearby");
		        		startActivity(intent);
		        	}
				});
			
		} else {
			findViewById(R.id.button_local).setVisibility(View.GONE);
		}
		
		
		findViewById(R.id.button_env).setOnClickListener(mEnvClickListener);
	}
	
	
	
	
	private View.OnClickListener mEnvClickListener = new View.OnClickListener() {
    	public void onClick(View arg0) {
    		final EditText input = new EditText(ActivityDirector.this);  
    		AlertDialog alert = new AlertDialog.Builder(ActivityDirector.this)
				.setTitle("Set your environment")
				.setMessage("Enter your environment tag")
				.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						String tag = input.getText().toString();
						Intent holla = new Intent("profile.tag.LOAD");
						holla.putExtra("tag", tag);
						sendBroadcast(holla);
						Log.d("junction","set environment tag to " + tag);
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						
					}
				})
				.create();
    		
    		
    		alert.setView(input); 
    		alert.show();
    		
    		
    	}
    };
	
	
	
	
	
	
	protected static void createJunction(Activity parentActivity, URI uri) {
		// URI: junction://my.switchboard.com/sessionID?role=user

		String openerPackage = parentActivity.getIntent().getStringExtra("package");
		// TODO: reconcile differences between package from intent, from URI, and in activityDesc
		
		if (openerPackage != null) {
			/*
    		Intent launchIntent = new Intent("junction.intent.action.JOIN");
			launchIntent.putExtra("junctionVersion", 1);
			launchIntent.putExtra("activityDescriptor", json.toString());
			launchIntent.putExtra("invitationURI", uri.toString());
			*/
			
			Intent launchIntent = AndroidJunctionMaker.getIntentForActivityJoin(uri);
			launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (IntentLauncher.launch(parentActivity,
					launchIntent,
					openerPackage,
					  null,
					  openerPackage
					  )) {
				parentActivity.finish();
			}
			
			return;
    	}
		
		// look up activity information from Junction
		SwitchboardConfig cfg = AndroidJunctionMaker.getDefaultSwitchboardConfig(uri);
		ActivityScript ad = null;
		try {
			ad = AndroidJunctionMaker.getInstance(cfg).getActivityScript(uri);
			if (ad == null) {
				Log.w("junction","Could not retrieve activity script");
				Toast.makeText(parentActivity, "Failed to retrieve activity script.", Toast.LENGTH_SHORT).show();
				parentActivity.finish();
				return;
			}
			Log.d("junction","Trying to launch app for " + ad.getActivityID());
		} catch (JunctionException e) {
			Toast.makeText(parentActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
			Log.e("junction","Failed to get activity script.");
			return;
		}
		
		
		if (JunctionMaker.DIRECTOR_ACTIVITY.equals(ad.getActivityID())) {
			CastingDirector.sLastEnvironment = uri;
			
			Toast.makeText(parentActivity, "Set your environment.", Toast.LENGTH_SHORT).show();
			parentActivity.finish();
			return;
		}

		Log.d("junction","sending join notification for " + uri);
		Intent joinNotification = new Intent("junction.notify.JOINED_ACTIVITY");
		joinNotification.putExtra("activityID",ad.getActivityID());
		joinNotification.putExtra("activityURI",uri.toString());
		parentActivity.sendBroadcast(joinNotification);
		
		
		// This is a pretty gross hack for now, but I didn't
		// want to have to rewrite the function below.
		// Session info should be kept separate from
		// activity description info.
		JSONObject gross = ad.getJSON();
		try {
			gross.put("invitationURI", uri.toString());
			String role = null;
			if (uri.getQuery()!=null && uri.getQuery().contains("role=")) {
				role = uri.getQuery().substring(uri.getQuery().indexOf("role=")+5);
				if (role.contains("&")) {
					role = role.substring(0,role.indexOf("&"));
				}
				gross.put("role",role);
			}
		} catch (JSONException e) {
			
		}
		createJunction(parentActivity,gross);
	}
	
	
	/**
	 * Note that this function shows some legacy support that should be fixed.
	 * Namely, the activityDescriptor field is populated from the JSON
	 * which has session/switchboard information. The naming and mixing of
	 * fields is in poor shape.
	 * 
	 * BJD 8/24/09
	 */
	protected static void createJunction(Activity parentActivity, JSONObject invitation) {
		try {
	    	//Log.d("junction","json is: " + invitation.toString());
			// TODO: Add / replace this method with createJunction(Activity p, URI uri)
			// 
			
			// The invitation has:
			// switchboard <required>
			// sessionID <required>
			// role <optional>
			
			String invitationString = invitation.getString("invitationURI"); 
			URI invitationURI = new URI(invitationString);
			
			// TODO: look up from XMPP room description if not available here
			JSONObject json = invitation; // hack hack hack attack
			
			//Log.d("junction","have ad from scan: " + json);
			/*
	    	if (json.has("ad")) {
	    		AndroidIntentInfo aii = getAndroidAction(json.optString("ad"));
	    		
	    	}*/
	    	
	    	/*
	    	 * if json.has("role"), get the platform
	    	 * if android is possible, get url. otherwise, check for web. else, fail.
	    	 * if no role, find all roles with suitable platforms
	    	 * display list to user
	    	 * 
	    	 */
	    	
	    	JSONObject bestPlatform = null;
	    	String bestPlatformType = null;
	    	
	    	if (invitation.has("role")) {
	    		String requestedRole = invitation.getString("role");
	    		Log.d("junction","using role " + requestedRole);
	    		
	    		JSONObject roles = json.optJSONObject("roles");
	    		if (null != roles) {
	    			if (roles.has(requestedRole)) {
	    				JSONObject role = roles.optJSONObject(requestedRole);
	    				JSONObject platforms = role.getJSONObject("platforms");
	    				if (platforms.has("android")) {
	    					Log.d("junction","found android codebase");
	    					bestPlatform = platforms.getJSONObject("android");
	    					bestPlatformType = "android";
	    				} else if (platforms.has("web")) {
	    					Log.d("junction","found web codebase");
	    					bestPlatform = platforms.getJSONObject("web");
	    					bestPlatformType = "web";
	    				}
	    			}
	    		} else {
	    			// look up on Junction server
	    			
	    		}
	    	} else {
	    		// No role specified
	    		
	    		// TODO: platform hints ("mobile", "bigscreen", ...)
	    		JSONObject roles = json.optJSONObject("roles");
	    		Iterator<String>keys = roles.keys();
	    		while (keys.hasNext()) {
	    			String key = keys.next();
	    			JSONObject platforms = roles.getJSONObject(key).getJSONObject("platforms");
	    			if (platforms.has("android")) {
	    				bestPlatform = platforms.getJSONObject("android");
    					bestPlatformType = "android";
	    			}
	    			else if (!"android".equals(bestPlatformType) && platforms.has("web")) {
	    				bestPlatform = platforms.getJSONObject("web");
	    				bestPlatformType = "web";
	    			}
	    			
	    		}
	    	}
	    	// TODO: search for any matching roles / platforms
	    	
	    	
	    	String bestPackage = parentActivity.getIntent().getStringExtra("package");
	    	if (bestPackage == null && "android".equals(bestPlatformType)) {
	    		 bestPackage = bestPlatform.optString("package");
	    	}
	    	String bestAppName = bestPackage;
	    	if (null != json.optString("friendlyName")) {
	    		bestAppName = json.optString("friendlyName");
	    	} else if (null != json.optString("ad")) {
	    		bestAppName = json.optString("ad");
	    	}
	    	
	    	if (bestPlatform != null) {
	    		Log.d("junction","got platform " + bestPlatform);
	    		if ("android".equals(bestPlatformType)) {
	    			Log.d("junction","best platform is android");
	    			/*
	    			Intent launchIntent = new Intent("junction.intent.action.JOIN");
	    			launchIntent.putExtra("junctionVersion", 1);
	    			launchIntent.putExtra("activityDescriptor", invitation.toString());
	    			launchIntent.putExtra("invitationURI", invitationString);
	    			*/
	    			Intent launchIntent = AndroidJunctionMaker.getIntentForActivityJoin(invitationURI);
	    			if (IntentLauncher.launch(parentActivity,
	    					launchIntent,
							  bestPackage,
							  bestPlatform.optString("url"),
							  bestAppName
							  )) {
	    				
	    				Log.d("junction","app launched successfully; killing launcher");
	    				parentActivity.finish();
	    			} else {
	    				Log.d("junction","requesting app installation");
	    			}
	    			
	    		} else if ("web".equals(bestPlatformType)) {
	    			StringBuffer inviteParams = new StringBuffer("jxinvite=");
	    			inviteParams.append(invitationString);
	    			
	    			String url = bestPlatform.getString("url");
	    			if (url.contains("?")) {
	    				url += "&" + inviteParams;
	    			} else {
	    				url += "?" + inviteParams;
	    			}
	    			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	    			parentActivity.startActivity(intent);
	    		}
	    	} else if (bestPackage != null) {
    			Intent launchIntent = AndroidJunctionMaker.getIntentForActivityJoin(invitationURI);
    			if (IntentLauncher.launch(parentActivity,
    					launchIntent,
						  bestPackage,
						  null,
						  bestPackage
						  )) {
    				parentActivity.finish();
    			}
	    	}
		} catch (Exception e) {
			Log.e("junction","Error creating junction",e);
			Toast.makeText(parentActivity, "Error joining activity.", Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mNfc.onResume(this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mNfc.onPause(this);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		mNfc.onNewIntent(this, intent);
	}
	
	NdefHandler mNdefHandler = new NdefHandler() {		
		@Override
		public int handleNdef(NdefMessage[] ndefMessages) {
			NdefRecord record = ndefMessages[0].getRecords()[0];
			String type = new String(record.getType());
			if (TYPE_APPMANIFEST.equals(type)) {
				Intent appIntent = new Intent(Intent.ACTION_VIEW);
				appIntent.putExtra("content", record.getPayload());
				appIntent.setClass(ActivityDirector.this, AppManifestHandler.class);
				startActivity(appIntent);
			}
			return NDEF_CONSUME;
		}
	};
}