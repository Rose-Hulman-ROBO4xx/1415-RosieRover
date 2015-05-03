//global command ID's
//int CMD_DRIVE=100;

//global vars
//int LEFT_MOTOR_DIR=2;
//int RIGHT_MOTOR_DIR=12;
#include <Servo.h>
const int pwPin = 11;
Servo leftWheel;
Servo rightWheel;
int LEFT_MOTOR_PWM=8;
int RIGHT_MOTOR_PWM=9;
int PAN_1=22;
int PAN_2=23;
int FIRE=26;
//int PRIME_CANNON=
int TILT_1=24;
int TILT_2=25;
int SIGNAL_LED=27;
int XBEE_RX=19;
int XBEE_TX=18;
//int ULTRASONIC=11;
int MOTOR_STOP=90;
int safeDist=48;
int controllerState[23];
bool controllerSignal=false;
bool controllerOverride=false;
int blockVideo = 0;


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

//controller stuff
String inputString = "";         // a string to hold incoming data
boolean stringComplete = false;  // whether the string is complete
long controllerLastSignal=0;
int controllerTimeout=3000;
int controllerValues[23];
int OVERRIDE_BUTTON=13;
int controllerMotorLeft;
int controllerMotorRight;
int stickMidVal=127;
int stickDeadRange=15;


void setup() {
  leftWheel.attach(LEFT_MOTOR_PWM);
  rightWheel.attach(RIGHT_MOTOR_PWM);
  // put your setup code here, to run once:
  Serial.begin(115200);               //initial the Serial
  //Serial.begin(9600);
  Serial1.begin(9600);
    pinMode(PAN_1,OUTPUT);//PAN 1
    pinMode(PAN_2,OUTPUT); //PAN 2
    pinMode(FIRE,OUTPUT); //FIRE
    pinMode(TILT_1,OUTPUT); //TILT 2
    pinMode(TILT_2,OUTPUT); //FIRE
    pinMode(LEFT_MOTOR_PWM,OUTPUT); //LEFT MOTOR
    pinMode(RIGHT_MOTOR_PWM,OUTPUT); //RIGHT MOTOR
    pinMode(SIGNAL_LED,OUTPUT); //led for when connected to phone
    pinMode(pwPin, INPUT);
    pinMode(XBEE_RX,INPUT);
    pinMode(XBEE_TX,OUTPUT);


    //inputString.reserve(200);
}

