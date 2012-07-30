package aoabook.sample.chap3.digitalout;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

/*
 * デジタル出力をする
 */
public class DigitalOutActivity extends Activity {
	private static final String TAG = "DigitalOut";

	private static final String ACTION_USB_PERMISSION = "aoabook.sample.ccessory.action.action.USB_PERMISSION";

	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;

	private UsbAccessory mAccessory;
	private ParcelFileDescriptor mFileDescriptor;
	private OutputStream mOutputStream;

	// USB接続状態変化のインテントを受け取るレシーバ
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				Toast.makeText(DigitalOutActivity.this, "receiver", Toast.LENGTH_SHORT).show();
				//
				// ユーザが確認ダイアログでOKまたはキャンセルを押下した場合
				//
				synchronized (this) {
					// UsbAccessoryインスタンスを取得
					UsbAccessory accessory = UsbManager.getAccessory(intent);
					// ユーザがアクセスを許可した場合
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						// アクセサリをオープンする
						openAccessory(accessory);
					} else {
						Log.d(TAG, "permission denied for accessory " + accessory);
					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				//
				// アクセサリが切断された場合
				//
				UsbAccessory accessory = UsbManager.getAccessory(intent);

				if (accessory != null && accessory.equals(mAccessory)) {
					// アクセサリをクローズする
					closeAccessory();
				}
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// UsbManagerのインスタンスを取得
		mUsbManager = UsbManager.getInstance(this);
		
		// ユーザからのアクセス可否のアクションがあった時にブロードキャストされるインテント
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		
		// フィルタを設定し、ブロードキャストレシーバを登録する
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);
		
		ToggleButton ledToggle = (ToggleButton)findViewById(R.id.toggle_led);
		ledToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (mOutputStream != null) {
					byte[] cmd = new byte[1];
					try {
						if (isChecked) {
							Log.v(TAG, "checked");
							cmd[0] = 1;
							mOutputStream.write(cmd);
						} else {
							Log.v(TAG, "unchecked");
							cmd[0] = 0;
							mOutputStream.write(cmd);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		// アクセサリリストを取得する
		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		// 現状の仕様ではアクセサリは一つなので先頭要素のみ
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (mUsbManager.hasPermission(accessory)) {
				// パーミッションを取得している場合はアクセサリをオープンする
				openAccessory(accessory);
			} else {
				// パーミッションを取得していない場合はrequestPermission()を呼んでユーザにパーミッションを要求する
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						// アクセス許可確認ダイアログを表示する
						mUsbManager.requestPermission(accessory, mPermissionIntent);
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
		// アクセサリをクローズする
		closeAccessory();
	}

	@Override
	public void onDestroy() {
		// ブロードキャストレシーバの登録解除
		unregisterReceiver(mUsbReceiver);
		super.onDestroy();
	}

	//
	// アクセサリをオープンする
	//
	private void openAccessory(UsbAccessory accessory) {
		// アクセサリをオープンする
		mFileDescriptor = mUsbManager.openAccessory(accessory);
		if (mFileDescriptor != null) {
			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
			mOutputStream = new FileOutputStream(fd);
		} else {
			Log.d(TAG, "Failed to open the accessory");
		}
	}

	//
	// アクセサリをクローズする
	//
	private void closeAccessory() {
		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			mOutputStream = null;
			mFileDescriptor = null;
			mAccessory = null;
		}
	}
}
