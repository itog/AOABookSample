#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

#define  LED_PIN  A0

// AndroidAccessoryオブジェクト作成
AndroidAccessory acc("MyManufacturer",         // 製造者名
                "MyModel",                     // モデル名
                "This is a sample accessory",  // 説明文
                "1.0",                         // バージョン名
                "http://android.com",          // URL
                "0000000012345678");           // シリアル番号

void setup()
{
  // set communiation speed
  Serial.begin(115200);  pinMode(LED_PIN, OUTPUT);
  acc.powerOn();
}
 
void loop()
{
  byte msg[1];
  if (acc.isConnected()) {
    // Androidが接続されていたらデータを読み込む
    int len = acc.read(msg, sizeof(msg), 1);
    if (len > 0) {
      if (msg[0] == 1) {
        // LEDを点灯する
        digitalWrite(LED_PIN,HIGH);
      } else {
        // LEDを消灯する
        digitalWrite(LED_PIN,LOW);
      }
    }
  } else {
    // Androidが接続されてない場合はLEDを消灯する
    digitalWrite(LED_PIN , LOW);
  }
  delay(10);
}

