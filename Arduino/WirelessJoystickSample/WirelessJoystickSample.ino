/*
// # 
// # Editor     : Tong Hui from DFRobot, based on Lauren from DFRobot v1.0 code
// # Date       : 18.01.2012

// # Product name: Wireless Gamepad v2.2 for Arduino
// # Product SKU : DFR0182
// # Code Version: 2.1

// # Description:
// # The sketch for using the gamepad and print the button state and the analog values of the gamepad
// # Enable the vibration function
// #   Send 'v' via the Arduino Serial monitor to enable the vibration
// #   Send 's' via the Arduino Serial monitor to stop it
 
*/

int buttonState[17];
int joystick[4];
int AnalogButton[2];

int inputCommand = 0;

#define virbrationMotorPin 2

void setup()
{
  Serial.begin(57600);  //Init the Serial baudrate
  Serial1.begin(9600);  //Init the Serial1 port to enable the xbee wireless communication
  InitIO();             //Initialize the inputs/outputs and the buffers
}

void InitIO(){ 
  for(int i = 0; i < 17; i++) pinMode(i, INPUT); 
  pinMode(virbrationMotorPin,OUTPUT);
  digitalWrite(virbrationMotorPin,LOW);  // Stop shacking of the gamepad
}

unsigned long timer = 0;

void loop()
{
  if(millis() - timer > 100){  // manage the updating freq of all the controlling information
    DataUpdate();  //read the buttons and the joysticks data
    printData();   //print the datas and states
    timer = millis(); 
  }
  
  if(Serial.available()){
    char input = Serial.read();
    
    switch(input){
      case 'v':
        Serial.println("Vibration");
        inputCommand = input;
        digitalWrite(virbrationMotorPin,HIGH);
        break;
      
      case 's':
        Serial.println("Stop");
        inputCommand = input;
        digitalWrite(virbrationMotorPin,LOW);
        break;
        
      default:
        break;
    }
  }
}

void DataUpdate(){
  
  for(int i = 3; i < 17; i++)  buttonState[i] = digitalRead(i);
  buttonState[0] = analogRead(0);
  buttonState[1] = analogRead(1);
  for(int i = 0; i < 4; i++)  joystick[i] = analogRead(i);
  for(int i = 4; i < 6; i++)  AnalogButton[i-4] = analogRead(i);
  
}

char* Buttons[17] = {"J2","J1",NULL,"S2","S1","UP","LEFT","DOWN","RIGHT","1","4","2","3","RZ1","RZ2","LZ1","LZ2"};
  // Buttons Nmes
//joystick: RB, LB , RY, RX, LY, LX
int controllerState[23];
void printData(){
//  for(int i = 0; i < 17; i++)  Serial.print(buttonState[i]),Serial.print(" ");
//  for(int i = 0; i < 4; i++)  Serial.print(joystick[i]),Serial.print(" ");
//  for(int i = 0; i < 2; i++)  Serial.print(AnalogButton[i]),Serial.print(" ");
//  Serial.println("");
  //Serial1.print("Button Pressed:");
  for(int i = 0; i < 2; i++){
    if(buttonState[i] < 100){
      //Serial1.print(Buttons[i]);
      //Serial1.print(",");
      controllerState[i]=1;
    }else{
      controllerState[i]=0;
    }
  }
  for(int i = 3; i < 17; i++){
    if(buttonState[i] == 0){
      //Serial1.print(Buttons[i]);
      //Serial1.print(",");
      controllerState[i]=1;
    }else{
      controllerState[i]=0;
    }
  }
  //Serial1.println("");
  //Serial1.print("Analog Sticks:");
  for(int i = 0; i < 4; i++){
    //Serial1.print(joystick[i]);
    //Serial1.print(",");
    controllerState[i+17]=joystick[i];
  }
  for(int i = 0; i < 2; i++){
    //Serial1.print(AnalogButton[i]);
    //Serial1.print(",");
    controllerState[i+21]=AnalogButton[i];
  }
  //Serial1.println("");
  //Serial1.println(-1);
  for(int i=0;i<23;i++){
    Serial1.print(controllerState[i]);
    Serial1.print(",");
  }
  Serial1.println("");
    
}
