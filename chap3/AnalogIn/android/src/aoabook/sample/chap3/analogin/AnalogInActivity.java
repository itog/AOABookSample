package aoabook.sample.chap3.analogin;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

/*
 * アナログ入力の値を表示する
 */
public class AnalogInActivity extends Activity {
	private static final String TAG = "AnaloglIn";

	private static final String ACTION_USB_PERMISSION = "aoabook.sample.accessory.action.USB_PERMISSION";

	private TextView mStatusText;
	
	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;

	private UsbAccessory mAccessory;
	private ParcelFileDescriptor mFileDescriptor;
	private FileInputStream mInputStream;
	
	private boolean mThreadRunning = false;
	
	// USB接続状態変化のインテントを受け取るレシーバ
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				Toast.makeText(AnalogInActivity.this, "receiver", Toast.LENGTH_SHORT).show();
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
		
		mStatusText = (TextView)findViewById(R.id.text_status);
		// UsbManagerのインスタンスを取得
		mUsbManager = UsbManager.getInstance(this);
		
		// ユーザからのアクセス可否のアクションがあった時にブロードキャストされるインテント
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		
		// フィルタを設定し、ブロードキャストレシーバを登録する
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);
		
		checkByteBehavior();
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
			mInputStream = new FileInputStream(fd);
			
			new Thread(new MyRunnable()).start();
		} else {
			Log.d(TAG, "Failed to open the accessory");
		}
	}

	//
	// アクセサリをクローズする
	//
	private void closeAccessory() {
		mThreadRunning = false;
		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			mInputStream = null;
			mFileDescriptor = null;
			mAccessory = null;
		}
	}
	
	class MyRunnable implements Runnable {
		@Override
		public void run() {
			mThreadRunning = true;
			while (mThreadRunning) {
				byte[] buffer = new byte[2];
				try {
					// インプットストリームの読み込み
					mInputStream.read(buffer);
					
					// 受信した2byteをIntに変換
					final int value = composeInt(buffer[0], buffer[1]);
					Log.v(TAG, "Analog value = " + value);
					mStatusText.post(new Runnable() {
						@Override
						public void run() {
							// TextViewにアナログ値を表示する
							mStatusText.setText(String.valueOf(value));
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	/**
	 * 2byteからintを作成する
	 * @param hi 上位バイト
	 * @param lo 下位バイト
	 * @return int値
	 */
	private static int composeInt(byte hi, byte lo) {
		return ((hi & 0xff) << 8) + (lo & 0xff);
	}
	
	/*
	 * Javaのbyteについて
	 */
	void checkByteBehavior() {
		int a = 255; // 0xff
		byte b = (byte)a;
		int c = (int)b;
		int d = b & 0xff;
		Log.v(TAG, String.format("a == %d == 0x%x", a, a)); // a == 255 == 0xff
		Log.v(TAG, String.format("b == %d == 0x%x", b, b)); // a == -1 == 0xff
		Log.v(TAG, String.format("c == %d == 0x%x", c, c)); // a == -1 == 0xffffffff
		Log.v(TAG, String.format("d == %d == 0x%x", d, d)); // a == 255 == 0xff
	}
}
