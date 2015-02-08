//global command ID's
//int CMD_DRIVE=100;

//global vars
const int numReadings = 7; //running average for ultrasonics
const int numSensors = 2; //how many sensors are there
int readings[numSensors][numReadings];
int readIndex = 0; //index of current reading
int total[numSensors] = {0, 0};
int dist[numSensors];
int lastReadTime = 0;

int LEFT_MOTOR_DIR = 9;
int RIGHT_MOTOR_DIR = 12;
int LEFT_MOTOR_PWM = 10;
int RIGHT_MOTOR_PWM = 11;
int ULTRASONIC[numSensors] = {A0, A1};
int ULTRASONIC_TRIGGER = 4;
int ALERTLED = 3;
int LED[numSensors] = {8, 7};
int BUZZER = 5;
int ESTOP = 2;

int timeoutTime = 2000; //allows 2000ms (2s) before determined connection is lost
int distanceThreshold = 24; //stop robot if within 24 inches of something

unsigned long comTimeout = 0;
int motorSpeedL;
int motorSpeedR;


void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);               //initial the Serial
  Serial.print("Initializing...");
  pinMode(LEFT_MOTOR_DIR, OUTPUT);
  pinMode(LEFT_MOTOR_PWM, OUTPUT);
  pinMode(RIGHT_MOTOR_PWM, OUTPUT);
  pinMode(RIGHT_MOTOR_DIR, OUTPUT);
  pinMode(BUZZER, OUTPUT);
  pinMode(ESTOP, INPUT);
  pinMode(ULTRASONIC_TRIGGER, OUTPUT);
  pinMode(ALERTLED, OUTPUT);
  for (int thisSensor = 0; thisSensor < numSensors; thisSensor++) {
    pinMode(ULTRASONIC[thisSensor], INPUT);
    pinMode(LED[thisSensor], OUTPUT);
    for (int thisReading = 0; thisReading < numReadings; thisReading++) {
      readings[thisSensor][thisReading] = 0;
    }
  }
  digitalWrite(ULTRASONIC_TRIGGER, HIGH);
  delay(1);
  digitalWrite(ULTRASONIC_TRIGGER, LOW);
  lastReadTime = millis();
  attachInterrupt(0, emergencyStop, FALLING);
  Serial.println("done.");
}

void loop() {

  /*
  starts this if-block when a command is sent
  Commands sent from website to android look like:
    "Status(x1,x2)"
  Commands sent from Android to Arduino look like:
    "x1,x2"

  x1=left motor speed
  x2=right motor speed
  */
  if (Serial.available()) {
    comTimeout = millis() + timeoutTime; //comTimeout is 2 sec after last received command

    //set variables
    motorSpeedL = Serial.parseInt(); //left motor speed
    motorSpeedR = Serial.parseInt(); //right motor speed

    //Serial.print("Speeds received: ");
    //Serial.print(motorSpeedL);
    //Serial.print(", ");
    //Serial.print(motorSpeedR);

  }
  //end of if

  //stop moving if safety conditions not met
  if (!safetyCheck()) {
    motorSpeedL = 0;
    motorSpeedR = 0;
  }

  //set motor directions
  if (motorSpeedL >= 0) {
    digitalWrite(LEFT_MOTOR_DIR, HIGH);
  } else {
    digitalWrite(LEFT_MOTOR_DIR, LOW);
  }
  if (motorSpeedR >= 0) {
    digitalWrite(RIGHT_MOTOR_DIR, HIGH);
  } else {
    digitalWrite(RIGHT_MOTOR_DIR, LOW);
  }

  //set motor speed
  analogWrite(LEFT_MOTOR_PWM, abs(motorSpeedL));
  analogWrite(RIGHT_MOTOR_PWM, abs(motorSpeedR));

}

/*
method holds all safety checks (stop button, sensors,
lost connection, etc) and returns true if robot is OK to
move as last requested, false if requested movement is
determined to be hazardous
*/
bool safetyCheck() {
  bool unsafe[numSensors];

  //Serial.print("Safety check: ");
  if (!digitalRead(ESTOP)) {
    digitalWrite(ALERTLED, HIGH);
    //Serial.println("E-Stop Active.");
    return false;
  }
  //Serial.print("E-Stop Off, ");

  // Running average of ultrasonic readings for rear sensor
  readUltrasonics();
  unsafe[0] = (dist[0] <= distanceThreshold) && (motorSpeedL < 0);
  unsafe[1] = (dist[1] <= distanceThreshold) && (motorSpeedR < 0);
  for (int i = 0; i < numSensors; i++) {
    if (unsafe[i]) {
      digitalWrite(ALERTLED, HIGH);
      //Serial.print("Sensor Stop: ");
      //Serial.println(i);
      return false;
    } else {
      //Serial.print("Sensor OK: ");
      //Serial.print(dist[i],DEC);
      //Serial.print(", ");
    }
  }
  digitalWrite(ALERTLED, LOW);

  //check for lost connection
  if (millis() > comTimeout) { //went too long without hearing ping. connection lost?
    //Serial.println("Comm timeout.");
    return false;
  } else {
    //Serial.print("Comm OK, ");
  }
  //Serial.println("passed.");
  return true;
}

void readUltrasonics() {
  while ((millis() - lastReadTime) < 50) {
  }
  for (int i = 0; i < numSensors; i++) {
    readings[i][readIndex] = analogRead(ULTRASONIC[i]);
    isort(readings[i],numReadings);
    //taking the median
    dist[i] = readings[i][numReadings/2];
    readIndex++;
    if (readIndex >= numReadings)
      readIndex = 0;
    // factor of 2 below is converting reading to inches
    
    if ( dist[i] <= distanceThreshold) {
      // back
      digitalWrite(LED[i], HIGH);
    } else {
      digitalWrite(LED[i], LOW);
    }
  }
  digitalWrite(ULTRASONIC_TRIGGER, HIGH);
  delay(1);
  digitalWrite(ULTRASONIC_TRIGGER, LOW);
  lastReadTime = millis();
}

void emergencyStop() {
  analogWrite(LEFT_MOTOR_PWM, 0);
  analogWrite(RIGHT_MOTOR_PWM, 0);
  digitalWrite(ALERTLED, HIGH);
  motorSpeedL = 0;
  motorSpeedR = 0;
  //Serial.println("E-Stop activated");
}

//insert sort function
void isort(int *a, int n){
// *a is an array pointer function
  for (int i = 1; i < n; ++i)
  {
    int j = a[i];
    int k;
    for (k = i - 1; (k >= 0) && (j < a[k]); k--)
    {
      a[k + 1] = a[k];
    }
    a[k + 1] = j;
  }
} 

//Mode function, returning the mode or median.
//Credit to Bill Gentles, Nov 12, 2010
//int mode(int *x,int n){ 
//  int i = 0;
//  int count = 0;
//  int maxCount = 0;
//  int mode = 0;
//  int bimodal;
//  int prevCount = 0;
//
//  while(i<(n-1)){
//    prevCount=count;
//    count=0;
//    while(x[i]==x[i+1]){
//      count++;
//      i++;
//    }
//    if(count>prevCount&count>maxCount){
//      mode=x[i];
//      maxCount=count;
//      bimodal=0;
//    }
//    if(count==0){
//      i++;
//    }
//    if(count==maxCount){//If the dataset has 2 or more modes.
//      bimodal=1;
//    }
//    if(mode==0||bimodal==1){//Return the median if there is no mode.
//      mode=x[(n/2)];
//    }
//    return mode;
//  }
//
//
//} 

