# Pic0₂ξmeter
<img src="./Images/Logo_Picoxi.png" alt="Logo for the Picoximeter" width="200"/>

## Information
The Picoximeter is a TinyPico based pulse oximeter. It sends data to an OLED screen and you can connect to it with the app via BLE.

<img src="./Images/use.jpg" alt="Pulseoximeter with app in use." width="400"/>

## Disclaimer
This is not a medical device but created for research and development only.
The software and schematics are provided AS IS, without warranty or liability for any damages caused by use.

## Device
### Hardware
The pulse oximeter is based on a 3D printer case with a TinyPico, MAX30100 (Purple PCB), 128x64px OLED Screen, a sliding switch, a 220mAh battery and a spring from a pen. A different microcontroller with the same form factor could work as well i.e. an Adafruit ItsyBitsy though I have not tried that. This might compromise BLE functionally.

The stl files for the print are available in the repo. Please note that the tolerances of an 0.4mm nozzle are already accounted for. If you have problems with the tolerances I would recommend the "Expand horizontal" setting in Cura or a comparable setting in another slicer.
The soldering can be difficult for beginners as the space is quite narrow though it is possible with a basic soldering iron.

<img src="./Images/render.png" alt="Rendered image of the device case." height="300"/><img src="./Images/Assembly.jpg" alt="Image with an assembled Picoximeter which is opened to see the wires." height="300"/>

### Software
The code is writen in Arduino and mainly based on the examples given for the MAX30100, OLED and BLE libraries used. Those are available through the library manager by oxullo (MAX30100) and Adafruit (OLED), the BLE libraries come with the ESP32 board manager.

## App
The app is written in Java with Android Studio, the source code is provided. It has functionality for connecting to the device and reveiving updates when new data is available. The data can be stored and tagged. This data can be accessed via the home screen menu to be sorted, deleted and to edit the tag on a reading. Basic instructions are provided through mentioned menu as well.
The app is mainly focused on use in portrait mode, only some functionality has basic landscape layouts.

<img src="./Images/Day_UI.png" alt="Bright UI for the app." height="400"/><img src="./Images/Night_UI.png" alt="Das UI for the app." height="400"/>

## License
GNU General Public License v2.0

### External Libraries
No changes to the used code libraries have been made.
- [Pulsoximeter Code](https://github.com/oxullo/Arduino-MAX30100) from oxullo under [GNU General Public License v3.0](https://github.com/oxullo/Arduino-MAX30100/blob/master/LICENSE.md)
- [OLED Library](https://github.com/adafruit/Adafruit_SSD1306) "Copyright (c) 2012, Adafruit Industries All rights reserved." following [this licence agreement](https://github.com/adafruit/Adafruit_SSD1306/blob/master/license.txt)
- [ESP32 Arduino Core](https://github.com/espressif/arduino-esp32) from espressif under [GNU Lesser General Public License v2.1](https://github.com/espressif/arduino-esp32/blob/master/LICENSE.md)

