package com.nammasanthe.ledger.utils

import android.content.Context
import android.util.Log

/**
 * TwilioConfig — Manages Twilio API credentials securely.
 *
 * Credentials can be sourced from:
 *   1. BuildConfig (build.gradle.kts buildConfigField)
 *   2. Environment variables
 *   3. Android Keystore (future: for production)
 *   4. Secure backend endpoint (future: recommended for production)
 *
 * DO NOT store credentials in source code or unencrypted shared preferences.
 */
object TwilioConfig {
    private val tag = "TwilioConfig"

    /**
     * Get Twilio Account SID from BuildConfig or environment.
     * To set this, add to app/build.gradle.kts:
     *   buildTypes {
     *       debug {
     *           buildConfigField "String", "TWILIO_ACCOUNT_SID", "\"your-account-sid-here\""
     *       }
     *   }
     */
    fun getAccountSid(context: Context): String? {
        return try {
            val clazz = Class.forName("com.nammasanthe.ledger.BuildConfig")
            val field = clazz.getField("TWILIO_ACCOUNT_SID")
            field.get(null) as? String
        } catch (e: Exception) {
            Log.w(tag, "TWILIO_ACCOUNT_SID not found in BuildConfig: ${e.message}")
            System.getenv("TWILIO_ACCOUNT_SID")
        }
    }

    /**
     * Get Twilio Auth Token from BuildConfig or environment.
     * To set this, add to app/build.gradle.kts:
     *   buildTypes {
     *       debug {
     *           buildConfigField "String", "TWILIO_AUTH_TOKEN", "\"your-auth-token-here\""
     *       }
     *   }
     */
    fun getAuthToken(context: Context): String? {
        return try {
            val clazz = Class.forName("com.nammasanthe.ledger.BuildConfig")
            val field = clazz.getField("TWILIO_AUTH_TOKEN")
            field.get(null) as? String
        } catch (e: Exception) {
            Log.w(tag, "TWILIO_AUTH_TOKEN not found in BuildConfig: ${e.message}")
            System.getenv("TWILIO_AUTH_TOKEN")
        }
    }

    /**
     * Get Twilio Phone Number (the number messages will be sent FROM).
     * To set this, add to app/build.gradle.kts:
     *   buildTypes {
     *       debug {
     *           buildConfigField "String", "TWILIO_PHONE_NUMBER", "\"+1234567890\""
     *       }
     *   }
     */
    fun getPhoneNumber(context: Context): String? {
        return try {
            val clazz = Class.forName("com.nammasanthe.ledger.BuildConfig")
            val field = clazz.getField("TWILIO_PHONE_NUMBER")
            field.get(null) as? String
        } catch (e: Exception) {
            Log.w(tag, "TWILIO_PHONE_NUMBER not found in BuildConfig: ${e.message}")
            System.getenv("TWILIO_PHONE_NUMBER")
        }
    }

    /**
     * Check if all required Twilio credentials are configured.
     */
    fun isConfigured(context: Context): Boolean {
        val accountSid = getAccountSid(context)
        val authToken = getAuthToken(context)
        val phoneNumber = getPhoneNumber(context)

        val configured = !accountSid.isNullOrBlank() &&
                !authToken.isNullOrBlank() &&
                !phoneNumber.isNullOrBlank()

        if (!configured) {
            Log.w(tag, "Twilio not fully configured. Missing: " +
                    "${if (accountSid.isNullOrBlank()) "ACCOUNT_SID " else ""}" +
                    "${if (authToken.isNullOrBlank()) "AUTH_TOKEN " else ""}" +
                    "${if (phoneNumber.isNullOrBlank()) "PHONE_NUMBER" else ""}")
        }

        return configured
    }

    /**
     * Create a TwilioSmsService instance if credentials are available.
     * Returns null if credentials are not configured.
     */
    fun createService(context: Context): TwilioSmsService? {
        return if (isConfigured(context)) {
            val accountSid = getAccountSid(context)!!
            val authToken = getAuthToken(context)!!
            val phoneNumber = getPhoneNumber(context)!!
            TwilioSmsService(context, accountSid, authToken, phoneNumber)
        } else {
            Log.w(tag, "Cannot create TwilioSmsService: credentials not configured")
            null
        }
    }
}
