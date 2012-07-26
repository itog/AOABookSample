package aoabook.sample.chap5.smarthouse;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class SmartHouseService extends AOABaseService {
	static final String TAG = "SmartHouseService";
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override 
	public void onStart(Intent i, int id) {
		super.onStart(i, id);
		Log.v(TAG, "onStart");
		String cmd = i.getStringExtra("airconditioner");
		if (cmd.equals("on")) {
			Toast.makeText(this, "エアコンON", Toast.LENGTH_SHORT).show();
			//mArduinoAccessory.airconPower(true);
		} else if (cmd.equals("off")) {
			Toast.makeText(this, "エアコンOFF", Toast.LENGTH_SHORT).show();
			//mArduinoAccessory.airconPower(false);
		}		
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
