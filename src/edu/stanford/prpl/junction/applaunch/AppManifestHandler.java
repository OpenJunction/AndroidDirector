package edu.stanford.prpl.junction.applaunch;

import java.io.InputStream;
import java.util.List;

import edu.stanford.mobisocial.appmanifest.ApplicationManifest;
import edu.stanford.mobisocial.appmanifest.platforms.PlatformReference;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

public class AppManifestHandler extends Activity {

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
		
		Uri manifestUri = inbound.getData();
		byte[] manifestBytes = null;
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
			total = 0;
			manifestBytes = new byte[total];
			while ((read = in.read(buffer)) > 0) {
				System.arraycopy(buffer, 0, manifestBytes, total, read);
				total += read;
			}
		} catch (Exception e) {
			
		}
		
		ApplicationManifest manifest = new ApplicationManifest(manifestBytes);
    	List<PlatformReference> platforms = manifest.getPlatformReferences();
    	int i = 0;
    	for (PlatformReference platform : platforms) {
    		if (platform.getPlatformIdentifier() == ApplicationManifest.PLATFORM_ANDROID_PACKAGE) {
    			bestAndroidIndex = i;
    		} else if (platform.getPlatformIdentifier() == ApplicationManifest.PLATFORM_WEB_GET) {
    			bestWebIndex = i;
    		}
    		i++;
    	}
    	
    	Toast.makeText(this, "Android and web platforms at " + bestAndroidIndex + ", " + bestWebIndex, Toast.LENGTH_SHORT).show();
	}
}
