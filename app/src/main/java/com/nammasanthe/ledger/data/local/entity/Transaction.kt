package com.nammasanthe.ledger.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Transaction types: CREDIT = Udari given to customer, PAYMENT = customer pays back */
enum class TransactionType { CREDIT, PAYMENT }

/**
 * Transaction entity — each row is one Udari (credit) or payment event.
 * FK → Customer with ON DELETE CASCADE: deleting a customer deletes all their transactions.
 */
@Entity(
    tableName      = "transactions",
    foreignKeys    = [
        ForeignKey(
            entity         = Customer::class,
            parentColumns  = ["id"],
            childColumns   = ["customerId"],
            onDelete       = ForeignKey.CASCADE   // cascade delete per §8 schema
        )
    ],
    indices = [Index("customerId")]   // index for fast customer-based queries
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id         : Long            = 0,
    val customerId : Long,           // FK → Customer.id (required)
    val type       : TransactionType, // CREDIT or PAYMENT (immutable after insert)
    val amount     : Double,          // Always > 0.0 (validated in ViewModel)
    val note       : String? = null,  // Optional — max 80 chars
    val timestamp  : Long   = System.currentTimeMillis()  // Auto-set, never user-editable
)