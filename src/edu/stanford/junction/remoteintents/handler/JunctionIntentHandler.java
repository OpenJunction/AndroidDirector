package edu.stanford.junction.remoteintents.handler;

import java.net.URI;

import org.json.JSONObject;

import android.content.Intent;
import android.util.Log;
import edu.stanford.junction.JunctionException;
import edu.stanford.junction.SwitchboardConfig;
import edu.stanford.junction.android.AndroidJunctionMaker;
import edu.stanford.junction.provider.xmpp.XMPPSwitchboardConfig;

public class JunctionIntentHandler extends IntentHandler {
	
	SwitchboardConfig mSwitchboardConfig = new XMPPSwitchboardConfig("prpl.stanford.edu");
	AndroidJunctionMaker mJunctionMaker = AndroidJunctionMaker.getInstance(mSwitchboardConfig);

	URI mActivity = null;
	
	@Override
	public void handleIntent(Intent intent) {
		JSONObject obj = intentToJSON(intent);
		Log.d("junction","sending intent to remote activity");
		try {
			mJunctionMaker.sendMessageToActivity(mActivity, obj);
		} catch (JunctionException e) {
			Log.e("junction",e.getMessage());
		}
	}	

	/**
	 * Constructs a JunctionIntentHandler, which passes
	 * a given intent to the specified activity session,
	 * given in this constructor.
	 * @param activity
	 */
	public JunctionIntentHandler(URI activity) {
		mActivity = activity;
	}
}