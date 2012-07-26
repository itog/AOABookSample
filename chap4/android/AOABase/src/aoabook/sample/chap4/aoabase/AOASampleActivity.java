package aoabook.sample.chap4.aoabase;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;


public class AOASampleActivity extends AOABaseActivity {
	private static final String TAG = "OpenAccessoryBaseActivity";
	
	private ToggleButton[] mDigitalOutToggles;
	private SeekBar[] mAnalogOutSeekBars;
	private TextView[] mSwitchStatuses;
	private TextView[] mAnalogInValues;
	 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
 
		setContentView(R.layout.main);
		/**
		 * デジタル出力
		 */
		mDigitalOutToggles = new ToggleButton[4];
		mDigitalOutToggles[0] = (ToggleButton) findViewById(R.id.digitalOut0);
		mDigitalOutToggles[1] = (ToggleButton) findViewById(R.id.digitalOut1);
		mDigitalOutToggles[2] = (ToggleButton) findViewById(R.id.digitalOut1);
		mDigitalOutToggles[3] = (ToggleButton) findViewById(R.id.digitalOut3);
		for (int i = 0; i < mDigitalOutToggles.length; i++) {
			mDigitalOutToggles[i].setOnCheckedChangeListener(mCheckedChangeListener);
		}
		
		/**
		 * アナログ出力
		 */
		mAnalogOutSeekBars = new SeekBar[4];
		mAnalogOutSeekBars[0] = (SeekBar)findViewById(R.id.analogOut0);
		mAnalogOutSeekBars[1] = (SeekBar)findViewById(R.id.analogOut1);
		mAnalogOutSeekBars[2] = (SeekBar)findViewById(R.id.analogOut1);
		mAnalogOutSeekBars[3] = (SeekBar)findViewById(R.id.analogOut3);
		for (int i = 0; i < mAnalogOutSeekBars.length; i++) {
			mAnalogOutSeekBars[i].setMax(255);
			mAnalogOutSeekBars[i].setProgress(0);
			mAnalogOutSeekBars[i].setOnSeekBarChangeListener(seekBarListener);
		}
		/**
		 * デジタル入力表示用View
		 */
		mSwitchStatuses = new TextView[4];
		mSwitchStatuses[0] = (TextView)findViewById(R.id.digitalIn0);
		mSwitchStatuses[1] = (TextView)findViewById(R.id.digitalIn1);
		mSwitchStatuses[2] = (TextView)findViewById(R.id.digitalIn2);
		mSwitchStatuses[3] = (TextView)findViewById(R.id.digitalIn3);
		
		/**
		 * アナログ入力表示用View
		 */
		mAnalogInValues = new TextView[4];
		mAnalogInValues[0] = (TextView)findViewById(R.id.analogIn0);
		mAnalogInValues[1] = (TextView)findViewById(R.id.analogIn1);
		mAnalogInValues[2] = (TextView)findViewById(R.id.analogIn2);
		mAnalogInValues[3] = (TextView)findViewById(R.id.analogIn3);
		
		/**
		 * メッセージハンドラ
		 * UIスレッドで処理を行う
		 */
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				byte[] data = (byte[])msg.obj;
				switch (msg.what) {
				case ArduinoProtocol.UPDATE_DIGITAL_STATE:
					if (data[1] == 1) {
						mSwitchStatuses[data[0]].setBackgroundColor(Color.RED);
					} else {
						mSwitchStatuses[data[0]].setBackgroundColor(Color.BLACK);
					}
					break;
				case ArduinoProtocol.UPDATE_ANALOG_STATE:
					final int value = composeInt(data[1], data[2]);
					Log.v(TAG, "Analog id, value = " + data[0] + ", " + value);
					mAnalogInValues[data[0]].setText(String.valueOf(value));
					break;
				default:
					break;
				}
			}
		};
	}

	@Override
	public void onResume() {
		super.onResume();
	}
 
	@Override
	public void onPause() {
		super.onPause();
	}
 
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private int composeInt(byte hi, byte lo) {
		return ((hi & 0xff) << 8) + (lo & 0xff);
	}
	
	/**
	 * トグルボタンリスナー
	 */
	private OnCheckedChangeListener mCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (mArduinoProtocol == null) {
				Log.w(TAG, "Not connected");
				return ;
			}
			switch (buttonView.getId()) {
			case R.id.digitalOut0:
				mArduinoProtocol.digitalWrite(0, isChecked);
				break;
			case R.id.digitalOut1:
				mArduinoProtocol.digitalWrite(1, isChecked);
				break;
			case R.id.digitalOut2:
				mArduinoProtocol.digitalWrite(2, isChecked);
				break;
			case R.id.digitalOut3:
				mArduinoProtocol.digitalWrite(3, isChecked);
				break;
			default:
				break;
			}
		}
	};
	
	/**
	 * シークバーリスナー
	 */
	private OnSeekBarChangeListener seekBarListener = new OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (mArduinoProtocol == null) {
				Log.w(TAG, "Not connected");
				return ;
			}
			
			switch (seekBar.getId()) {
			case R.id.analogOut0:
				mArduinoProtocol.analogWrite(0, progress);
				break;
			case R.id.analogOut1:
				mArduinoProtocol.analogWrite(1, progress);
				break;
			case R.id.analogOut2:
				mArduinoProtocol.analogWrite(2, progress);
				break;
			case R.id.analogOut3:
				mArduinoProtocol.analogWrite(3, progress);
				break;
			default:
				break;
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// do nothing
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// do nothing
		}
	};
}
