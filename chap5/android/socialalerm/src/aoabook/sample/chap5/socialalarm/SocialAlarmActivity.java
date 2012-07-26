package aoabook.sample.chap5.socialalarm;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserStreamAdapter;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;


public class SocialAlarmActivity extends AOABaseActivity {
	static final String TAG = "SAlarm";
	
//		static String CONSUMER_KEY = "YOUR_CONSUMER_KEY";
//		static String CONSUMER_SECRET = "YOUR_CONSUMER_SECRET";
	// Twitter上でアプリ登録した際に得られるアプリ固有値
	static String CONSUMER_KEY = "NNqcfQDLqkmLZN8eHY9Ihw";
	static String CONSUMER_SECRET = "aPAIKTRZUtT37q5oZWEtVBJi5Zk74eABF7iWEqo";
	
	// Preferenceに保存するときのファイル名、キー名
	static String PREFERENCE_NAME = "twitter_oauth";
	static final String PREF_KEY_SECRET = "oauth_token_secret";
	static final String PREF_KEY_TOKEN = "oauth_token";
	
	// Twitter認証後にコールバックされるURL
	static final String CALLBACK_URL = "oauth://t4jsample";
	
	// OAuth認証のコールバックURLに含まれるOAuth認証コードのパラメータ名
	static final String IEXTRA_OAUTH_VERIFIER = "oauth_verifier";


	private static final int DELAY_TIME = 1000;
	
	private ToggleButton mEnableButton;
	private EditText mEditText;
	private TimePicker mTimePicker;

	private static SharedPreferences mSharedPreferences;
	
	private static Twitter mTwitter;
	private static TwitterStream mTwitterStream;
	private static RequestToken mRequestToken;
	private String mKeyword;
	
