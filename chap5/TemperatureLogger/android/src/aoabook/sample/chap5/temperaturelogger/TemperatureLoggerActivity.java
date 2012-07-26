package aoabook.sample.chap5.temperaturelogger;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ToggleButton;

public class TemperatureLoggerActivity extends Activity {
	static final String TAG = "TemperatureLogger";
	static final String ACTION_POST = "aoabook.sample.chap5.temeraturelogger.POST_DATA";

	private ToggleButton mToggle;
	private EditText mTemperatureText;

	// インターバルの最小時間単位
	static final int TIME_MIN = 60 * 1000;
	private PendingIntent mPendingIntent;
	private AlarmManager mAlermManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mTemperatureText = (EditText)findViewById(R.id.intervalText);
		mToggle = (ToggleButton) findViewById(R.id.toggle);
		mToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!isChecked) {
					mAlermManager.cancel(mPendingIntent);
				} else {
					// 
					Context c = TemperatureLoggerActivity.this.getApplicationContext();
					Intent i = new Intent(c, AlarmReceiver.class);
					mPendingIntent = PendingIntent.getBroadcast(c, 0, i, 0);
					mAlermManager = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
					mAlermManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), Integer.valueOf(mTemperatureText.getText().toString()) * TIME_MIN, mPendingIntent);
				}
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
}