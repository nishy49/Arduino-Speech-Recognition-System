#include <AFMotor.h> 
#include <Servo.h>   
String voice;

AF_DCMotor motor1 (1, MOTOR12_1KHZ); 
AF_DCMotor motor2 (2, MOTOR12_1KHZ); 
AF_DCMotor motor3 (3, MOTOR12_1KHZ); 
AF_DCMotor motor4 (4, MOTOR12_1KHZ); 
Servo myServo; 

void setup()
{
  Serial.begin(9600); 
  myServo.attach(10); 
  myServo.write(90);  

}

void loop() 
{
  while (Serial.available()){ 
     delay(10); 
    char c = Serial.read(); 
    if (c == '#') {break;} 
    voice += c; 
  }
  if (voice.length() > 0){
    if(voice == "*go ahead"){
      forward();
      }
    else if(voice == "*go back"){
      back();
      }
    else if(voice == "*right") {
      right();
    }
    else if(voice == "*left") {
      left();
    }
    else if(voice == "*stop") {
      stop_car();
    }
    
  voice=""; 
  }
}

void forward()
{
  motor1.run(FORWARD);
  motor1.setSpeed(700);
  motor2.run(FORWARD);
  motor2.setSpeed(700);
  motor3.run(FORWARD);
  motor3.setSpeed(700);
  motor4.run(FORWARD);
  motor4.setSpeed(700);
  delay(2000);
  motor1.run(RELEASE);