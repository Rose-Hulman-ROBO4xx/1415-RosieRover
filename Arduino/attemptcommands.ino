//global command ID's
int CMD_DRIVE=100;

//global vars
int LEFT_MOTOR_DIR=2;
int RIGHT_MOTOR_DIR=4;
int LEFT_MOTOR_PWM=3;
int RIGHT_MOTOR_PWM=5;


unsigned long comTimeout=0;
unsigned long time;
int motorSpeedL;
int motorSpeedR;
int x;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);               //initial the Serial
    pinMode(2,OUTPUT);
    pinMode(3,OUTPUT);
    pinMode(4,OUTPUT);
    pinMode(5,OUTPUT);

}

void loop() {
  // put your main code here, to run repeatedly:
  if(Serial.available()){
    
    //parse serial into command
    
    x=Serial.parseInt();
    //Serial.write(x);
        
    //call command
    if (x==CMD_DRIVE){
      drive();
    }
  }
    
    //do constant motor things
    if(millis()>comTimeout){ //previous command has ended
      motorSpeedL=0;
      motorSpeedR=0;
      
    }
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

void drive(){
  
      motorSpeedL=Serial.parseInt();
      motorSpeedR=Serial.parseInt();
      comTimeout=millis()+Serial.parseInt();
      Serial.write("Driving,");
      Serial.print(motorSpeedL);
      Serial.print(motorSpeedR);
      
      
      //set direction
      if (motorSpeedL>0){
        digitalWrite(LEFT_MOTOR_DIR,HIGH);
      }else{
        digitalWrite(LEFT_MOTOR_DIR,LOW);
      }
      if (motorSpeedR>0){
        digitalWrite(RIGHT_MOTOR_DIR,HIGH);
      }else{
        digitalWrite(RIGHT_MOTOR_DIR,LOW);
      }
      

}


