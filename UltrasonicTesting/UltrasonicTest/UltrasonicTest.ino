int redLED = 3;
int grnLED = 2;
int buzzer = 11;
int ultrasonicPin = 0;
int sensorValue = 0;
int distance = 0;
int grnThreshold = 24;
int redThreshold = 12;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  Serial.println("Begin Test");
  pinMode(grnLED, OUTPUT);
  pinMode(redLED, OUTPUT);
  pinMode(buzzer, OUTPUT);

}

void loop() {
  // put your main code here, to run repeatedly:
  sensorValue = analogRead(ultrasonicPin);
  Serial.print("Reading: ");
  Serial.print(sensorValue, DEC);
  distance = sensorValue / 2;
  Serial.print(", Distance: ");
  Serial.print(distance, DEC);
  Serial.println(" inches");
  if (distance < redThreshold) {
    digitalWrite(redLED, HIGH);
    digitalWrite(grnLED, LOW);
  } else if (distance < grnThreshold) {
    digitalWrite(grnLED, HIGH);
    digitalWrite(redLED, LOW);
  } else {
    digitalWrite(grnLED, LOW);
    digitalWrite(redLED, LOW);
  }
  delay(100);
}
