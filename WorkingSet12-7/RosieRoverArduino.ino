//global command ID's
//int CMD_DRIVE=100;

//global vars
int LEFT_MOTOR_DIR=2;
int RIGHT_MOTOR_DIR=12;
int LEFT_MOTOR_PWM=5;
int RIGHT_MOTOR_PWM=6;

int timeoutTime=2000; //allows 2000ms (2s) before determined connection is lost
unsigned long comTimeout=0;
int motorSpeedL;
int motorSpeedR;
int q1;
int q2;


void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);               //initial the Serial
  //Serial.begin(9600);
    pinMode(2,OUTPUT);
    pinMode(5,OUTPUT);
    pinMode(61,OUTPUT);
    pinMode(12,OUTPUT);

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
 
  if(Serial.available()){
    while(Serial.parseInt()!=-1){}
    comTimeout=millis()+timeoutTime; //comTimeout is 2 sec after last received command
    
    //set variables
    
    motorSpeedL=Serial.parseInt(); //left motor speed
    motorSpeedR=Serial.parseInt(); //right motor speed
    q1=Serial.parseInt();//placeholder
    q2=Serial.parseInt();//placeholder
    Serial.print(motorSpeedL,DEC);
    Serial.print(",");
    Serial.print(motorSpeedR,DEC);
    Serial.print(",");
    Serial.print(q1,DEC);
    Serial.print(",");
    Serial.print(q2,DEC);
    Serial.print(";");
  }
  //end of if

    //stop moving if safety conditions not met
    if(safetyCheck()==false){
      motorSpeedL=0;
      motorSpeedR=0;
    }
    
    //set motor directions
    if (motorSpeedL>=0){
        digitalWrite(LEFT_MOTOR_DIR,HIGH);
      }else{
        digitalWrite(LEFT_MOTOR_DIR,LOW);
      }
      if (motorSpeedR>=0){
        digitalWrite(RIGHT_MOTOR_DIR,HIGH);
      }else{
        digitalWrite(RIGHT_MOTOR_DIR,LOW);
      }
      
      //set motor speed
      //analogWrite(LEFT_MOTOR_PWM,abs(motorSpeedL));
      //analogWrite(RIGHT_MOTOR_PWM,abs(motorSpeedR));
      
      if(micros()%256<abs(motorSpeedL)){
      digitalWrite(LEFT_MOTOR_PWM,HIGH);
    }else{
       digitalWrite(LEFT_MOTOR_PWM,LOW);
     }
     if(micros()%256<abs(motorSpeedR)){
      digitalWrite(RIGHT_MOTOR_PWM,HIGH);
    }else{
       digitalWrite(RIGHT_MOTOR_PWM,LOW);
     }
       
      
  
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
    return false;
  }
  
  //check that it is safe to drive forward (if moving forwards). 
  
  //check that it is safe to back up (if moving backwards).
  
  //all checks were good
  return true;  
}


