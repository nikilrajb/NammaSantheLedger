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

## Requirements

- Android 9.0 or higher
- Internet permission (for WhatsApp/SMS functionality)

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Author

**Nikil Raj B** - [GitHub](https://github.com/nikilrajb)

## Contributing

Contributions are welcome! Feel free to open an issue or submit a pull request.

---

**Note**: This is an open-source project. Please ensure you follow the licensing terms when using or distributing this application.
