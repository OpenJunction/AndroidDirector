package edu.stanford.prpl.junction.applaunch;

import android.bluetooth.BluetoothAdapter;
import android.os.Build;

// Backwards compatibility for Bluetooth, added in 2.0.
// http://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom

public abstract class BluetoothWrapper {
	public static int REQUIRED_SDK = 5;
	public static BluetoothWrapper getInstance() {
		if  (Build.VERSION.SDK_INT >= REQUIRED_SDK) {
			return BluetoothWrapperEclair.LazyHolder.sInstance;
		} else {
			return null;
		}
	}
	
	public abstract BluetoothAdapter getAdapter();
	

	private static class BluetoothWrapperEclair extends BluetoothWrapper {
		private static class LazyHolder {
			private static final BluetoothWrapper sInstance = new BluetoothWrapperEclair();
		}

		@Override
		public BluetoothAdapter getAdapter() {
			return BluetoothAdapter.getDefaultAdapter();
		}
	}

}