void loop() { 
  
  
  /////////////////////////////////////////////ultrasonic range finder////////////////////////////////////////////////////////////
   
    //Used to read in the pulse that is being sent by the MaxSonar device.
  //Pulse Width representation with a scale factor of 147 uS per Inch.
  //time limit set at 30ms (200in) otherwise it causes a big delay that interferes with communications
  pulse = pulseIn(pwPin, HIGH,30);
//  //147uS per inch
  if(pulse==0){
    inches=200;
  }else{
    inches = pulse/147;
  }

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
  
  ////////////////////////////////////////////commands from controller////////////////////////////////////////////////////
 //"J2","J1",NULL,"S2","S1","UP","LEFT","DOWN","RIGHT","1","4","2","3","RZ1","RZ2","LZ1","LZ2", RB, LB , RY, RX, LY, LX};

 //button[13] =RZ1 to override
 if(controllerValues[OVERRIDE_BUTTON]==1){
   //Serial.println("Override received");
   controllerOverride=true;
   controllerMotorLeft=MOTOR_STOP;
   controllerMotorRight=MOTOR_STOP;
   
   //drive control with right stick. x=[20], y=[19]
   if(controllerValues[20]>stickMidVal + stickDeadRange || controllerValues[20]< stickMidVal-stickDeadRange){
     controllerMotorLeft += 90*(1.0*controllerValues[20]/stickMidVal - 1.0);
     controllerMotorRight -= 90*(1.0*controllerValues[20]/stickMidVal - 1.0);
   }
   if(controllerValues[19]>stickMidVal + stickDeadRange || controllerValues[19]< stickMidVal-stickDeadRange){
     controllerMotorLeft += 90*(controllerValues[19]/stickMidVal - 1);
     controllerMotorRight += 90*(controllerValues[19]/stickMidVal - 1);
   }
   
   controllerMotorLeft=constrain(controllerMotorLeft,0,180);
   controllerMotorRight=constrain(controllerMotorRight,0,180);
//   Serial.print(controllerMotorLeft);
//   Serial.print(", ");
//   Serial.println(controllerMotorRight);
   
   
   //prime cannon spinners "LZ1" = [15]
//   if(controllerValues[15]==1){
//     digitalWrite(PRIME_CANNON,HIGH);
//   }else{
//     digitalWrite(PRIME_CANNON,LOW);
//   }

   //fire cannon, "1" button = [9]
   if(controllerValues[9]==1){
     digitalWrite(FIRE,HIGH);
   }else{
     digitalWrite(FIRE,LOW);
   } 
   
   
 }else{
   controllerOverride=false;
 }

//turn video on and off
   if(controllerValues[5]==1){
     blockVideo=0;
   }else if(controllerValues[7]==1){
     blockVideo=1;
   }
  
  
  
  ///////////////////////////////////////////////Read BT input signal///////////////////////////////////////////////////////////////////////////////
//   if(Serial.available()){
//    have_signal=1;
//    while(Serial.parseInt()!=-11111){}
//    comTimeout=millis()+timeoutTime; //comTimeout is 2 sec after last received command
//    
//    //set variables
//    emergency=Serial.parseInt(); //emergency stop
//    warning=Serial.parseInt(); //warning lights
//    motorSpeedL=Serial.parseInt(); //left motor speed
//    motorSpeedR=Serial.parseInt(); //right motor speed
//    fire=Serial.parseInt();//fire dart
//    pan=Serial.parseInt();//pan turret
//    tilt=Serial.parseInt();//tilt turret
//    ignore_us=Serial.parseInt(); //ignore warning from ultrasonic
//    
//    //tell phone to tell website to block video if (or stop camera) if signal sent from controller
////    if(blockVideo){
////      Serial.write("block video");
////    }else{
////      Serial.write("allow video");
////    }
//
//  }else{
//    
//    have_signal=0;
//    /*
//    emergency=1; //emergency stop if no signal
//    warning=1; //warning lights if no signal
//    motorSpeedL=MOTOR_STOP; //left motor speed
//    motorSpeedR=MOTOR_STOP; //right motor speed
//    fire=0;//fire dart
//    pan=0;//pan turret
//    tilt=0;//tilt turret
//    ignore_us=0;
//    */
//  }
  
  //////////////////////////////////signal light//////////////////////////////////////////////////////////////////
  if(have_signal==1){
    digitalWrite(SIGNAL_LED,HIGH);
  }else{
    digitalWrite(SIGNAL_LED,LOW);
  }


   
    
    /////////////////////////////////////////stop moving if safety conditions not met/////////////////////////////
    if(safetyCheck()==false && controllerOverride==false){
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
      if(controllerOverride==true){
//        Serial.print(controllerMotorLeft);
//        Serial.print(", ");
//        Serial.println(controllerMotorRight);
        leftWheel.write(controllerMotorLeft);
        rightWheel.write(controllerMotorRight);
      }else if(blockVideo==0){
        leftWheel.write(motorSpeedL);
        rightWheel.write(motorSpeedR);
//        Serial.print(motorSpeedL);
//        Serial.print(", ");
//        Serial.println(motorSpeedR);
      }else{
        leftWheel.write(MOTOR_STOP);
        rightWheel.write(MOTOR_STOP);
      }
}

/*
method holds all safety checks (stop button, sensors, 
lost connection, etc) and returns true if robot is OK to
move as last requested, false if requested movement is 
determined to be hazardous
*/
bool safetyCheck(){
  
  //check for lost connection to website
  if(millis()>comTimeout){ //went too long without hearing ping. connection lost?
    have_signal=0;
    return false;
    
  }
  //check lost connection to controller
  if(millis()>controllerLastSignal+controllerTimeout){
    
     for(int i=0;i<17;i++){
       controllerValues[i]=0;
     }
     for(int i=17;i<19;i++){
       controllerValues[i]=255;
     }
     for(int i=19;i<23;i++){
       controllerValues[i]=125;
     }
    return false;
  }
  
  //emergency
  if(emergency==1){
    return false;
  }
  if((ignore_us==0) && (inches<safeDist) && (motorSpeedL > MOTOR_STOP || motorSpeedR > MOTOR_STOP)){
    return false;
    //Serial.write(inches);
  }
  
  //check that it is safe to drive forward (if moving forwards). 
  
  //check that it is safe to back up (if moving backwards).
  
  //all checks were good
  return true;  
}

void serialEvent(){
    have_signal=1;
    while(Serial.parseInt()!=-11111){}
    //Serial.parseInt();
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
    
    Serial.write((int)(1-blockVideo));
    
}

char* command;
void serialEvent1() {
  Serial.end();
    while (Serial1.available()) {
        // get the new byte:
        char inChar = (char)Serial1.read();
        // add it to the inputString:
        inputString += inChar;
        // if the incoming character is a newline, set a flag
        // so the main loop can do something about it:
        if (inChar == '\n') {
          stringComplete = true;
        }
      }
    if (stringComplete==true) {
    //Serial.print(inputString); //test received string
    // clear the string:
    inputString = "";
    stringComplete = false;
  }
  controllerLastSignal=millis();
 
 int beginIdx=0;
 int idx=inputString.indexOf(",");
 String arg;
 char charBuffer[16];
 int i=0;
 
 while(idx != -1){
     arg=inputString.substring(beginIdx,idx);
     arg.toCharArray(charBuffer,16);
     controllerValues[i++]=atoi(charBuffer);
     beginIdx=idx+1;
     idx=inputString.indexOf(",",beginIdx);
 }
  Serial.begin(115200);
 for(i=0;i<23;i++){
   Serial.print(controllerValues[i]);
   Serial.print(",");
 }
 Serial.println();

}




