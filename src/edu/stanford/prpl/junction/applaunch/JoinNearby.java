package edu.stanford.prpl.junction.applaunch;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.junction.provider.bluetooth.BluetoothSwitchboardConfig;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class JoinNearby extends ListActivity {
	private BluetoothAdapter mBtAdapter;
	private int REQUEST_ENABLE_BT = 424534;
	private ArrayAdapter<String> mListAdapter;
	private List<String> mDeviceAddresses;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nearby);
		
		// Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        
        if (!mBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
        	doBluetoothScan();
        }
	}
	
	private void doBluetoothScan() {
		// ListView bindings
        ListView listView = (ListView)findViewById(android.R.id.list);
        mListAdapter = new ArrayAdapter<String>(this, R.layout.bt_device_name);
        mDeviceAddresses = new ArrayList<String>();
        
        setListAdapter(mListAdapter);
        listView.setOnItemClickListener(mOnItemClickListener);
        
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
        
        // TODO: add button to refresh view
        mBtAdapter.startDiscovery();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_OK) {
				doBluetoothScan();
			} else {
				// TODO: alert user
				finish();
			}
		}
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }


	// The BroadcastReceiver that listens for discovered devices
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                
                // TODO: separate junction:// beacons from devices.
                // TODO: can we see if a device supports our UUID?
                //if (device.getName() != null && device.getName().startsWith("junction://")) {
                	// TODO: look up friendlyName and other information
                	mListAdapter.add(device.getName());
                	mDeviceAddresses.add(device.getAddress());
                //}
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                
            }
        }
    };
    
    private final OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
    	public void onItemClick(AdapterView<?> av, View v, int pos,
    			long id) {
    		
    		mBtAdapter.cancelDiscovery();
    		
    		String jxInvite = null;
    		String txt = ((TextView) v).getText().toString();
    		if (txt.startsWith("junction://")) {
    			jxInvite = txt;
    		} else {
    			// TODO: can we check to see if the app UUID is supported?
    			//jxInvite = "junction://" + mDeviceAddresses.get(pos) + "/" + BluetoothSwitchboardConfig.APP_UUID + "#bt";
    			Toast.makeText(JoinNearby.this, "Junction session not available.", Toast.LENGTH_SHORT).show();
    		}

    		try {
	    		URI act = new URI(jxInvite);
	        	ActivityDirector.createJunction(JoinNearby.this, act);
    		} catch (Exception e) {
    			Log.e("junction","Error launching activity from " + jxInvite,e);
    		}
    	}
	};
}