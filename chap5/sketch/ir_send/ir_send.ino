
#include <Max3421e.h>
#include <EEPROM.h>

// デジタル入力ピンの定義
#define DIN0 6
#define DIN1 7
#define DIN2 8
#define DIN3 9

// 赤外線信号入出力
#define IR_IN DIN1
#define IR_OUT 3
#define MAX_LOOP_COUNT 10000

int irData[256];

int TV_POWER[] = {3478,1758,417,448,408,1296,439,453,413,425,442,423,444,422,435,457,410,456,411,454,413,452,415,451,416,449,418,420,437,1296,440,425,441,424,443,449,418,448,409,456,411,427,440,453,414,451,416,450,417,1315,410,455,412,426,441,452,415,423,444,448,408,430,437,455,411,427,440,1292,443,449,418,1314,411,1294,442,1318,418,1287,438,427,440,453,414,1291,445,448,409,1296,439,1293,443,1317,409,1296,439,426,441,1319,417,0};

void initPins()
{
  // デジタル入力ピンの初期化
  pinMode(IR_IN, INPUT);
  pinMode(IR_OUT, OUTPUT);
    
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
//  doReceiveIr();
  Serial.print("ready\n");
  delay(3000);
  Serial.print("go\n");
  sendIr(TV_POWER, sizeof(TV_POWER)/sizeof(TV_POWER[0]));
}


void sendIr(int *irData, int size) {
  Serial.print(size, DEC);
  Serial.print(" ");
  Serial.print(TV_POWER[0], DEC);
  Serial.print("\n");
  for (int cnt = 0; cnt < size; cnt++) {
    Serial.print(".");
    unsigned long len = irData[cnt];
    if (len == 0) break;      // 0なら終端。
    unsigned long startTime = micros();
    do {
      digitalWrite(IR_OUT, 0x1 - (cnt & 0x1)); // iが偶数なら赤外線ON、奇数なら0のOFFのまま
      delayMicroseconds(8);  // キャリア周波数38kHzでON/OFFするよう時間調整
      digitalWrite(IR_OUT, 0);
      delayMicroseconds(18);
    } while (long(startTime + len - micros()) > 0); // 送信時間に達するまでループ
  }
  Serial.print("OK\n");
}


// 赤外線リモコンの出力を受信し、シリアルポートに出力する
void doReceiveIr() {
  unsigned long cnt = 0;
  unsigned int previousValue;
  unsigned int currentValue;
  unsigned long currentTime;
  unsigned long previousTime = micros();
  
  int index = 0;

//  Serial.print("waitng\n");
  while (1) {
    // ON/OFF状態が変わるまでループする
    for (; cnt < MAX_LOOP_COUNT; cnt++) {
      if (digitalRead(IR_IN) != previousValue) {
        currentValue = digitalRead(IR_IN);
        break;
      }
    }
    if (cnt >= MAX_LOOP_COUNT) {
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
  Serial.print(index, DEC);
  Serial.print(")\n");
}


