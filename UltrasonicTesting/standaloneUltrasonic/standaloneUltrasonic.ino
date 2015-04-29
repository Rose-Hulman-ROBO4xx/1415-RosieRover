const int pwPin = 4; 
const int redLED = 7;
const int grnLED = 8;
const int buzzer = 3;
const int safeDist = 40;
//variables needed to store values
long pulse, inches, cm; 

void setup() { 
  //This opens up a serial connection to shoot the results back to the PC console
  Serial.begin(9600);
  pinMode(pwPin, INPUT);
  pinMode(redLED, OUTPUT);
  pinMode(grnLED, OUTPUT);
  pinMode(buzzer, OUTPUT);
} 


void loop() { 
  

  //Used to read in the pulse that is being sent by the MaxSonar device.
  //Pulse Width representation with a scale factor of 147 uS per Inch.

  pulse = pulseIn(pwPin, HIGH);
  //147uS per inch
  inches = pulse/147;

  Serial.print(inches);
  Serial.print("in, ");
  Serial.println();

  if (inches < safeDist) {
    digitalWrite(redLED,HIGH);
    digitalWrite(grnLED,LOW);
    analogWrite(buzzer,127);
  } else {
    digitalWrite(redLED,LOW);
    digitalWrite(grnLED,HIGH);
    analogWrite(buzzer,0);
  }


} 

