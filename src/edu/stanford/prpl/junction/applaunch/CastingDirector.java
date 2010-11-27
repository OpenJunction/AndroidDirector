package edu.stanford.prpl.junction.applaunch;

import java.net.URI;

import org.json.JSONException;
import org.json.JSONObject;

import edu.stanford.junction.JunctionMaker;
import edu.stanford.junction.SwitchboardConfig;
import edu.stanford.junction.android.AndroidJunctionMaker;
import edu.stanford.junction.android.WaitForInternet;
import edu.stanford.junction.android.WaitForInternetCallback;
import edu.stanford.junction.api.activity.ActivityScript;
import edu.stanford.junction.provider.xmpp.XMPPSwitchboardConfig;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class CastingDirector extends Activity {
	private boolean LAST_ENVIRONMENT_QUERY=false;
	private static String SCAN_PKG = "com.google.zxing.client.android";
	private static String SCAN_ACTION = "com.google.zxing.client.android.SCAN";
	
	private String mPkg = null;
	
	private SwitchboardConfig mSwitchboardConfig;
	private String[] mRoles = null;
	private String[] mDirectors = null;
	private String mScriptTxt = null;
	private ActivityScript mScript = null;
	private int mCastOrJoin = 0;
	private boolean amCastingActivity=true; // whether we are casting or joining
	private String mJoiningSessionURI = null;
	
	private int mFillingIndex = -1;
	private int REQUEST_SCAN = 540123;
	
	
	// TODO: better define the role of an environment
	protected static URI sLastEnvironment = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mSwitchboardConfig = new XMPPSwitchboardConfig("prpl.stanford.edu");
			// so gross. sorry for the pain this will cause when removing. bjd, 4/22/10.
		mPkg = getIntent().getStringExtra(AndroidJunctionMaker.Intents.EXTRA_CAST_PACKAGE);
		mRoles = getIntent().getStringArrayExtra(AndroidJunctionMaker.Intents.EXTRA_CAST_ROLES);
		mDirectors = getIntent().getStringArrayExtra(AndroidJunctionMaker.Intents.EXTRA_CAST_DIRECTORS);
		mCastOrJoin = getIntent().getIntExtra(AndroidJunctionMaker.Intents.EXTRA_CAST_OR_JOIN, 
				AndroidJunctionMaker.Intents.ALLOW_CAST|AndroidJunctionMaker.Intents.ALLOW_JOIN);
		
		
		
		/**
		 * Casting Director first created
		 * after being triggered by
		 * a request to create or join an activity.
		 */
		Log.d("junction","Casting " + mRoles.length + " roles");
		
		mScriptTxt = getIntent().getStringExtra(AndroidJunctionMaker.Intents.EXTRA_ACTIVITY_SCRIPT);
		mScript = null;
		try {
			mScript = new ActivityScript(new JSONObject(mScriptTxt));
		} catch (JSONException e) {
			Log.e("junction","error creating activity script",e);
		}
		
		// TODO: demo hack here.
		if (mRoles.length > 1) {
			Log.w("junction","Casting code is not production-ready. Hacked to support one casting.");
		}
		mFillingIndex=0;
		
		
		if (LAST_ENVIRONMENT_QUERY && sLastEnvironment != null) {
			AlertDialog alert =
				new AlertDialog.Builder(this)
					.setTitle("Load in environment?")
					.setMessage("Would you like to use your last known environment?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int arg1) {
							mDirectors[mFillingIndex] = sLastEnvironment.toString();
							CastingDirector.this.runOnUiThread(new Runnable() {
								public void run() {
									doFireIfReady();
								}
							});
						}
					})
					.setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							sLastEnvironment=null;
							CastingDirector.this.runOnUiThread(new Runnable() {
								public void run() {
									doScanForEnv();
								}
							});
						}
					}).create();
			alert.show();
		} else {
			// No known environment.
			doScanForEnv();
		}
	}
	
	
	private void doRejoinActivity() {
		// Once we have all the roles filled..:
		Intent rejoinIntent = new Intent(AndroidJunctionMaker.Intents.ACTION_JOIN);
		rejoinIntent.putExtra(AndroidJunctionMaker.Intents.EXTRA_JUNCTION_VERSION, AndroidJunctionMaker.JUNCTION_VERSION);
		rejoinIntent.setPackage(mPkg);
		if (amCastingActivity) {
			rejoinIntent.putExtra(AndroidJunctionMaker.Intents.EXTRA_CAST_ROLES, mRoles);
			rejoinIntent.putExtra(AndroidJunctionMaker.Intents.EXTRA_CAST_DIRECTORS, mDirectors);
			rejoinIntent.putExtra(AndroidJunctionMaker.Intents.EXTRA_ACTIVITY_SCRIPT, mScriptTxt);
		} else {
			rejoinIntent.putExtra(AndroidJunctionMaker.Intents.EXTRA_ACTIVITY_SESSION_URI,mJoiningSessionURI);
		}
		
		
		// note: if you don't set the package, the user is prompted to choose
		// from a list of apps. This is a useful way to launch a generic activity
		
		startActivity(rejoinIntent);
		finish();
	}
	
	private void doFireIfReady() {
		if (amCastingActivity) {
			for (int i=0;i<mDirectors.length;i++) {
				if (AndroidJunctionMaker.CASTING_DIRECTOR.toString().equals(mDirectors[i])) {
					Log.d("junction","activity not ready yet");
					return;
				}
			}
		}
		//Log.d("junction","all systems go. starting activity");
		doRejoinActivity();
	}
	
	private void doScanForEnv() {
		WaitForInternetCallback callback = 
			new WaitForInternetCallback(this) {
				public void onConnectionSuccess() {
					Intent intent = new Intent(SCAN_ACTION);
			        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
					IntentLauncher.launch(CastingDirector.this, 
							  intent,
							  SCAN_PKG,
							  "Barcode Scanner");
				}
				
				public void onConnectionFailure() {
					Log.w("junction","Could not get an internet connection.");
					CastingDirector.this.finish();
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
	
	class LookupActivityTask extends AsyncTask<Intent, String, Void> {
		private ProgressDialog dialog;
		
		LookupActivityTask() {
			dialog = new ProgressDialog(CastingDirector.this);
			//dialog.setTitle("Joining Activity");
			dialog.setIndeterminate(true);
			dialog.setMessage("Joining activity...");
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new Dialog.OnCancelListener() {
				public void onCancel(DialogInterface arg0) {
					LookupActivityTask.this.cancel(true);
				}
			});
		}
		
		@Override
		protected void onPreExecute() {
			dialog.show();
		}
		
		@Override
		protected void onPostExecute(Void result) {
			dialog.dismiss();
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			dialog.setMessage(values[0]);
		}
		
		@Override
		protected void onCancelled() {
			CastingDirector.this.finish();
		}
		
		@Override
		protected Void doInBackground(Intent... args) {
			try {
				Intent data = args[0];
				String contents = data.getStringExtra("SCAN_RESULT");
				URI uri = new URI(contents);
				AndroidJunctionMaker jm = AndroidJunctionMaker.getInstance(mSwitchboardConfig);
				this.publishProgress("Looking up activity information...");
				
				ActivityScript script = jm.getActivityScript(uri);
				String joiningAD = script.getActivityID();
				if (joiningAD != null && joiningAD.equalsIgnoreCase(mScript.getActivityID())) {
					this.publishProgress("Joining existing activity...");
					
					if ((mCastOrJoin & AndroidJunctionMaker.Intents.ALLOW_JOIN) == 0) {
						Log.w("junction","Intent does not allow joining, but an existing session has been found. Joining anyways.");
					}
					amCastingActivity=false;
					mJoiningSessionURI=contents;
				} else if (JunctionMaker.DIRECTOR_ACTIVITY.equalsIgnoreCase(joiningAD)) {
					this.publishProgress("Launching new activity...");
					if ((mCastOrJoin & AndroidJunctionMaker.Intents.ALLOW_CAST) == 0) {
						Log.w("junction","Intent does not allow casting, but a director has been found. Casting anyways.");
					}
										
					// TODO: see if the director is running/knows about an instance of this activity?
					sLastEnvironment=uri;
					mDirectors[mFillingIndex] = contents;	
				} else {
					Log.w("junction","Found an activity that is not a director or the requested type (" + joiningAD + "). Joining anyways.");
					amCastingActivity=false;
					mJoiningSessionURI=contents;
				}
				
				
				mFillingIndex = -1;
				this.publishProgress("Launching application...");
				doFireIfReady();
			} catch (Exception e) {
				Log.e("junction","error scanning URI",e);
				Toast.makeText(CastingDirector.this, "Error joining session.", Toast.LENGTH_SHORT).show();
				finish();
			}
			
			
			return null;
		}
	};
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == IntentLauncher.REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				LookupActivityTask task = new LookupActivityTask();
				task.execute(data);
			} else {
				Log.d("junction","Failed to scan QR");
				finish();
			}
		} else {
			Log.w("junction","Unknown request code");
			finish();
		}
	}
}



/*


if(true)return;
// TODO: support multiple castings; support more than scanning.

int size = mRoles.length;
for (int i=0;i<size;i++) {
	if (AndroidJunctionMaker.CASTING_DIRECTOR.toString().equals(mDirectors[i])) {
		try {
			
			
			// fill this role
			// use hints to find directors and/or activities
			JSONObject spec = mScript.getRoleSpec(mRoles[i]);
			Log.d("junction","filling in " + spec);
			String hint = null;
			if (spec.has("hints")) {
				JSONArray hints = spec.getJSONArray("hints");
				Log.d("junction","Found hint: " + hints.getString(0));
				hint = hints.getString(0);
			}
			
			if (hint != null) {
				//directors[i] = directorStr;
			}
			
			if (AndroidJunctionMaker.CASTING_DIRECTOR.toString().equals(mDirectors[i])) {
				Log.w("junction","could not get director");
				// TODO
				// Hack for testing
				mDirectors[i] = "junction://prpl.stanford.edu/bj-bigscreen";
			}
		} catch (Exception e) {
			Log.e("junction","Error casting role",e);
		}
	} else {
		Log.d("junction","leaving " + mDirectors[i]);
	}
}

*/