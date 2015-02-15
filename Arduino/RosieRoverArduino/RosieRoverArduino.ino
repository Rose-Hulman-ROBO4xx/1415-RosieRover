//global command ID's
//int CMD_DRIVE=100;

//global vars
//int LEFT_MOTOR_DIR=2;
//int RIGHT_MOTOR_DIR=12;
#include <Servo.h>
const int pwPin = 11;
Servo leftWheel;
Servo rightWheel;
int LEFT_MOTOR_PWM=9;
int RIGHT_MOTOR_PWM=10;
int PAN_1=2;
int PAN_2=3;
int FIRE=4;
int TILT_1=7;
int TILT_2=8;
int SIGNAL_LED=12;
//int ULTRASONIC=11;
int MOTOR_STOP=90;
int safeDist=48;


int timeoutTime=1000; //allows 1000ms (2s) before determined connection is lost
unsigned long comTimeout=0;
unsigned int motorSpeedL;
unsigned int motorSpeedR;
int pan;
int tilt;
int fire;
int emergency;
int warning;
int have_signal;
int ignore_us; //ignore warning from ultrasonic sensor if 1
//variables needed to store values
long pulse, inches, cm;

void setup() {
  leftWheel.attach(LEFT_MOTOR_PWM);
  rightWheel.attach(RIGHT_MOTOR_PWM);
  // put your setup code here, to run once:
  Serial.begin(115200);               //initial the Serial
  //Serial.begin(9600);
    pinMode(PAN_1,OUTPUT);//PAN 1
    pinMode(PAN_2,OUTPUT); //PAN 2
    pinMode(FIRE,OUTPUT); //FIRE
    pinMode(TILT_1,OUTPUT); //TILT 2
    pinMode(TILT_2,OUTPUT); //FIRE
    pinMode(LEFT_MOTOR_PWM,OUTPUT); //LEFT MOTOR
    pinMode(RIGHT_MOTOR_PWM,OUTPUT); //RIGHT MOTOR
    pinMode(SIGNAL_LED,OUTPUT); //led for when connected to phone
    pinMode(pwPin, INPUT);

}

void loop() { 
  
  
  /////////////////////////////////////////////ultrasonic range finder////////////////////////////////////////////////////////////
   
    //Used to read in the pulse that is being sent by the MaxSonar device.
  //Pulse Width representation with a scale factor of 147 uS per Inch.

  pulse = pulseIn(pwPin, HIGH);
  //147uS per inch
  inches = pulse/147;
  /*
  //change inches to centimetres
  cm = inches * 2.54;
  
  Serial.print(inches);
  Serial.print("in, ");
  Serial.print(cm);
  Serial.print("cm");
  Serial.println();
  delay(500);
  */
  
  ///////////////////////////////////////////////Read BT input signal///////////////////////////////////////////////////////////////////////////////
  if(Serial.available()){
    have_signal=1;
    while(Serial.parseInt()!=-11111){}
    comTimeout=millis()+timeoutTime; //comTimeout is 2 sec after last received command
    
    //set variables
    emergency=Serial.parseInt(); //emergency stop
    warning=Serial.parseInt(); //warning lights
    motorSpeedL=Serial.parseInt(); //left motor speed
    motorSpeedR=Serial.parseInt(); //right motor speed
    fire=Serial.parseInt();//fire dart
    pan=Serial.parseInt();//pan turret
    tilt=Serial.parseInt();//tilt turret
    ignore_us=Serial.parseInt(); //ignore warning from ultrasonic

  }else{
    
    have_signal=0;
    /*
    emergency=1; //emergency stop if no signal
    warning=1; //warning lights if no signal
    motorSpeedL=MOTOR_STOP; //left motor speed
    motorSpeedR=MOTOR_STOP; //right motor speed
    fire=0;//fire dart
    pan=0;//pan turret
    tilt=0;//tilt turret
    ignore_us=0;
    */
  }

  //////////////////////////////////signal light//////////////////////////////////////////////////////////////////
  if(have_signal==1){
    digitalWrite(SIGNAL_LED,HIGH);
  }else{
    digitalWrite(SIGNAL_LED,LOW);
  }

    /////////////////////////////////////////stop moving if safety conditions not met/////////////////////////////
    if(safetyCheck()==false){
      //motorSpeedL=0;
      //motorSpeedR=0;
      motorSpeedL=MOTOR_STOP;
      motorSpeedR=MOTOR_STOP;
    }
    //////////////////////////////////////////////////turret//////////////////////////////////////////////////////
    //fire
    if(fire==1){
      digitalWrite(FIRE,HIGH);
    }else{
      digitalWrite(FIRE,LOW);
    }
    //pan
    if(pan==1){
       digitalWrite(PAN_1,HIGH);
       digitalWrite(PAN_2,LOW);
    }else if(pan==-1){
      digitalWrite(PAN_1,LOW);
       digitalWrite(PAN_2,HIGH);
    }else{
      digitalWrite(PAN_1,LOW);
       digitalWrite(PAN_2,LOW);
    }
    //tilt
    if(tilt==1){
       digitalWrite(TILT_1,HIGH);
       digitalWrite(TILT_2,LOW);
    }else if(tilt==-1){
      digitalWrite(TILT_1,LOW);
       digitalWrite(TILT_2,HIGH);
    }else{
      digitalWrite(TILT_1,LOW);
       digitalWrite(TILT_2,LOW);
    }

     
      /////////////////////////////////////////Drive Motors//////////////////////////////////////////////////////////////
      leftWheel.write(motorSpeedL);
      rightWheel.write(motorSpeedR);
}

/*
method holds all safety checks (stop button, sensors, 
lost connection, etc) and returns true if robot is OK to
move as last requested, false if requested movement is 
determined to be hazardous
*/
bool safetyCheck(){
  
  //check for lost connection
  if(millis()>comTimeout){ //went too long without hearing ping. connection lost?
    have_signal=0;
    return false;
    
  }
  if(emergency==1){
    return false;
  }
  if((ignore_us==0) && (inches<safeDist) && (motorSpeedL > MOTOR_STOP || motorSpeedR > MOTOR_STOP)){
    return false;
    Serial.write(inches);
  }
  
  //check that it is safe to drive forward (if moving forwards). 
  
  //check that it is safe to back up (if moving backwards).
  
  //all checks were good
  return true;  
}



