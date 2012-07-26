package aoabook.sample.chap5.smarthouse;

import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.parse.Parse;
import com.parse.PushService;

public class SmartHouseActivity extends Activity implements OnClickListener {
	static final String TAG = "SmartHouse";
	
	//Parse上でアプリを作成し、取得するID、KEY
	private static final String PARSE_APP_ID = "TXflzbyo1LGlFnN4qVBosOzTD6il0C7CWlFqsx8Y";
	private static final String PARSE_CLIENT_KEY = "tgQ7KgaEVOh5EM17D31LBl5zfNag6gXP8MgqI1gC";
	
	private static final String PREF_CHANNEL_ID_KEY = "channel_id";
	
	private EditText mKeywordText;
	private ToggleButton mEnableToggle;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // ユニークなチャネルIDを生成して、表示
        mKeywordText = (EditText)findViewById(R.id.keywordText);
        mKeywordText.setText(getChannelId(this));
        
        mEnableToggle = (ToggleButton)findViewById(R.id.enableToggle);
        mEnableToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					// Push通信待受を開始する
					PushService.subscribe(SmartHouseActivity.this, getChannelId(SmartHouseActivity.this), SmartHouseActivity.class);
				} else {
					// Push通信の待受を停止する
					PushService.unsubscribe(SmartHouseActivity.this, getChannelId(SmartHouseActivity.this));
				}
			}
        });
        
        findViewById(R.id.onButton).setOnClickListener(this);
        findViewById(R.id.offButton).setOnClickListener(this);
        
        // Parseライブラリの初期化
        Parse.initialize(this, PARSE_APP_ID, PARSE_CLIENT_KEY);
    }

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	/**
	 * チャネルID用のユニークIDを取得する
	 * @param context
	 * @return
	 */
	private static String getChannelId(Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		String channelId = sharedPrefs.getString(PREF_CHANNEL_ID_KEY, null);		
	    if (channelId == null) {	    	
            channelId = "ch" + UUID.randomUUID().toString();
            Editor editor = sharedPrefs.edit();
            editor.putString(PREF_CHANNEL_ID_KEY, channelId);
            editor.commit();
        }
	    return channelId;
	}

	@Override
	public void onClick(View v) {
		Intent i = new Intent(this, SmartHouseService.class);
		switch(v.getId()) {
		case R.id.onButton:
			i.putExtra("airconditioner", "on");
			startService(i);
			break;
		case R.id.offButton:
			i.putExtra("airconditioner", "off");
			startService(i);
			break;
		default:
			break;
		}
	}
}
