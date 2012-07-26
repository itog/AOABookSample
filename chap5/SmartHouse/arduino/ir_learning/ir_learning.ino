
#include <Max3421e.h>

// デジタル入力ピンの定義
#define DIN0 6
#define DIN1 7
#define DIN2 8
#define DIN3 9

// 赤外線信号入出力
#define IR_IN DIN1
#define MAX_LOOP_COUNT 10000


void initPins()
{
  // デジタル入力ピンの初期化
  pinMode(IR_IN, INPUT);
    
  // 内部プルアップを有効化
  digitalWrite(IR_IN, HIGH);
}

void setup()
{
  Serial.begin(115200);
  Serial.print("\r\nStart");

  // 各入出力ピンの初期化を行う
  initPins();
}


void loop()
{
  doReceiveIr();
  delay(10);
}


// 赤外線リモコンの出力を受信し、シリアルポートに出力する
void doReceiveIr() {
  unsigned int previousValue;
  unsigned int currentValue;
  unsigned long currentTime;
  unsigned long previousTime = micros();
  
  int index = 0;
  unsigned long i = 0;
  while (1) {
    // ON/OFF状態が変わるまでループする
    for (; i < MAX_LOOP_COUNT; i++) {
      if (digitalRead(IR_IN) != previousValue) {
        currentValue = digitalRead(IR_IN);
        break;
      }
    }
    if (i >= MAX_LOOP_COUNT) {
      break;
    }

    currentTime = micros();
    Serial.print(currentTime - previousTime, DEC);
    Serial.print(",");
    previousValue = currentValue;
    previousTime = currentTime;
    index++;
  }
  Serial.print("|(");
  Serial.print(index, DEC); // 受信データ数
  Serial.print(")\n");
}


