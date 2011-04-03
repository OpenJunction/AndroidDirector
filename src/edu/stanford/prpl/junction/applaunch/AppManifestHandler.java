package edu.stanford.prpl.junction.applaunch;

import java.io.InputStream;
import java.util.List;

import org.mobisocial.appmanifest.ApplicationManifest;
import org.mobisocial.appmanifest.platforms.PlatformReference;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class AppManifestHandler extends Activity {
	public static final String EXTRA_APPLICATION_ARGUMENT = "android.intent.extra.APPLICATION_ARGUMENT";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent inbound = getIntent();
		if (inbound == null) {
			Toast.makeText(this, "No app manifest found", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		int bestAndroidIndex = -1;
		int bestWebIndex = -1;
		byte[] manifestBytes = null;
		ApplicationManifest manifest;
		
		// TODO: hack because we have no uri for a tag's payload.
		if (inbound.hasExtra("content")) {
			manifestBytes = inbound.getByteArrayExtra("content");
		} else {
			Uri manifestUri = inbound.getData();
			try {
				// First get length ;(
				InputStream in = getContentResolver().openInputStream(manifestUri);
				int read = 0;
				int total = 0;
				byte[] buffer = new byte[1024];
				while ((read = in.read(buffer)) > 0) {
					total += read;
				}
				
				// Now read bytes.
				manifestBytes = new byte[total];
				total = 0;
				while ((read = in.read(buffer)) > 0) {
					System.arraycopy(buffer, 0, manifestBytes, total, read);
					total += read;
				}
			} catch (Exception e) {
				Toast.makeText(this, "Error reading app manifest", Toast.LENGTH_SHORT).show();
				Log.d("junction","Error reading app manifest", e);
				finish();
				return;
			}
		}
		
		Log.d("NfcService", "manifest size in bytes: " + manifestBytes.length);
		manifest = new ApplicationManifest(manifestBytes);
    	List<PlatformReference> platforms = manifest.getPlatformReferences();
    	int i = 0;
    	Log.d("NfcService", "platform count " + platforms.size());
    	for (PlatformReference platform : platforms) {
    		Log.d("NfcService", "platform " + platform.getPlatformIdentifier());
    		if (platform.getPlatformIdentifier() == ApplicationManifest.PLATFORM_ANDROID_PACKAGE) {
    			bestAndroidIndex = i;
    		} else if (platform.getPlatformIdentifier() == ApplicationManifest.PLATFORM_WEB_GET) {
    			bestWebIndex = i;
    		}
    		i++;
    	}
    	
    	if (bestAndroidIndex != -1) {
    		Intent fire = new Intent();
    		fire.setAction("android.intent.action.MAIN");
    		fire.addCategory("android.intent.category.LAUNCHER");
    		
    		PlatformReference android = manifest.getPlatformReferences().get(bestAndroidIndex);
    		String androidStr = new String(android.getAppReference());
    		int col = androidStr.indexOf(":");
    		String pkg = androidStr.substring(0, col);
    		String arg = androidStr.substring(col+1);
    		
    		fire.setPackage(pkg);
    		fire.putExtra(EXTRA_APPLICATION_ARGUMENT, arg);
    		
    		List<ResolveInfo> resolved = getPackageManager().queryIntentActivities(fire, 0);
    		if (resolved.size() > 0) {
    			ActivityInfo info = resolved.get(0).activityInfo;
    			fire.setComponent(new ComponentName(info.packageName, info.name));
        		startActivity(fire);
        		finish();
        		return;
    		}
    	}
    	
    	if (bestWebIndex != -1) {
    		PlatformReference android = manifest.getPlatformReferences().get(bestWebIndex);
    		String webStr = new String(android.getAppReference());
    		
    		Intent fire = new Intent(Intent.ACTION_VIEW);
    		fire.setData(Uri.parse(webStr));
    		startActivity(fire);
    		finish();
    		return;
    	}
	
		Toast.makeText(this, "No usable platform found.", Toast.LENGTH_SHORT).show();
    	
    	finish();
	}
}
