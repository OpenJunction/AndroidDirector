package edu.stanford.junction.remoteintents;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import edu.stanford.junction.remoteintents.handler.JunctionIntentHandler;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class RemoteIntentManager extends Service {
	
	public static final String CATEGORY_REMOTABLE = "";
	private Map<String,BroadcastReceiver> mInstalledReceivers = new HashMap<String,BroadcastReceiver>();
	
	private BroadcastReceiver mTagChanged = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Log.d("junction","Clearing all remote intent handlers");
			for (String key : mInstalledReceivers.keySet()) {
				unregisterReceiver(mInstalledReceivers.get(key));
			}
			mInstalledReceivers.clear();
		}
	};
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		Log.d("junction","RemoteIntent onBind called");
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		IntentFilter filter2 = new IntentFilter("profile.tag.LOAD");
		registerReceiver(mTagChanged, filter2);
		
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		if ("INSTALL_FILTER".equals(intent.getAction())) {
			String method = intent.getStringExtra("method");
			if ("junction".equals(method)) {
				installJunctionHandler(intent);
			} else {
				Log.w("junction","Unknown receiver method " + method);
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private void installJunctionHandler(Intent intent) {
		IntentFilter filter = intent.getParcelableExtra("intentFilter");
		filter.setPriority(100);
		
		String activity = intent.getStringExtra("activityID");
		URI uri;
		try {
			uri = new URI(intent.getStringExtra("activityURI"));
		} catch (Exception e) {
			Log.e("junction","could not get URI to install remote intent handler",e);
			return;
		}
		
		Log.d("junction","installing junction remote intent handler for " + activity);
		final JunctionIntentHandler handler = new JunctionIntentHandler(uri);
		BroadcastReceiver receiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				Log.d("junction","handling remote intent");
				handler.handleIntent(arg1);
				if (this.isOrderedBroadcast()) {
					abortBroadcast();
				}
			}
		};
		
		if (mInstalledReceivers.containsKey(activity)) {
			unregisterReceiver(mInstalledReceivers.get(activity));
		}
		registerReceiver(receiver, filter);
		mInstalledReceivers.put(activity, receiver);
	}

}