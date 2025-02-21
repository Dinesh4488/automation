import processing.serial.*;
Serial myPort;
String data = "";
int iAngle, iDistance;
float pixsDistance;
boolean objectDetected = false;
ArrayList<PVector> detectedObjects = new ArrayList<>();
int fadeTime = 5000; // Time to fade old detections (in ms)
int lastResetTime = 0; // Track last full sweep reset

void setup() {
  size(1200, 700);
  smooth();
  myPort = new Serial(this, "COM12", 9600);
  myPort.bufferUntil('\n'); // Read until newline for better parsing
}

void draw() {
  background(0);
  drawRadar();
  drawLine();
  drawObjects();
  drawText();
  drawIndicator();
}

void serialEvent(Serial myPort) {
  String rawData = myPort.readStringUntil('\n');
  if (rawData != null) {
    rawData = trim(rawData);
    println("Received: " + rawData); // Debugging
    String[] values = split(rawData, ',');
    
    if (values.length == 2) {
      try {
        iAngle = int(values[0]);
        iDistance = int(values[1]);

        // Ensure valid distance values
        if (iDistance < 0 || iDistance > 400) {
          iDistance = 400; // Assume max range if out of bounds
        }
        
        objectDetected = (iDistance > 0 && iDistance < 40); // Detect object

        if (objectDetected) {
          detectedObjects.add(new PVector(iAngle, iDistance, millis())); // Store time of detection
        }

        // Clear old detections after full sweep
        if (iAngle == 165) {
          lastResetTime = millis();
        }
      } catch (Exception e) {
        println("Error parsing data: " + e.getMessage());
      }
    }
  }
}

void drawRadar() {
  pushMatrix();
  translate(width/2, height-height*0.074);
  noFill();
  stroke(98, 245, 31);
  strokeWeight(2);
  for (int i = 1; i <= 4; i++) {
    arc(0, 0, width * (0.687 - (i * 0.2)), width * (0.687 - (i * 0.2)), PI, TWO_PI);
  }
  for (int a = 30; a <= 150; a += 30) {
    line(0, 0, -width/2 * cos(radians(a)), -width/2 * sin(radians(a)));
  }
  popMatrix();
}

void drawObjects() {
  pushMatrix();
  translate(width/2, height-height*0.074);
  strokeWeight(9);

  for (int i = detectedObjects.size() - 1; i >= 0; i--) {
    PVector obj = detectedObjects.get(i);
    float elapsed = millis() - obj.z;
    
    if (elapsed > fadeTime) {
      detectedObjects.remove(i); // Remove old detections gradually
      continue;
    }
    
    float objPixDist = obj.y * ((height - height * 0.1666) * 0.025);
    
    // Fading effect based on elapsed time
    stroke(255, 10, 10, map(elapsed, 0, fadeTime, 255, 0));
    line(objPixDist * cos(radians(obj.x)), -objPixDist * sin(radians(obj.x)),
         (width - width * 0.505) * cos(radians(obj.x)), -(width - width * 0.505) * sin(radians(obj.x)));
  }
  popMatrix();
}

void drawLine() {
  pushMatrix();
  strokeWeight(9);
  stroke(30, 250, 60);
  translate(width/2, height-height*0.074);
  line(0, 0, (height - height * 0.12) * cos(radians(iAngle)), -(height - height * 0.12) * sin(radians(iAngle)));
  popMatrix();
}

void drawText() {
  pushMatrix();
  fill(98, 245, 31);
  textSize(25);
  String distanceText = (iDistance < 400) ? (iDistance + " cm") : "Out of Range";
  text("Angle: " + iAngle + "°", width - width * 0.48, height - height * 0.0277);
  text("Distance: " + distanceText, width - width * 0.26, height - height * 0.0277);
  popMatrix();
}

void drawIndicator() {
  pushMatrix();
  fill(objectDetected ? color(255, 0, 0) : color(0, 255, 0)); // Red if object detected, Green otherwise
  ellipse(width - 50, 50, 30, 30); // Indicator light in the top-right corner
  popMatrix();
}
