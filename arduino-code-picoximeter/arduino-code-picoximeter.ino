// imports for PulOx
#include <Wire.h>
#include "MAX30100_PulseOximeter.h"
// imports for Display
#include <SPI.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
// imports for BLE
#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <esp_system.h>
#include <BLE2902.h>

PulseOximeter pox;
#define REPORTING_PERIOD_MS 1000
uint32_t tsLastReport = 0;

#define SCREEN_WIDTH 128 // OLED display width, in pixels
#define SCREEN_HEIGHT 32 // OLED display height, in pixels
#define OLED_RESET     4 // Reset pin # (or -1 if sharing Arduino reset pin)
#define SCREEN_ADDRESS 0x3C ///< See datasheet for Address; 0x3D for 128x64, 0x3C for 128x32
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);
#define BLACK 0x0000
#define WHITE 0xFFFF

#define SERVICE_UUID        "00001822-0000-1000-8000-00805f9b34fb" //specific to pulse oximetry services
#define CHARACTERISTIC_UUID "d761c8ea-1ac4-11ec-9621-0242ac130002" //choosen arbitrarily
BLECharacteristic *aCharacteristic;

String addZeroes(int number){
  String text = "";
  if(number >= 10){
    if(number >= 100){
      return "";
    }
    return "0";
  }
  return "00";
}

void setup() {
  Serial.begin(115200);

  if(!display.begin(SSD1306_SWITCHCAPVCC, SCREEN_ADDRESS)) {
    Serial.println(F("SSD1306 allocation failed"));
    for(;;); // Don't proceed, loop forever
  }

  display.clearDisplay();
  display.display();

  //BLE server is being initialized
  //create one BLEService and one Characteristic
  BLEDevice::init("Picoximeter");
  BLEServer *aServer = BLEDevice::createServer();
  
  //uuid for the BLE service is set
  BLEService *aService = aServer->createService(SERVICE_UUID);
  //uuid for the BLE characteristic is set
  //the characteristics properties are defined
  aCharacteristic = aService->createCharacteristic(
                     CHARACTERISTIC_UUID,
                     BLECharacteristic::PROPERTY_READ   |
                     BLECharacteristic::PROPERTY_NOTIFY  
                   );
  aCharacteristic->addDescriptor(new BLE2902());
  
  //BLE server is being started
  aService->start();
  BLEAdvertising *aAdvertising = aServer->getAdvertising();
  aAdvertising->addServiceUUID(SERVICE_UUID);
  aAdvertising->start();

  if (!pox.begin()) {
    Serial.println("FAILED");
    for(;;);
  } else {
    Serial.println("SUCCESS");
  }
}

void loop() {
  pox.update();
  int hr = (int) pox.getHeartRate();
  int spO2 = (int) pox.getSpO2();
  
  if (millis() - tsLastReport > REPORTING_PERIOD_MS) {
    Serial.print("Heart rate:");
    Serial.print(hr);
    Serial.print("bpm / SpO2:");
    Serial.print(spO2);
    Serial.println("%");

    display.clearDisplay();
    display.setCursor(0, 0);
    display.setTextSize(1);
    display.setTextColor(SSD1306_WHITE);
    display.println(F(""));
    
    display.print("HR:   ");
    display.setTextColor(SSD1306_BLACK, SSD1306_BLACK);
    display.print(addZeroes(hr));
    display.setTextColor(SSD1306_WHITE, SSD1306_BLACK);
    display.print(hr);
    
    display.print(" bpm\nSpO2: ");
    display.setTextColor(SSD1306_BLACK, SSD1306_BLACK);
    display.print(addZeroes(spO2));
    display.setTextColor(SSD1306_WHITE, SSD1306_BLACK);
    display.print(spO2);
    display.print("%");

    display.display();
    
    String toSend = String(hr) + ";" + String(spO2);
    
    aCharacteristic->setValue(toSend.c_str());
    aCharacteristic->notify();
    
    tsLastReport = millis();
  }
}
