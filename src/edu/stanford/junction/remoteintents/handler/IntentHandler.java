package edu.stanford.junction.remoteintents.handler;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * TODO:
 * * Support non-aborted broadcasts
 * * Support startActivityForResult queries
 */
public abstract class IntentHandler {
	public abstract void handleIntent(Intent intent);
	
	/**
	 * Converts an Intent object to a
	 * JSON representation.
	 * 
	 * @param intent
	 * @return
	 */
	public static JSONObject intentToJSON(Intent intent) {
		try {
			JSONObject obj = new JSONObject();

			obj.put("action", intent.getAction());

			Set<String>cats = intent.getCategories();
			JSONArray catz = new JSONArray();
			for (String cat : cats) {
				catz.put(cat);
			}
			obj.put("categories",catz);
			obj.put("extras",bundleToJSON(intent.getExtras()));

			return obj;
		} catch (Exception e) {
			Log.e("junction","could not convert intent to json",e);
			return null;
		}
	}

	/**
	 * Converts a Bundle object to a
	 * JSON representation.
	 * 
	 * @param intent
	 * @return
	 */
	public static JSONObject bundleToJSON(Bundle b) {
		try {
			JSONObject obj = new JSONObject();
			if (b == null) return obj;
			Set<String>keys = b.keySet();
			for (String key : keys) {
				Object value = b.get(key);
				if (value.getClass().isArray()) {
					JSONArray jarr = new JSONArray();
					Object[] arr = (Object[])value;
					for (int i=0;i<arr.length;i++) {
						jarr.put(arr[i]);
					}
					obj.put(key,jarr);
				}
				else if (value instanceof Bundle) {
					obj.put(key, bundleToJSON((Bundle)value));
				} else {
					obj.put(key, b.get(key));
				}
			}
			return obj;
		} catch (Exception e) {
			Log.e("junction","could not convert bundle to json",e);
			return null;
		}
	}
}