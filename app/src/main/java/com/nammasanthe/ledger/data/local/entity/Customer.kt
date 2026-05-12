package com.nammasanthe.ledger.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Customer entity — represents a market customer in the Room database.
 * The phone field has a UNIQUE index to prevent duplicate customer entries.
 */
@Entity(
    tableName = "customers",
    indices   = [Index(value = ["phone"], unique = true)]
)
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id        : Long   = 0,
    val name      : String,            // Required — max 60 chars (validated in VM)
    val phone     : String,            // Required — 10 digits, UNIQUE per §8
    val photoUri  : String?  = null,   // Optional — local file URI from camera/gallery
    val createdAt : Long    = System.currentTimeMillis()  // Auto-set on creation
)