package aoabook.sample.chap5.smarthouse;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Push通信を受け取った時に呼ばれるレシーバ
 */
public class RemoteIntentReceiver extends BroadcastReceiver {
	static final String TAG = "RIReveiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG, "received");
		try {
			// Pushで送られてきたパラメータを取得する
			JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
			Intent i = new Intent(context, SmartHouseService.class);
			// パラメータを付加してサービスを起動
			i.putExtra("airconditioner", json.getString("airconditioner"));
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startService(i);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
