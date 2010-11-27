package edu.stanford.junction.remoteintents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RemoteIntentReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent srv = new Intent("INSTALL_FILTER");
		srv.putExtras(intent.getExtras());
		srv.setClassName("edu.stanford.prpl.junction.applaunch", "edu.stanford.junction.remoteintents.RemoteIntentManager");
		context.startService(srv);
	}
}
