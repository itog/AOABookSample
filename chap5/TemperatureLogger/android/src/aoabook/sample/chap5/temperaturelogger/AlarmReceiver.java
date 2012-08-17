package aoabook.sample.chap5.temperaturelogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
	static final String TAG = "Receiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		Toast.makeText(context, "Received Alarm", Toast.LENGTH_SHORT).show();
		
		// TemperatureLogegrServiceを開始
		Intent i = new Intent(context, TemperatureLoggerService.class);
		context.startService(i);
	}
}
