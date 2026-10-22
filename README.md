# NEXO Water Quality — Android App

**A camera-based water-quality screening prototype**

NEXO uses colour-reference matching to help users compare water-test strip readings against reference palettes. It is an Android prototype intended for field screening and demonstration.

## Features

- Seven parameter colour-reference palettes based on the supplied chart
- pH reference scale from 1–14
- Alkalinity, hardness, iron, free chlorine, nitrate, and nitrite reference values
- Selected-parameter palette and a screen to view all seven charts
- HSV/RGB colour matching with rejection of colours too far from the selected palette
- Workflow for one to five tests
- Intended-use selection, local history, GPS, map, and report sharing

## Technology and build

- Android Studio
- Kotlin / Gradle
- JDK 21
- Camera and location permissions for the relevant functions

To run locally, open the repository in Android Studio, use JDK 21, sync Gradle, and run the app on a physical Android device. Grant camera/location permissions when requested.

## Calibration and limitations

The RGB values are approximate references derived from the supplied chart image. Lighting, camera sensor, strip chemistry, timing, and display differences can affect colour matching. For any real measurement device, calibrate against the exact manufacturer's chart or kit under controlled lighting and validate against known standards.

**This app is a field-screening prototype. It is not laboratory equipment and does not provide certified water-quality measurements.**
