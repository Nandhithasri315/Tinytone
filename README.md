# TinyTone

TinyTone is an Android application designed to help children improve pronunciation, speech clarity, and voice modulation through interactive speech exercises. The application provides pronunciation practice, real-time feedback, progress tracking, and achievement-based motivation in a completely offline environment.

## Features

* Pronunciation practice using Text-to-Speech and Speech Recognition
* Real-time pronunciation accuracy scoring
* Audio waveform visualization during recording
* Voice modulation exercises for soft, loud, and varied speech
* Achievement and badge system to encourage engagement
* Progress tracking with locally stored user data
* Fully offline functionality

## Tech Stack

* Kotlin
* Android SDK
* XML Layouts
* Room Database (SQLite)
* TextToSpeech API
* SpeechRecognizer API
* AudioRecord API
* SharedPreferences
* Kotlin Coroutines
* Material Design Components

## Architecture

The application follows the MVC (Model-View-Controller) architecture.

### Core Components

* **Word Practice Module** – Handles pronunciation exercises and accuracy evaluation.
* **Voice Analysis Module** – Processes recorded audio and generates feedback.
* **Badge Management System** – Awards achievements based on user progress.
* **Progress Tracking Module** – Stores and displays user statistics.
* **Waveform Visualizer** – Provides real-time visual feedback during recording.

## Screenshots

### Home Screen

<img width="238" height="422" alt="image" src="https://github.com/user-attachments/assets/337761d4-543f-4f36-864d-ef2be8eff083" />
<img width="191" height="413" alt="image" src="https://github.com/user-attachments/assets/8e46035e-20b8-490b-abea-d52685675373" />


### Pronunciation Practice

<img width="405" height="431" alt="image" src="https://github.com/user-attachments/assets/b5329e28-14a6-4d77-adb6-3ccaa3906d87" />
<img width="417" height="450" alt="image" src="https://github.com/user-attachments/assets/106905b2-6385-459b-a17e-f1e469d7a7c1" />


### Accuracy Result
<img width="198" height="416" alt="image" src="https://github.com/user-attachments/assets/e3292683-e2ff-4824-971c-ea959df293b4" />
<img width="307" height="326" alt="image" src="https://github.com/user-attachments/assets/0eb53d5a-063a-4931-b5a3-1dd70f4ff43d" />



## Installation

### Prerequisites

* Android Studio
* JDK 11 or higher
* Android SDK API 24+

### Steps

```bash
git clone https://github.com/your-username/tinytone.git
```

1. Open the project in Android Studio.
2. Sync Gradle dependencies.
3. Connect an Android device or launch an emulator.
4. Build and run the application.

## Future Enhancements

* Personalized learning recommendations
* Expanded vocabulary database
* Multiple difficulty levels
* Parent progress dashboard
* Enhanced speech analysis techniques

## Author

**Nandhithasri T**

Master of Computer Applications (MCA)
PSG College of Technology, Coimbatore
