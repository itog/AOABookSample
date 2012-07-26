package aoabook.sample.chap5.temperaturelogger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class TemperatureLoggerService extends AOABaseService {
	static final String TAG = "TemperatureLogger";
	
	// POSTするサーバのURL
	static final String TEMPERTUER_SERVER_URL = "http://tlogs.herokuapp.com/tlogs";
	// 送信リクエストフラグ
	private boolean mPostRequest;
	
	@Override
	public void onCreate() {
		super.onCreate();
		setHandler(mHandler);
	}
	
	@Override 
	public void onStart(Intent i, int id) {
		super.onStart(i, id);
		mPostRequest = true;
	}
	
	/**
	 * メッセージハンドラ
	 * UIスレッドで処理を行う
	 */
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			byte[] data = (byte[])msg.obj;
			switch (msg.what) {
			case ArduinoProtocol.UPDATE_ANALOG_STATE:
				if (data[0] == 0) {
					final double t = getTemperatureByRawdata(composeInt(data[1], data[2]));
					if (mPostRequest) {
						Toast.makeText(TemperatureLoggerService.this, "POST temperature = " + t, Toast.LENGTH_SHORT).show();
						
						new Thread(new Runnable() {
							@Override
							public void run() {
								postData(t);
							}
						}).start();
						mPostRequest = false;
					}
				}
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 温度データをサーバにPostする
	 * @param temperature
	 */
	public void postData(double temperature) {
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(TEMPERTUER_SERVER_URL);
			urlConnection = (HttpURLConnection) url.openConnection();
			String data = "[tlog][temperature]=" + String.valueOf(temperature);
			// POST可能にする
			urlConnection.setDoOutput(true);
			OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
			out.write(data.getBytes());
			out.flush();
			out.close();

			// レスポンスを取得する
			InputStream in = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            StringBuffer strBuff = new StringBuffer();
            String temp = null;
            while((temp = br.readLine()) != null) {
                strBuff.append(temp);
            }
            Log.v(TAG, strBuff.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
	}
		
	private int composeInt(byte hi, byte lo) {
		return ((hi & 0xff) << 8) + (lo & 0xff);
	}
	
	// アナログ入力からのデータを温度に変換する
	public double getTemperatureByRawdata(int adcData) {

		double voltage = adcData * 4.9;

		double voltageAtZero = 400;
		double temperatureCoefficient = 19.5;
		double temperature = ((double) voltage - voltageAtZero) / temperatureCoefficient;
		return temperature;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
