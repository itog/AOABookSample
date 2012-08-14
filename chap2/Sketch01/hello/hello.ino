//
// setup関数
// 起動時に一度だけ呼び出される：ここで初期化処理を行う
//
void setup() {
  Serial.begin(115200); // ボーレートの設定
}

//
// loop関数
// Arduino ボードが動作中、繰り返し呼び出される
//
void loop() {
  Serial.println("Hello World");
  delay(500);  // 500ms 停止
}

