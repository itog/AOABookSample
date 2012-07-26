#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

// スイッチのポートを定義
#define POTENTIOMETER A0

// AndroidAccessoryオブジェクト作成
AndroidAccessory acc("MyManufacturer",         // 製造者名
                "MyModel",                     // モデル名
                "This is a sample accessory",  // 説明文
                "1.0",                         // バージョン名
                "http://android.com",          // URL
                "0000000012345678");           // シリアル番号

int counter;

void setup()
{
  Serial.begin(115200);
  Serial.print("\r\nStart");

  pinMode(POTENTIOMETER, INPUT);

  acc.powerOn();
}

void loop()
{
  int status;
  byte msg[2];

  if (acc.isConnected()) {
    // 高速でデータを送りすぎてしまうと受け取り側が間に合わないので間隔を調整する
    if (counter++ % 10) {
      // アナログ値を読み込む
      status = analogRead(POTENTIOMETER);
      // 読み込んだアナログ値をbyte配列に格納
      msg[0] = (status >> 8) & 0xff;
      msg[1] = status &0xff;
      // Androidに送信
      acc.write(msg, sizeof(msg));
    }
  }
  delay(1);
}

