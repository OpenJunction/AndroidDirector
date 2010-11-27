package edu.stanford.junction.remoteintents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import edu.stanford.junction.remoteintents.handler.ActionedIntentHandler;
import edu.stanford.junction.remoteintents.handler.IntentHandler;

public class RemoteIntentEnvironment extends IntentHandler {
	// TODO: support multiple handlers per action?
	// support late-binding actions?
	private Map<String,IntentHandler> mHandlers = new HashMap<String,IntentHandler>();
	private RemoteIntentManager mService=null;
	
	public RemoteIntentEnvironment(RemoteIntentManager service) {
		mService = service;
	}
	
	public boolean supportsAction(String action) {
		return mHandlers.containsKey(action);
	}
	
	@Override
	public void handleIntent(Intent intent) {
		String action = intent.getAction();
		if (!supportsAction(action)) {
			Log.w("junction","could not handle intent " + intent);
			return;
		}
		
		mHandlers.get(action).handleIntent(intent);
	}
	
	public void registerIntentHandlers(List<String> actions, IntentHandler handler) {
		for (int i=0;i<actions.size();i++){
			mHandlers.put(actions.get(i), handler);
		}
		//mService.reload();
	}
	
	public void registerIntentHandler(String action, IntentHandler handler) {
		//Log.d("junction","registered " + action);
		mHandlers.put(action, handler);
		//mService.reload();
	}
	
	public void registerIntentHandler(ActionedIntentHandler handler) {
		String action = handler.getHandledAction();
		registerIntentHandler(action,handler);
	}
	
	public Set<String> getSupportedActions() {
		return mHandlers.keySet();
	}
	
	public void clear() {
		//onClearHandler.onClear();
		mHandlers.clear();
		//mService.reload();
		
		
		// this.notify();
	}
	
	public Context getContext() {
		return mService;
	}
}
