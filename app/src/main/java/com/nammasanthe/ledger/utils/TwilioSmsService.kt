package com.nammasanthe.ledger.utils

import android.content.Context
import android.util.Log
import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * TwilioSmsService — Programmatic SMS/WhatsApp sending via Twilio API.
 *
 * Usage:
 *   val service = TwilioSmsService(context, accountSid, authToken, twilioPhoneNumber)
 *   service.sendSms(phoneNumber, message) // SMS
 *   service.sendWhatsApp(phoneNumber, message) // WhatsApp
 *
 * Credentials should be stored securely:
 *   - BuildConfig (for debug): add TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_PHONE to build.gradle.kts
 *   - Production: use Android Keystore or secure backend endpoint
 *
 * DO NOT hardcode credentials in source code.
 */
class TwilioSmsService(
    private val accountSid: String,
    private val authToken: String,
    private val twilioPhoneNumber: String
) {
    private val tag = "TwilioSmsService"
    private var isInitialized = false

    init {
        try {
            Twilio.init(accountSid, authToken)
            isInitialized = true
            Log.d(tag, "Twilio initialized successfully")
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize Twilio: ${e.message}")
        }
    }

    /**
     * Send an SMS via Twilio (requires credentials configured).
     *
     * @param toPhoneNumber Recipient phone number (e.g., "+919663906075")
     * @param message SMS message text
     * @return True if message was sent successfully, false otherwise
     */
    suspend fun sendSms(toPhoneNumber: String, message: String): Boolean {
        return sendMessage(toPhoneNumber, message, isWhatsApp = false)
    }

    /**
     * Send a WhatsApp message via Twilio (requires WhatsApp Business API integration).
     *
     * @param toPhoneNumber Recipient phone number in WhatsApp format (e.g., "whatsapp:+919663906075")
     * @param message Message text
     * @return True if message was sent successfully, false otherwise
     */
    suspend fun sendWhatsApp(toPhoneNumber: String, message: String): Boolean {
        val whatsappNumber = if (toPhoneNumber.startsWith("whatsapp:")) {
            toPhoneNumber
        } else {
            "whatsapp:+91${toPhoneNumber.trimStart('0')}"
        }
        return sendMessage(whatsappNumber, message, isWhatsApp = true)
    }

    private suspend fun sendMessage(
        toPhoneNumber: String,
        message: String,
        isWhatsApp: Boolean
    ): Boolean {
        if (!isInitialized) {
            Log.e(tag, "Twilio not initialized. Check credentials.")
            return false
        }

        return withContext(Dispatchers.IO) {
            try {
                val msgType = if (isWhatsApp) "WhatsApp" else "SMS"
                Log.d(tag, "Sending $msgType to $toPhoneNumber")

                val msg = Message.creator(
                    PhoneNumber(toPhoneNumber),  // To number
                    PhoneNumber(twilioPhoneNumber),  // From number (Twilio account number)
                    message  // Message body
                ).create()

                Log.d(tag, "$msgType sent successfully. SID: ${msg.sid}")
                true
            } catch (e: Exception) {
                Log.e(tag, "Failed to send message: ${e.message}", e)
                false
            }
        }
    }

    /**
     * Optionally shutdown Twilio (currently unused but available for future cleanup).
     */
    fun shutdown() {
        // Twilio SDK doesn't expose a shutdown method; credentials persist for app lifetime
        Log.d(tag, "Twilio service shutdown")
    }
}
