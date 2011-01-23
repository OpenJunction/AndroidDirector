package edu.stanford.prpl.junction.applaunch;

import java.net.URLEncoder;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public class Invitation extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.invitation);

		// TODO: hook up close and launch app buttons
		String uri = getIntent().getExtras().getString("invitationURI");
		uri = URLEncoder.encode(uri);
		WebView webView = (WebView)findViewById(R.id.webview);
        webView.loadUrl("http://chart.apis.google.com/chart?cht=qr&chl="+uri+"&chs=350x350");
	}
}