	// アラーム再生時間
	private int mDuration;
	// アラーム再生中フラグ
	private boolean mRinging;
	// ハンドラ
	private Handler mHandler = new Handler();
	// 停止要求フラグ
	private boolean mStopRequest;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mEditText = (EditText)findViewById(R.id.editText1);
		mTimePicker = (TimePicker)findViewById(R.id.timePicker1);
		mTimePicker.setIs24HourView(true);
		mEnableButton = (ToggleButton) findViewById(R.id.toggleButton1);
		mEnableButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (isConnected()) {
						// Twitter認証済みの場合
						
						createTwitterInstance();
						mKeyword = mEditText.getText().toString();
						// 起こして欲しい時間とキーワードをツイート
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									mTwitter.updateStatus(mTimePicker.getCurrentHour() + ":" + mTimePicker.getCurrentMinute()
											+ "に起して！このキーワードつけてメンションください→"
											+ mEditText.getText() + " " + System.currentTimeMillis());
								} catch (TwitterException e) {
									e.printStackTrace();
								}
							}
						}).start();
						
						// streaming APIでmention watch開始
						mTwitterStream.addListener(mUserStreamAdapter);
						mTwitterStream.user();
					} else {
						// Twitter認証されていない場合はOAuth認証を行う
						new Thread(new Runnable() {
							@Override
							public void run() {
								askOAuth();
							}
						}).start();
					}
				} else {
					// ストリーミングAPIの停止、アラームの停止
					if (mTwitterStream != null) {
						mTwitterStream.shutdown();
					}
					requestStopAlarm();
				}
			}
		});

		mSharedPreferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
		//
		// Twitterの認証画面からのコールバックIntentで呼ばれた時の処理
		//
		final Uri uri = getIntent().getData();
		// コールバックに指定したURLから始まるURLで呼ばれたことを確認
		if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					// 認証コードを取得
					String verifier = uri.getQueryParameter(IEXTRA_OAUTH_VERIFIER);
					try {
						// トークンを取得し、トークン、シークレットをPreferenceに保存
						AccessToken accessToken = mTwitter.getOAuthAccessToken(mRequestToken, verifier);
						Editor e = mSharedPreferences.edit();
						e.putString(PREF_KEY_TOKEN, accessToken.getToken());
						e.putString(PREF_KEY_SECRET, accessToken.getTokenSecret());
						e.commit();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
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
	/**
	 * Twitterと接続済みか？
	 * 
	 * @return
	 */
	private boolean isConnected() {
		return mSharedPreferences.getString(PREF_KEY_TOKEN, null) != null;
	}

	/**
	 * OAuth認証
	 */
	private void askOAuth() {
		// アプリに登録時に得られるCONSUMER_KEY、CONSUMER_SECRETを元に
		// OAuth認証用のを開く
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(CONSUMER_KEY);
		configurationBuilder.setOAuthConsumerSecret(CONSUMER_SECRET);
		Configuration configuration = configurationBuilder.build();
		mTwitter = new TwitterFactory(configuration).getInstance();

		try {
			// コールバックURLを指定
			mRequestToken = mTwitter.getOAuthRequestToken(CALLBACK_URL);
			// 認証用URLを開く
			this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mRequestToken.getAuthenticationURL())));
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Twitter APIを使うためのインスタンスを生成する
	 */
	private void createTwitterInstance() {
		String oauthAccessToken = mSharedPreferences.getString(PREF_KEY_TOKEN, "");
		String oAuthAccessTokenSecret = mSharedPreferences.getString(PREF_KEY_SECRET, "");

		ConfigurationBuilder confbuilder = new ConfigurationBuilder();
		Configuration conf = confbuilder
							.setOAuthConsumerKey(CONSUMER_KEY)
							.setOAuthConsumerSecret(CONSUMER_SECRET)
							.setOAuthAccessToken(oauthAccessToken)
							.setOAuthAccessTokenSecret(oAuthAccessTokenSecret)
							.build();
		mTwitter = new TwitterFactory(conf).getInstance();
		mTwitterStream = new TwitterStreamFactory(conf).getInstance();
	}

	/**
	 * StreamingAPIのリスナー
	 */
	UserStreamAdapter mUserStreamAdapter = new UserStreamAdapter() {
		@Override
		public void onStatus(Status status) {
			try {
				String name = mTwitterStream.getScreenName();
				String tweet = status.getText();
				Log.v(TAG, "@" + status.getUser().getScreenName() + " : " + tweet);
				// 自分宛のメンションかつ指定したキーワードを含む場合
				if (tweet.startsWith("@" + name) && tweet.contains(mKeyword)) {
					requestAlarm();
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (TwitterException e) {
				e.printStackTrace();
			}
		}
	};


	/**
	 * アラームを鳴らす
	 */
	private void ringAlarm(boolean on) {
		Log.v(TAG, "ring");
		mArduinoProtocol.digitalWrite(0, on);
	}

	/**
	 * タイマーの終了要求をする
	 */
	private void requestStopAlarm() {
		mStopRequest = true;
	}
	
	/**
	 * アラームの開始要求をする
	 * アラームが未開始の場合は10秒のアラームをスタートする
	 * アラームがすでに鳴っている場合は鳴っている時間を10秒延長する
	 */
	private void requestAlarm() {
		Log.v(TAG, "requestAlarm");

		// アラーム時間を10秒延長する
		mDuration += 10;
		if (!mRinging) {
			// ベルがなっている最中でなければベルを鳴らし、1秒毎にスレッドを起動する
			ringAlarm(true);
			mRinging = true;
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (mStopRequest) {
						Log.v(TAG, "force stop");
						mDuration = 0;
					}
					// アラーム時間でをデクリメントして、0になったら終了
					mDuration -= 1;
					if (mDuration <= 0) {
						ringAlarm(false);
						mRinging = false;
					} else {
						mHandler.postDelayed(this, DELAY_TIME);
					}
				}
			}, DELAY_TIME);
		} else {
			Log.v(TAG, "ringing");
		}
	}
}
