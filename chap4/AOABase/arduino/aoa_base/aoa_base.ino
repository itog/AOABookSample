
#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

// デジタル出力ピンの定義
#define DOUT0 2
#define DOUT1 3
#define DOUT2 4
#define DOUT3 5

// デジタル入力ピンの定義
#define DIN0 6
#define DIN1 7
#define DIN2 8
#define DIN3 9

// アナログデジタル出力ピンの定義
#define AOUT0 10
#define AOUT1 11
#define AOUT2 12
#define AOUT3 13

// アナログデジタル出力入力ピンの定義
#define AIN0 A0
#define AIN1 A1
#define AIN2 A2
#define AIN3 A3


// プロトコル定義
#define START_BYTE 0x7f
#define CMD_DIGITAL_WRITE 0x0
#define CMD_ANALOG_WRITE 0x01
#define UPDATE_DIGITAL_STATE 0x40
#define UPDATE_ANALOG_STATE 0x41
#define MAX_MSG_SIZE 6

AndroidAccessory acc("MyManufacturer",
		     "OpenAccessoryBase",
		     "Android Open Accessory basic implementation",
		     "1.0",
		     "http://android.com",
		     "0000000012345678");

// デジタル入力ピンの状態を保持
byte dinValues[4];
// メッセージバッファ
byte msg[MAX_MSG_SIZE];

void initPins()
{
  // デジタル出力ピンの初期化
  pinMode(DOUT0, OUTPUT);
  pinMode(DOUT1, OUTPUT);
  pinMode(DOUT2, OUTPUT);
  pinMode(DOUT3, OUTPUT);
  digitalWrite(DOUT0, LOW);
  digitalWrite(DOUT1, LOW);
  digitalWrite(DOUT2, LOW);
  digitalWrite(DOUT3, LOW);

  // アナログ出力ピンの初期化
  digitalWrite(AOUT0, 0);
  digitalWrite(AOUT1, 0);
  digitalWrite(AOUT2, 0);
  digitalWrite(AOUT3, 0);
  pinMode(AOUT0, OUTPUT);
  pinMode(AOUT1, OUTPUT);
  pinMode(AOUT2, OUTPUT);
  pinMode(AOUT3, OUTPUT);

  // デジタル入力ピンの初期化
  pinMode(DIN0, INPUT);
  pinMode(DIN1, INPUT);
  pinMode(DIN2, INPUT);
  pinMode(DIN3, INPUT);
    
  // 内部プルアップを有効化
  digitalWrite(DIN0, HIGH);
  digitalWrite(DIN1, HIGH);
  digitalWrite(DIN2, HIGH);
  digitalWrite(DIN3, HIGH);

  // アナログ入力ピンの初期化
  pinMode(AIN0, INPUT);
  pinMode(AIN1, INPUT);
  pinMode(AIN2, INPUT);
  pinMode(AIN3, INPUT);
}



void setup()
{
  Serial.begin(115200);
  Serial.print("\r\nStart");

  // 各入出力ピンの初期化を行う
  initPins();

  // デジタル入力ピンの値を取得
  dinValues[0] = digitalRead(DIN0);
  dinValues[1] = digitalRead(DIN1);
  dinValues[2] = digitalRead(DIN2);
  dinValues[3] = digitalRead(DIN3);
    
  // アクセサリの電源ON
  acc.powerOn();
}


  int count;

void loop()
{
  int b = 0;
  uint16_t val = 0;

  if (acc.isConnected()) {
    //
    // 受信処理
    //
    int len = acc.read(msg, sizeof(msg), 1);
    int i;
    if (msg[0] == START_BYTE && len >= msg[1]) {
      Serial.print("receive cmd:");
      Serial.print(msg[2], HEX);
      Serial.print("\r\n");

      switch (msg[2]) {
      case CMD_DIGITAL_WRITE:
        switch (msg[3]) {
        case 0x0:
          digitalWrite(DOUT0, msg[4] ? HIGH : LOW);
          break;
        case 0x1:
          digitalWrite(DOUT1, msg[4] ? HIGH : LOW);
          break;
        case 0x2:
          digitalWrite(DOUT2, msg[4] ? HIGH : LOW);
          break;
        case 0x3:
          digitalWrite(DOUT3, msg[4] ? HIGH : LOW);
          break;
        default:
          break;
        }
        break;
  
      case CMD_ANALOG_WRITE:
        switch (msg[3]) {
        case 0x0:
          analogWrite(AOUT0, msg[4]);
          break;
        case 0x1:
          analogWrite(AOUT1, msg[4]);
          break;
        case 0x2:
          analogWrite(AOUT2, msg[4]);
          break;
        case 0x3:
          analogWrite(AOUT3, msg[4]);
          break;
        default:
          break;
        }
	break;

      default:
	break;
      }
    }

    //
    // デジタル入力情報送信
    //
    msg[0] = START_BYTE;
    msg[1] = 5;
    msg[2] = UPDATE_DIGITAL_STATE;
    b = digitalRead(DIN0);
    if (b != dinValues[0]) {
      msg[3] = 0; //id
      msg[4] = b ? 0 : 1;
      acc.write(msg, 5);
      dinValues[0] = b;
    }
    b = digitalRead(DIN1);
    if (b != dinValues[1]) {
      msg[3] = 1;
      msg[4] = b ? 0 : 1;
      acc.write(msg, 5);
      dinValues[1] = b;
    }
    b = digitalRead(DIN2);
      if (b != dinValues[2]) {
        msg[3] = 2;
        msg[4] = b ? 0 : 1;
        acc.write(msg, 5);
        dinValues[2] = b;
    }
    b = digitalRead(DIN3);
    if (b != dinValues[3]) {
      msg[3] = 3;
      msg[4] = b ? 0 : 1;
      acc.write(msg, 5);
      dinValues[3] = b;
    }

    //
    // アナログ入力情報送信
    //
    msg[0] = START_BYTE;
    msg[1] = 6;
    msg[2] = UPDATE_ANALOG_STATE;
    switch (count++ % 50) {
    case 10:
      val = analogRead(AIN0);
      msg[3] = 0x0;
      msg[4] = val >> 8;
      msg[5] = val & 0xff;
      acc.write(msg, 6);
      break;
    case 20:
      val = analogRead(AIN1);
      msg[3] = 0x1;
      msg[4] = val >> 8;
      msg[5] = val & 0xff;
      acc.write(msg, 6);
      break;
    case 30:
      val = analogRead(AIN2);
      msg[3] = 0x2;
      msg[4] = val >> 8;
      msg[5] = val & 0xff;
      acc.write(msg, 6);
      break;
    case 40:
      val = analogRead(AIN3);
      msg[3] = 0x3;
      msg[4] = val >> 8;
      msg[5] = val & 0xff;
      acc.write(msg, 6);
      break;
    default:
      break;
    }
  } else {
    // 接続が切れた場合、出力値をリセットする
    digitalWrite(DOUT0, LOW);
    digitalWrite(DOUT1, LOW);
    digitalWrite(DOUT2, LOW);
    digitalWrite(DOUT3, LOW);
    analogWrite(AOUT0, 0);
    analogWrite(AOUT1, 0);
    analogWrite(AOUT2, 0);
    analogWrite(AOUT3, 0);
  }
    
  delay(10);
}


