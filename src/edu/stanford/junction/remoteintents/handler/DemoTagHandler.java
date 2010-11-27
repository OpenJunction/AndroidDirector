package edu.stanford.junction.remoteintents.handler;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Intent;
import android.util.Log;
import edu.stanford.junction.SwitchboardConfig;
import edu.stanford.junction.android.AndroidJunctionMaker;
import edu.stanford.junction.provider.xmpp.XMPPSwitchboardConfig;
import edu.stanford.junction.remoteintents.RemoteIntentEnvironment;
import edu.stanford.junction.remoteintents.RemoteIntentManager;

public class DemoTagHandler extends ActionedIntentHandler {
	public static final String TAG_ACTION = "profile.tag.LOAD";
	
	RemoteIntentEnvironment mEnv = null;
	@Override
	public void handleIntent(Intent intent) {
		String tag = intent.getStringExtra("tag");

		if (tag == null) {
			return;
		}
		
		if (tag.equals("room")) {
			/**
			 * TODO: demo hack.
			 * This is the core functionality of remote intenting-
			 * It is the glue between local/remote.
			 * All the functionality here should be pushed from one 
			 * of those sources.
			 **/
			
			IntentHandler jukeboxHandler = null;
			try {
				Log.d("junction","Switching to jukebox activity");
				jukeboxHandler = new JunctionIntentHandler(new URI("junction://prpl.stanford.edu/jukebox"));
			} catch (Exception e) { Log.e("junction","bad",e); }
			
			/** Set up receivers. **/
			/* Ugh. Should have made one org.jinzora.jukebox.COMMAND action. */
			List<String>actions = new ArrayList<String>();
			actions.add("org.jinzora.jukebox.PLAYLIST");
			actions.add("org.jinzora.jukebox.PLAYLIST_SYNC_RESPONSE");
			actions.add("org.jinzora.jukebox.cmd.PLAY");
			actions.add("org.jinzora.jukebox.cmd.PAUSE");
			actions.add("org.jinzora.jukebox.cmd.NEXT");
			actions.add("org.jinzora.jukebox.cmd.PREV");
			actions.add("org.jinzora.jukebox.cmd.STOP");
			actions.add("org.jinzora.jukebox.cmd.CLEAR");
			actions.add("org.jinzora.jukebox.cmd.JUMPTO");
			actions.add("org.jinzora.jukebox.cmd.PLAYPAUSE");
			
			mEnv.registerIntentHandlers(actions, jukeboxHandler);
			
			/** Request sync. **/
			Intent syncRequest = new Intent("org.jinzora.jukebox.PLAYLIST_SYNC_REQUEST");
			syncRequest.addCategory(RemoteIntentManager.CATEGORY_REMOTABLE);
			mEnv.getContext().sendBroadcast(syncRequest);
			
		} else {
			Log.d("junction","clearing remote intents");
			
			// TODO: this restarts twice. Optimize: mEnv.replaceWith();
			mEnv.clear();
			mEnv.registerIntentHandler(TAG_ACTION, this);
		}
	}	

	public DemoTagHandler(RemoteIntentEnvironment env) {
		mEnv=env;
	}

	@Override
	public String getHandledAction() {
		return TAG_ACTION;
	}
}