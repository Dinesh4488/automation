#include <Servo.h>

const int trigPin = 10;
const int echoPin = 11;
const int ledPin = 9;  // LED pin

long duration;
int distance;

Servo myServo;

void setup() {
  Serial.begin(9600);
  myServo.attach(12);
  pinMode(trigPin, OUTPUT);
  pinMode(echoPin, INPUT);
  pinMode(ledPin, OUTPUT); // Set LED pin as OUTPUT
}

void loop() {
  for (int pos = 15; pos <= 165; pos++) {
    myServo.write(map(pos, 15, 165, 20, 160));
    distance = getSmoothedDistance();
    Serial.print(pos);
    Serial.print(",");
    Serial.println(distance);
    
    if (distance > 0 && distance < 40) {
      digitalWrite(ledPin, HIGH);  // Turn LED ON
    } else {
      digitalWrite(ledPin, LOW);   // Turn LED OFF
    }

    delay(5);
  }

  for (int pos = 165; pos >= 15; pos--) {
    myServo.write(map(pos, 15, 165, 20, 160));
    distance = getSmoothedDistance();
    Serial.print(pos);
    Serial.print(",");
    Serial.println(distance);
    
    if (distance > 0 && distance < 40) {
      digitalWrite(ledPin, HIGH);
    } else {
      digitalWrite(ledPin, LOW);
    }

    delay(5);
  }
}

int calculateDistance() {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  duration = pulseIn(echoPin, HIGH, 20000);
  if (duration == 0) return 400;
  return duration * 0.034 / 2;
}

int getSmoothedDistance() {
  int total = 0, validReadings = 0;
  
  for (int i = 0; i < 3; i++) {
    int d = calculateDistance();
    if (d > 0 && d < 400) {
      total += d;
      validReadings++;
    }
    delay(2);
  }
  
  return (validReadings > 0) ? total / validReadings : 400;
}
