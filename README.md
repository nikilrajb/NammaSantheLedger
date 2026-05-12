# Namma Santhe Ledger

A modern Android application for managing customer transactions and credit ledgers. Designed for small businesses and traders to easily track customer payments, outstanding balances, and generate daily transaction summaries.

## Features

- **Customer Management**: Add, edit, and manage customer information
- **Transaction Tracking**: Record and track customer transactions (payments and credit)
- **Daily Summaries**: View daily transaction summaries and outstanding balances
- **Balance Tracking**: Monitor customer outstanding balances and payment history
- **WhatsApp/SMS Integration**: Quick customer contact options via WhatsApp or SMS
- **Material 3 Design**: Modern, intuitive UI with Material Design 3
- **Local Database**: All data stored locally on the device using Room

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Database**: Room (SQLite)
- **Architecture**: MVVM + Repository Pattern
- **Dependency Injection**: Hilt
- **Minimum SDK**: Android 9 (API 28)
- **Target SDK**: Android 14 (API 34)

## Getting Started

### Prerequisites

- Android Studio (Arctic Fox or newer)
- JDK 11 or higher
- Android SDK (API 28+)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/nikilrajb/NammaSantheLedger.git
   cd NammaSantheLedger
   ```

2. **Open in Android Studio**
   - File → Open → Select the project directory

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run the app**
   - Connect an Android device or start an emulator
   - Run → Run 'app'

## Project Structure

```
app/src/main/java/com/nammasanthe/ledger/
├── data/              # Data layer (Room, Repositories)
├── di/                # Dependency injection (Hilt)
├── ui/                # UI layer (Screens, Components, Theme)
└── utils/             # Utility functions

app/src/main/res/      # Resources (layouts, colors, strings)
```

## Usage

1. **Add Customer**: Tap the "+" button to add a new customer
2. **Record Transaction**: Navigate to the ledger and add transactions for customers
3. **View Summary**: Check the daily summary page for transaction overview
4. **Contact Customer**: Use the WhatsApp or SMS option to quickly reach customers

## WhatsApp/SMS Integration

The app supports two methods for sending reminders:

### Method 1: Intent-Based (User Interactive)
- Opens WhatsApp or SMS app on the device
- No external credentials required
- User manually sends the message
- **Current implementation** in `WhatsAppHelper.sendReminder()`

### Method 2: Twilio API (Programmatic)
- Sends SMS or WhatsApp automatically
- Requires Twilio account and credentials
- No user interaction needed
- Better for automated workflows

#### Setting Up Twilio

1. **Create a Twilio Account**
   - Sign up at [twilio.com](https://www.twilio.com)
   - Get your Account SID and Auth Token from the [Twilio Console](https://www.twilio.com/console)
   - Verify or purchase a phone number for sending messages

2. **Configure Credentials in Android Studio**
   
   **Option A: BuildConfig (for development)**
   
   Add to `app/build.gradle.kts` in the `android { buildTypes { debug { } } }` section:
   ```kotlin
   android {
       buildTypes {
           debug {
               buildConfigField "String", "TWILIO_ACCOUNT_SID", "\"your-account-sid\""
               buildConfigField "String", "TWILIO_AUTH_TOKEN", "\"your-auth-token\""
               buildConfigField "String", "TWILIO_PHONE_NUMBER", "\"+1234567890\""
           }
       }
   }
   ```
   
   **⚠️ WARNING**: Never commit credentials to git. Use local.properties or environment variables instead.
   
   **Option B: local.properties (recommended for development)**
   
   Add to `local.properties` (git-ignored):
   ```properties
   TWILIO_ACCOUNT_SID=your-account-sid
   TWILIO_AUTH_TOKEN=your-auth-token
   TWILIO_PHONE_NUMBER=+1234567890
   ```
   
   Then reference in `app/build.gradle.kts`:
   ```kotlin
   val properties = Properties().apply {
       load(file("${rootProject.rootDir}/local.properties").inputStream())
   }
   
   android {
       buildTypes {
           debug {
               buildConfigField "String", "TWILIO_ACCOUNT_SID", 
                   "\"${properties.getProperty("TWILIO_ACCOUNT_SID", "")}\""
               buildConfigField "String", "TWILIO_AUTH_TOKEN",
                   "\"${properties.getProperty("TWILIO_AUTH_TOKEN", "")}\""
               buildConfigField "String", "TWILIO_PHONE_NUMBER",
                   "\"${properties.getProperty("TWILIO_PHONE_NUMBER", "")}\""
           }
       }
   }
   ```

3. **Use in Code**
   ```kotlin
   // Create service
   val twilioService = TwilioConfig.createService(context)
   
   // Send WhatsApp
   twilioService?.sendWhatsApp("+919663906075", "Your message here")
   
   // Send SMS
   twilioService?.sendSms("+919663906075", "Your message here")
   
   // Or use the convenience function
   WhatsAppHelper.sendReminderViaTwilio(twilioService, customer, balance, vendorName)
   ```

4. **Production Deployment**
   
   For production, store credentials securely:
   - **Backend API**: Store credentials on server and call API from app
   - **Android Keystore**: Encrypt credentials using system keystore (more complex)
   - **Cloud Config**: Use Firebase Remote Config or similar
   
   Avoid embedding credentials in APK/AAB.

#### Twilio SDK Details

- **SDK**: `com.twilio.sdk:twilio:9.2.0`
- **Classes**: 
  - `TwilioSmsService` - Main service for sending messages
  - `TwilioConfig` - Credential management helper
  - `WhatsAppHelper.sendReminderViaTwilio()` - Convenience function

## Requirements

- Android 9.0 or higher
- Internet permission (for WhatsApp/SMS functionality)
- **Optional**: Twilio account for programmatic SMS/WhatsApp sending

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Author

**Nikil Raj B** - [GitHub](https://github.com/nikilrajb)

## Contributing

Contributions are welcome! Feel free to open an issue or submit a pull request.

---

**Note**: This is an open-source project. Please ensure you follow the licensing terms when using or distributing this application.
