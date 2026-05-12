package com.nammasanthe.ledger.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.nammasanthe.ledger.data.local.entity.Customer
import com.nammasanthe.ledger.utils.FormatUtils

/**
 * WhatsAppHelper — constructs and launches the WhatsApp / SMS reminder intent.
 *
 * Flow (Intent-based, user interactive):
 *  1. Try to open WhatsApp directly (package: com.whatsapp).
 *  2. If WhatsApp is not installed, fall back to generic SMS Intent.
 *  3. If SMS also fails, show a Toast with the message text to copy manually.
 *
 * For programmatic sending (no user interaction), use TwilioSmsService instead.
 */
object WhatsAppHelper {

    /**
     * Send a payment reminder to a customer via Intent (user opens WhatsApp/SMS app manually).
     *
     * @param context       Android context (Activity or Application)
     * @param customer      The customer to remind
     * @param netBalance    How much the customer owes (positive = owes vendor)
     * @param vendorName    Vendor's name (from Settings) — appears in message
     */
    fun sendReminder(
        context    : Context,
        customer   : Customer,
        netBalance : Double,
        vendorName : String
    ) {
        val amountStr = FormatUtils.formatCurrency(netBalance)
        val message = buildReminderMessage(customer.name, amountStr, vendorName)

        val sent = tryWhatsApp(context, customer.phone, message)
        if (!sent) {
            val sentSms = trySms(context, customer.phone, message)
            if (!sentSms) {
                Toast.makeText(
                    context,
                    "Could not open WhatsApp or SMS. Message: $message",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Alternative: Send via Twilio (programmatic, no user interaction needed).
     * Requires TwilioSmsService to be configured with valid Twilio credentials.
     *
     * @param twilioService TwilioSmsService instance with credentials
     * @param customer      The customer to remind
     * @param netBalance    How much the customer owes
     * @param vendorName    Vendor's name
     */
    suspend fun sendReminderViaTwilio(
        twilioService: TwilioSmsService,
        customer: Customer,
        netBalance: Double,
        vendorName: String
    ) {
        val amountStr = FormatUtils.formatCurrency(netBalance)
        val message = buildReminderMessage(customer.name, amountStr, vendorName)

        // Attempt WhatsApp first, then SMS
        val whatsappSent = twilioService.sendWhatsApp(customer.phone, message)
        if (!whatsappSent) {
            twilioService.sendSms(customer.phone, message)
        }
    }

    private fun buildReminderMessage(
        customerName: String,
        amount      : String,
        vendorName  : String
    ): String {
        // Bilingual message (Kannada + English) so all customers understand
        return "Namaskara $customerName,\n" +
                "Namma Santhe Ledger — $vendorName:\n" +
                "Neevu $amount kodabekide. (Your due amount is $amount)\n" +
                "Dhanyavada / Thank you."
    }

    private fun tryWhatsApp(ctx: Context, phone: String, message: String): Boolean {
        return try {
            // WhatsApp expects phone in international format: +91XXXXXXXXXX
            val intlPhone = "+91${phone.trimStart('0')}"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$intlPhone?text=${Uri.encode(message)}")
                setPackage("com.whatsapp")
            }
            if (intent.resolveActivity(ctx.packageManager) != null) {
                ctx.startActivity(intent)
                true
            } else false
        } catch (e: Exception) { false }
    }

    private fun trySms(ctx: Context, phone: String, message: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$phone")
                putExtra("sms_body", message)
            }
            ctx.startActivity(intent)
            true
        } catch (e: Exception) { false }
    }
}