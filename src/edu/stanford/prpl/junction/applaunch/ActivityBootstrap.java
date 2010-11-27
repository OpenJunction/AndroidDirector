package edu.stanford.prpl.junction.applaunch;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.junction.Junction;
import edu.stanford.junction.android.AndroidJunctionMaker;
import edu.stanford.junction.provider.xmpp.XMPPSwitchboardConfig;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

/**
 * This is the main user-facing Director application.
 * It also starts background listeners, such as the
 * RemoteIntent listener.
 * 
 * @author bjdodson
 *
 */
public class ActivityBootstrap extends Activity {
	private String mNumber;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mNumber = getIntent().getStringExtra("contact");
		
		final List<Map<String, ?>> available = getAvailableActivities(this);
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
					connectToJunction((ResolveInfo)((Map<String,?>)adapter.getItem(pos)).get("resolveInfo"));
				}
			})
			.setTitle("Start P2P session")
			.create().show();
	}
	
	
	private void connectToJunction(ResolveInfo info) {
		XMPPSwitchboardConfig config = new XMPPSwitchboardConfig("sb.openjunction.org");
		AndroidJunctionMaker jm = AndroidJunctionMaker.getInstance(config);

		URI invitation = jm.generateSessionUri();
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(mNumber, null, invitation.toString(), null, null);
		Toast.makeText(this, "Invitation sent by SMS.", Toast.LENGTH_SHORT).show();
		
		// TODO: set this up as a PendingIntent in case of delivery error;
		// Use in sendTextMessage().
		Intent intentForActivity = AndroidJunctionMaker.getIntentForActivityJoin(invitation);
		intentForActivity.addCategory(Intents.CATEGORY_BOOTSTRAP);
		intentForActivity.setClassName(info.activityInfo.packageName, info.activityInfo.name);
		startActivity(intentForActivity);
		finish();
	}
	
	// Clean up and move to public place
	public static List<Map<String,?>> getAvailableActivities(Context ctx) {
		Intent joinIntent = new Intent(AndroidJunctionMaker.Intents.ACTION_JOIN);
		joinIntent.addCategory(Intents.CATEGORY_BOOTSTRAP);

		List<ResolveInfo>resolved = ctx.getPackageManager().queryIntentActivities(joinIntent, 0);
		List<Map<String,?>> activities = new ArrayList<Map<String,?>>();
		for (ResolveInfo r : resolved) {
			Map<String,Object> info = new HashMap<String,Object>();
			info.put("name", r.activityInfo.loadLabel(ctx.getPackageManager()));
			info.put("icon", r.activityInfo.getIconResource());
			info.put("resolveInfo",r);
			activities.add(info);
		}
		return activities;
	}
	
	private class JXActivity {
		ResolveInfo mResolveInfo;
		PackageManager mPackageManager;
		
		JXActivity(PackageManager pm, ResolveInfo info) {
			mResolveInfo = info;
			mPackageManager = pm;
		}
		
		public String toString() {
			CharSequence s = mResolveInfo.activityInfo.loadLabel(mPackageManager);
			
			return (s == null) ? "Unknown" : s.toString();
		}
	}
}