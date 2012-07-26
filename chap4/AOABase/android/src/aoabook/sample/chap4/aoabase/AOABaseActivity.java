package aoabook.sample.chap4.aoabase;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public class AOABaseActivity extends Activity {
	private static final String TAG = "AOABase";
	
	private static final String ACTION_USB_PERMISSION = "aoabook.sample.aoabase.action.USB_PERMISSION";
	
	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;
	
	private UsbAccessory mAccessory;
	private ParcelFileDescriptor mFileDescriptor;
	
	protected ArduinoProtocol mArduinoProtocol;
	protected Handler mHandler;
	
	/**
	 * システムからのイベントを受け取るレシーバ
	 * ユーザのパーミッション可否、アクセサリ接続解除イベントを処理する
	 */
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = UsbManager.getAccessory(intent);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						openAccessory(accessory);
					} else {
						Log.d(TAG, "permission denied for accessory " + accessory);
					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				UsbAccessory accessory = UsbManager.getAccessory(intent);
				if (accessory != null && accessory.equals(mAccessory)) {
					closeAccessory();
				}
			}
		}
	};
 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
 
		mUsbManager = UsbManager.getInstance(this);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);
 
		if (getLastNonConfigurationInstance() != null) {
			mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
			openAccessory(mAccessory);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
 
		if (mFileDescriptor != null) {
			return;
		}
 
		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			// パーミッション取得済みであればアクセサリを開き、取得済みでなければパーミッションの要求をする
			if (mUsbManager.hasPermission(accessory)) {
				openAccessory(accessory);
			} else {
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						mUsbManager.requestPermission(accessory,mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		} else {
			Log.d(TAG, "mAccessory is null");
		}
	}
 
	@Override
	public void onPause() {
		super.onPause();
		closeAccessory();
	}
 
	@Override
	public void onDestroy() {
		unregisterReceiver(mUsbReceiver);
		super.onDestroy();
	}
	
	/**
	 * アクセサリをオープンする
	 * @param accessory
	 */
	private void openAccessory(UsbAccessory accessory) {
		mFileDescriptor = mUsbManager.openAccessory(accessory);
		if (mFileDescriptor != null) {
			mAccessory = accessory;
			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
			OutputStream os = new FileOutputStream(fd);
			InputStream is = new FileInputStream(fd);

			mArduinoProtocol = new ArduinoProtocol(os, is, mHandler);
		} else {
			Log.d(TAG, "accessory open fail");
		}
	}
 
	/**
	 * アクセサリをクローズする
	 */
	private void closeAccessory() {
		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
			}
			if (mArduinoProtocol != null) {
				mArduinoProtocol.requestStop();
			}
		} catch (IOException e) {
		} finally {
			mFileDescriptor = null;
			mAccessory = null;
		}
	}
}
