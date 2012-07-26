package aoabook.sample.chap5.smarthouse;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public class AOABaseService extends Service {
	private static final String TAG = "AccessoryBaseActivity";
	
	private static final String ACTION_USB_PERMISSION = "aoabook.sample.chap5.temperaturelogger.action.USB_PERMISSION";
	
	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;
	
	private UsbAccessory mAccessory;
	private ParcelFileDescriptor mFileDescriptor;
	
	protected ArduinoProtocol mArduinoAccessory;
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
	public void onCreate() {
		super.onCreate();
 
		mUsbManager = UsbManager.getInstance(this);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);
 	}

	@Override
	public void onStart(Intent i, int id) {
		super.onStart(i, id);
 
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
	public void onDestroy() {
		closeAccessory();
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
			
            mArduinoAccessory = new ArduinoProtocol(os, is, mHandler);
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
			if (mArduinoAccessory != null) {
				mArduinoAccessory.requestStop();
			}
		} catch (IOException e) {
		} finally {
			mFileDescriptor = null;
			mAccessory = null;
			mArduinoAccessory = null;
		}
	}
	
	protected void setHandler(Handler handler) {
		mHandler = handler;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
