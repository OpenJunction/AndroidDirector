package edu.stanford.prpl.junction.applaunch;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Invite extends Activity {
	private static String SCAN_APP = "com.google.zxing.client.android";
	private static String SCAN_ACTION = "com.google.zxing.client.android.ENCODE";
	
	private String uri = null;
	private String callingPkg = null;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final Bundle extras = getIntent().getExtras();
		uri = extras.getString("uri");
		callingPkg = extras.getString("package");
		
		setContentView(R.layout.invite);
		
		((Button) findViewById(R.id.button_showcode))
			.setOnClickListener(new OnClickListener() {
				//@Override
				public void onClick(View arg0) {
					Intent intent = new Intent("junction.intent.action.invite.QR");
					intent.putExtras(extras);
					startActivity(intent);
					finish();
				}
			});
		
		((Button) findViewById(R.id.button_scan))
		.setOnClickListener(new OnClickListener() {
			//@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intents.INVITE_SCAN);
				intent.putExtras(extras);
				startActivity(intent);
				finish();
			}
		});
		
		((Button) findViewById(R.id.button_sms))
		.setOnClickListener(new OnClickListener() {
			//@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intents.INVITE_SMS);
				intent.putExtras(extras);
				startActivity(intent);
				finish();
			}
		});
		
	}
}
