package aoabook.sample.chap4.fff;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class FastestFingerFirstActivity extends AOABase {
	private Button mStartButton;
	private ImageView mDroids[];
	private SoundPool soundPool;
	
	// SoundPoolで指定する音声id
	private int startSoundId;
	private int buttonSoundId;

	// 早押しの待ち受け状態フラグ
	private boolean isThinkingTime;
	
	/**
	 * 状態をリセットする
	 */
	private void resetState() {
		for (ImageView hat : mDroids) {
			hat.setPressed(false);
		}
	}
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.main);
		
		//
		// 音声をロードする
		//
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		buttonSoundId = soundPool.load(this, R.raw.botan, 1);
		startSoundId = soundPool.load(this, R.raw.start, 1);
		
		//
		// スタートボタンにリスナーを設定。
		//
		mStartButton = (Button)findViewById(R.id.StartButton);
		mStartButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				soundPool.play(startSoundId, 100, 100, 1, 0, 1);
				resetState();
				isThinkingTime = true;
			}
		});

		//
		// Viewとオブジェクトを紐付けます
		//
		mDroids = new ImageView[4];
		mDroids[0] = (ImageView)findViewById(R.id.RedDroidImage);
		mDroids[1] = (ImageView)findViewById(R.id.BlueDroidImage);
		mDroids[2] = (ImageView)findViewById(R.id.YellowDroidImage);
		mDroids[3] = (ImageView)findViewById(R.id.GreenDroidImage);
		
		//
		// ハンドラーの実体
		//
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				byte[] data = (byte[])msg.obj;
				switch (msg.what) {
				case ArduinoProtocol.UPDATE_DIGITAL_STATE:
					switchStateChanged(data[0], data[1] == 1 ? true : false);
					break;
				case ArduinoProtocol.UPDATE_ANALOG_STATE:
					// do nothing
					break;
				default:
					break;
				}
			}
		};
	}
	
	/**
	 * スイッチが押された場合の処理
	 * @param ch
	 * @param state
	 */
	public void switchStateChanged(int ch, boolean state) {
		if (isThinkingTime) {
			if (ch >= 0 && ch < mDroids.length && state) {
				soundPool.play(buttonSoundId, 100, 100, 1, 0, 1);
				mDroids[ch].setPressed(true);
				isThinkingTime = false;
			}
		}
	}
}
