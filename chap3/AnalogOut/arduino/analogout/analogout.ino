#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

#define  LED_PIN  2

// AndroidAccessoryオブジェクト作成
AndroidAccessory acc("MyManufacturer",         // 製造者名
                "MyModel",                     // モデル名
                "This is a sample accessory",  // 説明文
                "1.0",                         // バージョン名
                "http://android.com",          // URL
                "0000000012345678");           // シリアル番号

void setup()
{
  Serial.begin(115200);
  pinMode(LED_PIN, OUTPUT);
  acc.powerOn();
}
 
void loop()
{
  byte msg[0];
  if (acc.isConnected()) {
    int len = acc.read(msg, sizeof(msg), 1);
    // analogWriteで指定する値は0から255まで
    if (len > 0 && msg[0] > 0 && msg[0] < 256) {
        // 0-255でPWMのデューティー比を指定
        analogWrite(LED_PIN, msg[0]);
    }
  } else {
    digitalWrite(LED_PIN , 0);
  }
  delay(10);
}

