package aoabook.sample.chap2.accessory;

import java.io.FileDescriptor;
import java.io.FileInputStream;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

// UsbManager, UsbAccessoryのインポート
import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

/*
 * Android、Arduino間で通信をする
 */
public class AOAActivity extends Activity {
	private static final String TAG = "AccessoryMode";

	private static final String ACTION_USB_PERMISSION = "aoabook.sample.chap2.accessory.action.USB_PERMISSION";

	private Button button1;
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
				Toast.makeText(AOAActivity.this, "receiver",
						Toast.LENGTH_SHORT).show();
				//
				// ユーザが確認ダイアログでOKまたはキャンセルを押下した場合
				//
				synchronized (this) {
					// UsbAccessoryインスタンスを取得
					UsbAccessory accessory = UsbManager.getAccessory(intent);
					// ユーザがアクセスを許可した場合
					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						// アクセサリをオープンする
						openAccessory(accessory);
					} else {
						Log.d(TAG, "permission denied for accessory "
								+ accessory);
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
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				ACTION_USB_PERMISSION), 0);

		// フィルタを設定し、ブロードキャストレシーバを登録する
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);

		// ボタンがクリックされた時の処理
		button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOutputStream != null) {
					try {
						button1.setEnabled(false);
						mOutputStream.write('a');

						// readは処理をブロックするので5秒以上かかるとANRが発生してしまう
						// byte[] buffer = new byte[1];
						// mInputStream.read(buffer);
						// Toast.makeText(AccessoryModeActivity.this,
						// "Get data", Toast.LENGTH_SHORT).show();

						new Thread(new MyRunnable()).start();
					} catch (IOException e) {
						Log.e(TAG, "write failed", e);
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
						mUsbManager.requestPermission(accessory,
								mPermissionIntent);
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
			mInputStream = new FileInputStream(fd);
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
			mInputStream = null;
			mOutputStream = null;
			mFileDescriptor = null;
			mAccessory = null;
		}
	}

	private FileInputStream mInputStream;

	class MyRunnable implements Runnable {
		@Override
		public void run() {
			byte[] buffer = new byte[1];

			try {
				// インプットストリームの読み込み
				mInputStream.read(buffer);
				// UI処理はメインスレッドで行う
				// button1.setEnabled(true); //
				// スレッド内でUIに関係するメソッドを呼ぶとCalledFromWrongThreadExceptionが発生
				if (buffer[0] == 'b') {
					AOAActivity.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							button1.setEnabled(true);
							Toast.makeText(AOAActivity.this,
									"Message from Arduino", Toast.LENGTH_SHORT)
									.show();
						}
					});
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
}
