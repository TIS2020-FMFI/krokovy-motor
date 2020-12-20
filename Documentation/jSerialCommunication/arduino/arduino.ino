int YELLOW = 2;
int RED = 3;

void setup() {
  Serial.begin(9600);
  pinMode(YELLOW, OUTPUT);
  pinMode(RED, OUTPUT);
}

void loop() {
  while(true){
    if(Serial.available()){
      char c = Serial.read();
      if(c == '!') break;
    }
    Serial.print("#");
    digitalWrite(YELLOW, HIGH);
    delay(500);
    digitalWrite(YELLOW, LOW);
    delay(500);
  }
  int motor = 0;
  while(true){
    if(Serial.available()){
      char c = Serial.read();
      if (c == '+'){
        motor++;
        digitalWrite(RED, HIGH);
        delay(500);
        digitalWrite(RED, LOW);
        delay(500);
      }
      else if (c == '-') {
        motor--;
        digitalWrite(RED, HIGH);
        delay(500);
        digitalWrite(RED, LOW);
        delay(500);
      }
      else if(c == '!'){
        Serial.print("#");
        digitalWrite(YELLOW, HIGH);
        delay(500);
        digitalWrite(YELLOW, LOW);
        delay(500);
      }
     }
  }
}
