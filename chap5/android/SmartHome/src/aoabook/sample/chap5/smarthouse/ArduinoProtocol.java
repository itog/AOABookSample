package aoabook.sample.chap5.smarthouse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Handler;
import android.util.Log;

/**
 * class Arduino Accessory
 * Arduinoベースのアクセサリとの接続プロトコル依存部
 *
 */
public class ArduinoProtocol {
	static final String TAG = "ArduinoAccessory";
	
	/**
	 * アクセサリとの通信プロトコル
	 * [startbyte][size][cmd][data...]
	 */
	static final byte START_BYTE = 0x7f;
	static final byte CMD_AC_POWER = 0x02;
	
	static final int HEADER_SIZE = 3;
	
	static final int MAX_BUF_SIZE = 1024;
	
	private OutputStream mOutputStream;
	private InputStream mInputStream;
	private Handler mHandler;
	private boolean mThreadRunning;
	
	/**
	 * コンストラクタ
	 */
	public ArduinoProtocol(OutputStream os, InputStream is, Handler h) {
		mOutputStream = os;
		mInputStream = is;
	}
	
	/**
	 * スレッド停止要求
	 */
	public void requestStop() {
		mThreadRunning = false;
	}

	/**
	 * エアコンの電源ON/OFF
	 */
	void airconPower(boolean on) {
		byte[] buffer = new byte[HEADER_SIZE + 2];
		buffer[0] = START_BYTE;
		buffer[1] = (byte)buffer.length;
		buffer[2] = CMD_AC_POWER;
		buffer[3] = on ? (byte)1 : (byte)0;
		
		/**
		 * アクセサリに出力する
		 */
		if (mOutputStream != null) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "write failed", e);
			}
		}
	}
	
	/**
	 * Accessoryからメッセージを受けた時のコールバック
	 * プロトコルの解析を行い、処理をHandlerに渡す
	 * @param data
	 */
	public void onAccessoryMessage(byte[] data) {
		Log.v(TAG, "receive message");
	}
}
