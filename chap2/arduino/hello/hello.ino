
//
// setup関数
//
void setup() {
  Serial.begin(9600);
  pinMode(0, OUTPUT);
}

//
// loop関数
//
void loop() {
  Serial.println("Hello World");
  digitalWrite(0, HIGH);
  delay(500);
  digitalWrite(0, LOW);
  delay(500);
}
