package de.emdete.android.gui;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class Sample extends Activity {
	public static final String TAG = "de.emdete.sample";
	public static final ParcelUuid TEMPERATURE = ParcelUuid.fromString("00001809-0000-1000-8000-00805f9b34fb");
	public static final ParcelUuid BATTERY = ParcelUuid.fromString("0000180f-0000-1000-8000-00805f9b34fb");

	final Handler mHandler = new Handler() {
		@Override public void handleMessage(Message msg) {
			switch (msg.what) {
				default:
					Log.d(TAG, "MESSAGE=" + msg.what);
					break;
			}
		}
	};
	BluetoothManager bluetoothManager;
	BluetoothAdapter bluetoothAdapter;
	BluetoothGatt bluetoothGatt;
	BluetoothLeScanner bluetoothLeScanner;
	BluetoothDevice device;
	BluetoothGattCharacteristic characteristic;
	String device_address;
	String device_name;
	int counter;
	final ScanCallback scanCallback = new ScanCallback() {
		/*public void onBatchScanResults(List<ScanResult> results) {
			for (ScanResult result: results) {
				onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, result);
			}
		}*/
		public void onScanFailed(int errorCode) {
			Log.e(TAG, "onScanFailed() errorCode=" + errorCode);
			((Button) findViewById(R.id.button)).setText("Stopped");
		}

		public void onScanResult(int callbackType, ScanResult result) {
			//BluetoothDevice device = result.getDevice();
			//int rssi = result.getRssi();
			//long timestamp = result.getTimestampNanos();
			ScanRecord record = result.getScanRecord();
			//Map<ParcelUuid, byte[]> serviceDataList = record.getServiceData();
			byte[] temperature = record.getServiceData(TEMPERATURE);
			if (temperature != null) {
				final String tempStr = "" + (counter++) + ": " + tempFromBytes(temperature) + "°C";
				Log.d(TAG, "temperature=" + tempStr);
				((Button) findViewById(R.id.button)).setText(tempStr);
				if (device_name == null) {
					device_address = result.getDevice().getAddress();
					device_name = record.getDeviceName();
					bluetoothLeScanner.stopScan(scanCallback);
					step_3();
				}
				return;
			}
			//byte[] battery = record.getServiceData(BATTERY);
			Log.e(TAG, "onScanResult() result=" + result);
		}
	};
	final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			assert gatt == bluetoothGatt;
			switch (newState) {
				case BluetoothProfile.STATE_CONNECTING:
					Log.i(TAG, "STATE_CONNECTING:" + gatt);
					((Button) findViewById(R.id.button)).setText("Connecting");
					break;
				case BluetoothProfile.STATE_CONNECTED:
					Log.i(TAG, "STATE_CONNECTED: Attempting to start service discovery" + gatt);
					((Button) findViewById(R.id.button)).setText("Connected");
					bluetoothGatt.discoverServices();
					break;
				case BluetoothProfile.STATE_DISCONNECTING:
					Log.i(TAG, "STATE_DISCONNECTING:" + gatt);
					((Button) findViewById(R.id.button)).setText("Disconnecting");
					break;
				case BluetoothProfile.STATE_DISCONNECTED:
					Log.i(TAG, "STATE_DISCONNECTED:" + gatt);
					((Button) findViewById(R.id.button)).setText("Disconnected");
					bluetoothGatt.close();
					bluetoothGatt = null;
					break;
				default:
					Log.d(TAG, "onConnectionStateChange: status=" + status + ", newState=" + newState);
					break;
			}
		}

		@Override public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			Log.d(TAG, "onCharacteristicChanged:");
		}

		@Override public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			Log.d(TAG, "onCharacteristicRead: status=" + status);
		}

		@Override public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			Log.d(TAG, "onCharacteristicRead: status=" + status);
		}

		@Override public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			Log.d(TAG, "onReadRemoteRssi: status=" + status);
		}

		@Override public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			Log.d(TAG, "onServicesDiscovered: status=" + status);
		}

		@Override public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			Log.d(TAG, "onDescriptorRead: status=" + status);
		}

	};

	float tempFromBytes(byte[] temperature) {
		if (false)
			return ByteBuffer.wrap(temperature).order(ByteOrder.LITTLE_ENDIAN).getFloat();
		float ret = temperature[2];
		ret *= 256;
		ret += temperature[1] & 0x0ff;
		ret *= 256;
		ret += temperature[0] & 0x0ff;
		ret *= Math.pow(10, temperature[3]);
		return ret;
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate savedInstanceState=" + savedInstanceState);
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		Log.d(TAG, "onCreate preferences=" + preferences.getAll());
		device_address = preferences.getString("device_address",
			savedInstanceState==null?null:savedInstanceState.getString("device_address")); // "C5:D0:0A:0F:D6:1F");
		device_name = preferences.getString("device_name",
			savedInstanceState==null?null:savedInstanceState.getString("device_name")); // "F16D0D5C");
		Log.d(TAG, "onCreate device_address=" + device_address + ", device_name=" + device_name);
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override public void uncaughtException(Thread thread, Throwable e) {
				Log.e(TAG, "error e=" + e, e);
				finish();
			}
		});
		setContentView(R.layout.main);
		findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				Log.d(TAG, "onClick");
				step_rep();
			}
		});
		((Button)findViewById(R.id.button)).setText("Go!");
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult");
		switch (requestCode) {
			case 1:
				step_1();
				break;
			default:
				Log.d(TAG, "onActivityResult requestCode=" + requestCode);
				break;
		}
	}

	void step_1() {
		Log.d(TAG, "step_1");
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			bluetoothAdapter = bluetoothManager.getAdapter();
			if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
				step_2();
			} else {
				startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
			}
		}
		else {
			; // tell user he has no bt
		}
	}

	void step_2() {
		Log.d(TAG, "step_2");
		bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
		if (bluetoothLeScanner != null) {
			if (device_address != null) {
				step_3();
			} else {
				bluetoothLeScanner.startScan(scanCallback);
			}
		} else {
			Log.d(TAG, "step 2 failed: no BT LE");
		}
	}

	void step_3() {
		Log.d(TAG, "step_3");
		ScanSettings.Builder builderS = new ScanSettings.Builder();
		builderS.setReportDelay(0);
		builderS.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
		List<ScanFilter> filters = new ArrayList<>(1);
		ScanFilter.Builder builderF = new ScanFilter.Builder();
		builderF.setDeviceAddress(device_address);
		filters.add(builderF.build());
		bluetoothLeScanner.startScan(filters, builderS.build(), scanCallback);
		device = bluetoothAdapter.getRemoteDevice(device_address);
		if (device == null) {
			Log.d(TAG, "step 3 failed: no DEV");
			return;
		}
		bluetoothGatt = device.connectGatt(this, true, mGattCallback);
		if (bluetoothGatt == null) {
			Log.d(TAG, "step 3 failed: no GATT");
			return;
		}
		characteristic = new BluetoothGattCharacteristic(TEMPERATURE.getUuid(), 0, BluetoothGattCharacteristic.PERMISSION_READ);
		bluetoothGatt.setCharacteristicNotification(characteristic, true);
		step_rep();
	}

	void step_rep() {
		Log.d(TAG, "step_rep");
		if (bluetoothGatt != null && characteristic != null) {
			bluetoothGatt.connect();
			if (bluetoothGatt.readCharacteristic(characteristic)) {
				Log.d(TAG, "read characteristic=" + characteristic);
			}
			else {
				Log.d(TAG, "not read characteristic=" + characteristic);
			}
		}
	}

	void step_close() {
		Log.d(TAG, "step_close");
		if (bluetoothLeScanner != null) {
			bluetoothLeScanner.stopScan(scanCallback);
			bluetoothLeScanner = null;
		}
		if (bluetoothGatt != null) {
			bluetoothGatt.disconnect();
			bluetoothGatt.close();
			bluetoothGatt = null;
		}
		if (device != null) {
			device = null;
		}
	}

	@Override protected void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
		step_1();
	}

	@Override protected void onRestart() {
		super.onRestart();
		Log.d(TAG, "onRestart");
	}

	@Override protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
	}

	@Override protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
		step_close();
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("device_address", device_address);
		editor.putString("device_name", device_name);
		editor.commit();
	}

	@Override protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
	}

	@Override protected void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		Log.d(TAG, "onSaveInstanceState bundle=" + bundle);
		bundle.putString("device_address", device_address.toLowerCase());
		bundle.putString("device_name", device_name);
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
	}
}
