package com.fixd.app;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Handles scanning and pairing with the device if not already paired.
 * 
 * @author Rikin Marfatia (rikin@fixdapp.com)
 *
 */
public class DeviceListService extends Activity {
	
	public static final int RESULT_PAIRED = 1;
	
	private static String DEVICE = "OBDII";  // change to OBDII
	
	private BluetoothAdapter adapter;
	private ListView theDevices;
	private ArrayList<BluetoothDevice> foundDevices;
	private ArrayAdapter<String> theDevicesAdapter;
	private ListItemClicked listClicked;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_list_service);
		
		init();
		scanForBTDevices();
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if(receiver != null) {
			unregisterReceiver(receiver);
		}
	}
	
	
	private void init() {
		adapter = BluetoothAdapter.getDefaultAdapter();
		foundDevices = new ArrayList<BluetoothDevice>();
		
		theDevices = (ListView) findViewById(R.id.detected_devices);
		
		listClicked = new ListItemClicked();
		theDevices.setOnItemClickListener(listClicked);
		
		theDevicesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		theDevices.setAdapter(theDevicesAdapter);
	}
	
	/**
	 * This notices any change in state when scanning for devices and checking for
	 * if devices have been paired
	 */
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(BluetoothDevice.ACTION_FOUND.equals(action)) {
				boolean flag = true;
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String deviceName = device.getName();
				Toast.makeText(context, "Found: " + device.getName(), Toast.LENGTH_SHORT).show();
				for(int i = 0; i < foundDevices.size(); i++) {
					if(device.getAddress().equals(foundDevices.get(i).getAddress())) {
						flag = false;
					}
				}
					
				if(flag == true) {
					foundDevices.add(device);
					theDevicesAdapter.add(deviceName);
					theDevicesAdapter.notifyDataSetChanged();
				}
					
					
			} else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String deviceName = device.getName();
				int currState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
				int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
					
				if(currState == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
					toastMaker("Paired with: " + deviceName);
					if(DEVICE.equals(deviceName)) {
						adapter.cancelDiscovery();
						notifyPairing();
						finish();
					}
				}
			}
		}
		  
	};
	
	/**
	 * Scans for available Bluetooth devices in the area
	 */
	private void scanForBTDevices() {
		IntentFilter scanFilter = new IntentFilter();
		scanFilter.addAction(BluetoothDevice.ACTION_FOUND);
		scanFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		registerReceiver(receiver, scanFilter);
		adapter.startDiscovery();
	}
	
	/**
	 * Pairs with the chosen bluetooth device
	 * @param device
	 */
	private void pairDevice(BluetoothDevice device) {
		try {
			Method m = device.getClass().getMethod("createBond", (Class[])null);
			m.invoke(device, (Object[])null);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void notifyPairing() {
		Intent result = new Intent();
		setResult(RESULT_PAIRED, result);
	}
	
	
	private class ListItemClicked implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
			if(foundDevices.size() > 0) {
				BluetoothDevice partner = foundDevices.get(pos);
				Set<BluetoothDevice> paired = adapter.getBondedDevices();
				boolean alreadyPaired = false;
				for(BluetoothDevice device : paired) {
					if(device.getAddress().equals(partner.getAddress())) {
						alreadyPaired = true;
					}
				}
				
				if(!alreadyPaired) {
					pairDevice(partner);
				}	
			}
		}
		
	}

	private void toastMaker(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}
	
}
