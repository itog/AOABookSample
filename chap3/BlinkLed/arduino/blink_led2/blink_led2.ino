// A0ピンにLEDを接続します
#define LED_PIN A0

//
// setup関数
//
void setup() {
  Serial.begin(115200);
  // ピンを出力に設定する
  pinMode(LED_PIN, OUTPUT);
}

//
// loop関数
//
void loop() {
  // HIGHを出力
  digitalWrite(LED_PIN, HIGH);
  delay(1);
  // LOWを出力
  digitalWrite(LED_PIN, LOW);
  delay(1);
  Serial.println("looping");
}

