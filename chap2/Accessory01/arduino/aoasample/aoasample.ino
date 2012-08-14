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
  byte receive_msg[1];
  byte send_msg[1];
  send_msg[0] = 'b';
  if (acc.isConnected()) {
    // メッセージを読み込む
    int len = acc.read(receive_msg, sizeof(receive_msg), 1);
    // メッセージがaであれば
    if (len > 0 && receive_msg[0] == 'a') {
        // シリアルポート出力
        Serial.println("Get message from Android!");
        // Androidに応答メッセージを送る
        acc.write(send_msg, sizeof(send_msg));
    }
  }
  delay(10);
}

