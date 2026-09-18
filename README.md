# NEXO Water Quality — Final App

Version 7.0

## Included
- Seven parameter colour-reference palettes based on the supplied reference chart.
- Full pH 1–14 colour reference.
- Alkalinity: 0, 40, 80, 120, 180, 250, 720.
- Water Hardness: 0, 50, 120, 180, 250, 425.
- Iron: 0, 0.1, 0.3, 1.0, 5.0.
- Free Chlorine: 0, 0.1, 0.2, 0.5, 0.8, 4.0.
- Nitrate: 0, 0.5, 1, 5, 10.
- Nitrite: 0, 0.5, 1, 5, 10.
- Selected-parameter reference palette plus a "View All 7 Reference Charts" screen.
- HSV/RGB colour matching with rejection of colours that are too far from the selected palette.
- 1–5 test assessment workflow.
- Intended water-use selection, local history, GPS, map and report sharing.

## Important calibration note
The RGB values are approximate references extracted from the supplied chart image. For a real measurement device, calibrate against the exact manufacturer chart/kit under controlled lighting and validate against known standards. The app is a field-screening prototype and should not be treated as laboratory certification.

## Build
Use Android Studio with JDK 21. Sync Gradle and run on a physical Android device with camera/location permissions enabled.
