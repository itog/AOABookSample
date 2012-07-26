package aoabook.sample.chap4.concierge;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

public class ConciergeActivity extends AOABaseActivity implements RecognitionListener {
	private SpeechRecognizer mSpeechRecognizer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
		mSpeechRecognizer.setRecognitionListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		startVoiceRecognition();
	}

	@Override
	public void onPause() {
		super.onPause();
		
		mSpeechRecognizer.cancel();
	}

	/**
	 * 音声認識を開始する
	 */
	void startVoiceRecognition() {
		Log.v("", "startListening");
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
		mSpeechRecognizer.startListening(intent);
	}

	@Override
	public void onBeginningOfSpeech() {
	}

	@Override
	public void onBufferReceived(byte[] buffer) {
	}

	@Override
	public void onEndOfSpeech() {
	}

	@Override
	public void onError(int error) {
		// エラーの場合は再度音声認識を開始
		startVoiceRecognition();
	}

	@Override
	public void onEvent(int eventType, Bundle params) {
	}

	@Override
	public void onPartialResults(Bundle results) {
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
	}

	@Override
	public void onResults(Bundle results) {
		// 音声認識が行われた場合にその結果が渡される
		ArrayList<String> strings = results
				.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

		if (mArduinoProtocol != null) {
			// 結果は文字列配列で渡されるので、その中にコマンドがあるか
			if (strings.contains("暑い")) {
				Toast.makeText(ConciergeActivity.this, "扇風機つけます！", Toast.LENGTH_SHORT).show();
				mArduinoProtocol.digitalWrite(0, true);
			} else if (strings.contains("暗い")) {
				Toast.makeText(ConciergeActivity.this, "ライトつけます！", Toast.LENGTH_SHORT).show();
				mArduinoProtocol.digitalWrite(1, true);
			} else if (strings.contains("停止")) {
				Toast.makeText(ConciergeActivity.this, "停止します！", Toast.LENGTH_SHORT).show();
				mArduinoProtocol.digitalWrite(0, false);
				mArduinoProtocol.digitalWrite(1, false);
			} else {
				Toast.makeText(ConciergeActivity.this, strings.toString(), Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(ConciergeActivity.this, "Arduinoを接続して下さい", Toast.LENGTH_SHORT).show();
		}
		startVoiceRecognition();
	}

	@Override
	public void onRmsChanged(float rmsdB) {
	}
}
