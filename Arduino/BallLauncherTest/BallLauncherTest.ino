//ball launcher stuff
int LOAD_BALL_MOTOR_PWM = 10;
int FLYWHEEL_MOTOR_PWM = 11;
const int SHOOTER_DUTY_CYCLE = 255;
const int SHOOTER_STOP = 0;
int READY_SWITCH = 22;
const int TURRET_EMPTY = 0;
const int TURRET_READY = 1;
const int TURRET_FIRING = 2;
int turretState = TURRET_EMPTY;
boolean fire = false;
int debounceDelay = 30;

void setup() {
  Serial.begin(9600);

  pinMode(LOAD_BALL_MOTOR_PWM, OUTPUT); //Ball loading mechanism motor
  pinMode(FLYWHEEL_MOTOR_PWM, OUTPUT); //Flywheel motors
  pinMode(READY_SWITCH, INPUT_PULLUP); //Ready switch (low after a ball is fired, high if a ball is loaded

  Serial.println("Begin!");
}

void loop() {
  if (turretState == TURRET_EMPTY && !digitalRead(READY_SWITCH)) {
    delay(debounceDelay);
    if (!digitalRead(READY_SWITCH)) {
      // was empty, but now loadedi
      turretState = TURRET_READY;
      Serial.println("     Ready");
    }
  }

  if (turretState == TURRET_READY && digitalRead(READY_SWITCH)) {
    delay(debounceDelay);
    if (digitalRead(READY_SWITCH)) {
      // was loaded, but now empty
      turretState = TURRET_EMPTY;
      Serial.println("Empty");
    }
  }
  
  if (fire && turretState != TURRET_READY) {
    fire = false;
    Serial.println("Empty               Cannot fire.");
  }

  if (fire && turretState == TURRET_READY) {
    //Launch requested and ball loaded
    fire = false;
    turretState = TURRET_FIRING;
    Serial.println("          Firing");
    analogWrite(LOAD_BALL_MOTOR_PWM, SHOOTER_DUTY_CYCLE);
    analogWrite(FLYWHEEL_MOTOR_PWM, SHOOTER_DUTY_CYCLE);
  }

  if (turretState == TURRET_FIRING && digitalRead(READY_SWITCH)) {
    delay(debounceDelay);
    if (digitalRead(READY_SWITCH)) {
      //Was firing, switch goes low (firing done)
      turretState = TURRET_EMPTY;
      Serial.println("Empty           Done");
      analogWrite(LOAD_BALL_MOTOR_PWM, SHOOTER_STOP);
      analogWrite(FLYWHEEL_MOTOR_PWM, SHOOTER_STOP);
    }
  }
}

void serialEvent() {
  while (Serial.available()) {
    // get the new byte:
    char inChar = (char)Serial.read();
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (inChar == 'f') {
      fire = true;
    }
  }
}
