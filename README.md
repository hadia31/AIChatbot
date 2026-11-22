# AI Chatbot - On-Device Intent Classification with TensorFlow Lite

An Android chat application (Kotlin) that combines **my own trained TensorFlow Lite model**, running on the phone, with the **Gemini API**.

For every message the user sends:

1. The **TFLite model** (on-device, no internet needed) detects the *intent* of the message: **Greeting, Question, Thanks or Goodbye**, and its confidence.
2. The message is sent to the **Gemini API**, which writes the answer.
3. The detected intent and the answer are shown together in a ChatGPT-style dark chat screen.

> Made for **Assignment No. 2 - AI-Based Mini Application Development: Integrating ML/DL Models into Android with TensorFlow Lite** (Mobile Application Development, BSCS 5A).
> Author: **Hadia Javed** (Reg. No. 242201017)

## Features

- On-device intent detection with a self-trained TFLite model and a confidence percentage
- AI answers from the Gemini API with a "Typing..." placeholder
- Automatic retry (up to 3 times) when the server is busy (HTTP 503 / 429) and clear English error messages
- ChatGPT-style dark interface with a custom app icon
- API key kept outside the source code (not uploaded to GitHub)

## Screenshots

Add your screenshots here, for example in a `screenshots/` folder:

```
![Greeting](screenshots/screen1.png)
![Question](screenshots/screen2.png)
```

## Tech stack

| Part | Technology |
|---|---|
| Language / IDE | Kotlin, Android Studio |
| UI | XML layouts, RecyclerView 1.3.2 |
| Networking | OkHttp 4.12.0, org.json |
| On-device ML | TensorFlow Lite / LiteRT (`com.google.ai.edge.litert:litert:1.4.0`) |
| Model training | Python, TensorFlow / Keras, Google Colab |
| Generative AI | Gemini API (`gemini-flash-lite-latest`) |

## Prerequisites

- Android Studio (recent version) with **JDK 17** selected as the Gradle JDK
- An Android phone (Android 7.0 / API 24 or higher) or an emulator
- Internet connection (for the Gemini answers)
- A free **Gemini API key** from Google AI Studio: https://aistudio.google.com
- Git

## Installation and running

1. **Clone** the repository

   ```
   git clone https://github.com/hadia31/AIChatbot.git
   ```

2. **Open** the folder in Android Studio (`File > Open`).
3. **Create your API key file.** Open (or create) `local.properties` in the project root folder and add one line (no spaces, no quotes):

   ```
   GEMINI_API_KEY=your_api_key_here
   ```

   `local.properties` is listed in `.gitignore`, so your key is never uploaded to GitHub.
4. **Set the Gradle JDK:** `File > Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK` -> choose **17**.
5. Click **Sync Now** and wait until Gradle finishes.
6. Connect a phone with USB debugging on (or start an emulator) and press **Run**.
7. Try these messages: `hello`, `what is python`, `thank you`, `bye`. The line `Detected intent: ...` appears above every answer.

> If the API key line is missing, the app still builds and the intent detection still works, but the Gemini answers fail with an error message.

## Project structure

```
AIChatbot
|-- app
|   |-- src/main
|   |   |-- assets
|   |   |   |-- chat_classifier.tflite   (my trained TFLite model)
|   |   |   |-- vocab.txt                (96 words known to the model)
|   |   |   `-- labels.txt               (greeting, question, thanks, goodbye)
|   |   |-- java/com/example/aichatbot
|   |   |   |-- MainActivity.kt          (screen, Gemini call, uses the classifier)
|   |   |   |-- IntentClassifier.kt      (loads and runs the TFLite model)
|   |   |   |-- Message.kt               (data class for one chat message)
|   |   |   `-- MessageAdapter.kt        (shows the messages in a RecyclerView)
|   |   |-- res/layout                   (activity_main.xml, item_message.xml)
|   |   |-- res/drawable                 (bubble and button shapes)
|   |   `-- AndroidManifest.xml
|   `-- build.gradle.kts
|-- local.properties                     (NOT on GitHub - holds your API key)
`-- README.md
```

## How the TFLite model was made

| Property | Value |
|---|---|
| Task | Text classification into 4 intents |
| Training data | 60 hand-written sentences (15 per intent) |
| Input | Bag-of-words vector with 96 values |
| Network | Dense(16, ReLU) -> Dense(4, Softmax) |
| Training | Adam, sparse categorical cross-entropy, 100 epochs |
| Training accuracy | about 96.7 % |

Steps: create the dataset -> train the Keras model in Google Colab -> convert with `tf.lite.TFLiteConverter` -> save `chat_classifier.tflite`, `vocab.txt`, `labels.txt` -> copy them into `app/src/main/assets`.

## Test results (Samsung SM-A155F)

| Message | Expected | Detected | Confidence |
|---|---|---|---|
| hello | Greeting | Greeting | 50 % |
| what is python | Question | Question | 66 % |
| thank you | Thanks | Thanks | 47 % |
| bye | Goodbye | Goodbye | 37 % |
| thankyou (one word) | Thanks | Greeting (wrong) | 27 % |

The word "thankyou" is not in the 96-word vocabulary, and the dataset is small. More training examples would improve accuracy and confidence.

## Acknowledgements

- Reference repository studied for this assignment: https://github.com/Uzair390-Del/NumberGuessingAppTflite.git
- Libraries and services: TensorFlow / Keras, TensorFlow Lite (LiteRT), OkHttp, AndroidX RecyclerView, Google Colab, Gemini API
- Dataset: the 60 training sentences were written by me for this project
- AI assistance: Claude (Anthropic) was used as a learning and coding assistant and to generate the app icon image
