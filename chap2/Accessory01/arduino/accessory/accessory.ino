#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

// AndroidAccessoryオブジェクト作成
AndroidAccessory acc("MyManufacturer",         // 製造者名
                "MyModel",                     // モデル名
                "This is a sample accessory",  // 説明文
                "1.0",                         // バージョン名
                "http://android.com",          // URL
                "0000000012345678");           // シリアル番号

//

void setup()
{
  Serial.begin(115200); 
  acc.powerOn();  // USB HostコントローラON
}

void loop()
{
  // 接続確認
  if (acc.isConnected()) {
    Serial.println("Android connected");
  } else {
    Serial.println("Waiting for Android");
  }
  delay(10);
}

