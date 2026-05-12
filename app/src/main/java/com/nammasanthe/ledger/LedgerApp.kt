package com.nammasanthe.ledger

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * LedgerApp — the Application class for the entire app.
 *
 * @HiltAndroidApp is REQUIRED for Hilt DI to work.
 * Without it, Hilt cannot inject dependencies anywhere.
 *
 * This class MUST be declared in AndroidManifest.xml:
 *   android:name=".LedgerApp"
 */
@HiltAndroidApp
class LedgerApp : Application()